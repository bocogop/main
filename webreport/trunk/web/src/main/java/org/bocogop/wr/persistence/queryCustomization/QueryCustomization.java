package org.bocogop.wr.persistence.queryCustomization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.persistence.queryCustomization.ModelAssociationFieldInfoRegistry.ModelAssociationFieldInfo;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class representing a set of customizations to make when running a JPA
 * query, such as which fields to prefetch, or how many rows to return. CPB
 */
public class QueryCustomization implements Serializable {
	private static final long serialVersionUID = 4604749503370779962L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(QueryCustomization.class);

	// ------------------------------------------ Fields

	private List<PrefetchInfo> prefetchItems;

	private Integer startIndex = null;
	private Integer rowLimitation = null;

	/*
	 * Sanity-check variable to ensure we're calling things in the right order
	 * when building the query - CPB
	 */
	private boolean prefetchJoinsApplied = false;

	private List<String> extraJoins;
	private String orderBy;

	// ------------------------------------------ Constructors

	public QueryCustomization() {
	}

	public QueryCustomization(ModelAssociationFieldType... prefetchFields) {
		this(null, null, prefetchFields);
	}

	public QueryCustomization(Integer startIndex, Integer rowLimitation, ModelAssociationFieldType... prefetchFields) {
		this(startIndex, rowLimitation);
		if (ArrayUtils.isNotEmpty(prefetchFields)) {
			for (ModelAssociationFieldType t : prefetchFields)
				prefetchField(t, null);
		}
	}

	public QueryCustomization(PrefetchInfo... prefetchItems) {
		this(null, null, prefetchItems);
	}

	public QueryCustomization(Integer startIndex, Integer rowLimitation, PrefetchInfo... prefetchItems) {
		this(startIndex, rowLimitation);
		if (ArrayUtils.isNotEmpty(prefetchItems)) {
			for (PrefetchInfo t : prefetchItems)
				prefetchField(t);
		}
	}

	public QueryCustomization(Integer startIndex, Integer rowLimitation) {
		setStartIndex(startIndex);
		setRowLimitation(rowLimitation);
	}

	// ------------------------------------------ Common methods

	/*
	 * Necessary to implement this if we want to use QueryCustomization params
	 * in @Cacheable methods - CPB
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(prefetchItems).append(startIndex).append(rowLimitation).toHashCode();
	}

	// ------------------------------------------ Business methods

	public boolean hasPrefetchFields() {
		return CollectionUtils.isNotEmpty(prefetchItems);
	}

	/* Simple initial implementation */
	public boolean isFieldPrefetched(ModelAssociationFieldType fieldType) {
		for (PrefetchInfo info : getPrefetchFields())
			if (info.getFieldType() == fieldType)
				return true;
		return false;
	}

	public void appendRequiredJoin(StringBuilder sb, boolean useLeftJoin, String parentAlias,
			ModelAssociationFieldType field, String fieldAlias) {
		if (prefetchJoinsApplied)
			throw new IllegalStateException("The method " + QueryCustomization.class.getName()
					+ ".appendRequiredJoin was called AFTER the method"
					+ " appendAnyPrefetchJoins. All required joins must be"
					+ " added before the prefetch joins method is called.");

		sb.append(useLeftJoin ? " left join " : " join ");

		if (hasPrefetchFields()) {
			for (PrefetchInfo info : getPrefetchFields()) {
				if (info.isApplied() || info.isForceNewJoin() || info.getFieldType() != field)
					continue;
				if (StringUtils.isNotEmpty(info.getSelectAlias()) && !info.getSelectAlias().equals(parentAlias))
					continue;
				if (StringUtils.isNotEmpty(info.getAlias()) && !info.getAlias().equals(fieldAlias))
					throw new IllegalStateException("The DAO specified an alias of '" + fieldAlias + "' for the "
							+ field.getFieldName() + " field but the " + getClass().getSimpleName()
							+ " requested the alias '" + info.getAlias() + "'. Please change the "
							+ getClass().getSimpleName() + " to match.");
				sb.append(" fetch ");
				info.flagAsApplied();
			}

			logOuterJoinFetchDupsWarningIfNecessary(field);
		}

		if (StringUtils.isNotEmpty(parentAlias))
			sb.append(parentAlias).append(".");
		sb.append(field.getFieldName());
		if (StringUtils.isNotEmpty(fieldAlias)) {
			sb.append(" ").append(fieldAlias);
		}
	}

	public void appendRemainingJoins(StringBuilder sb, String... selectEntityAliases) {
		prefetchJoinsApplied = true;

		if (!hasPrefetchFields())
			return;

		trimPrefetchFieldsDueToCaching();

		if (ArrayUtils.isEmpty(selectEntityAliases)) {
			selectEntityAliases = new String[] { null };
		}

		for (PrefetchInfo info : getPrefetchFields()) {
			if (info.isApplied()) {
				/*
				 * This prefetch request was already applied to one of the
				 * existing joins; no need to join it a second time just for
				 * fetching
				 */
				continue;
			}

			logOuterJoinFetchDupsWarningIfNecessary(info.getFieldType());

			for (String selectAlias : selectEntityAliases) {
				if (StringUtils.isNotEmpty(info.getSelectAlias()) && !info.getSelectAlias().equals(selectAlias))
					continue;

				sb.append(" left join fetch ");
				if (StringUtils.isNotBlank(selectAlias))
					sb.append(selectAlias).append(".");
				sb.append(info.getFieldType().getFieldName());
				if (StringUtils.isNotEmpty(info.getAlias()))
					sb.append(" ").append(info.getAlias());
			}
		}

		if (extraJoins != null)
			for (String extraJoin : extraJoins) {
				sb.append(" ").append(extraJoin);
			}
	}

	public void appendOrderBy(StringBuilder sb) {
		if (StringUtils.isNotEmpty(orderBy))
			sb.append(" order by ").append(orderBy);
	}

	/*
	 * This method removes any prefetch fields that are cached at the read-only
	 * level and have already been populated in the cache (either partially or
	 * fully). We'll pull these the first time using a join fetch but each
	 * subsequent time it will be faster to pull them from the cache. CPB
	 */
	private void trimPrefetchFieldsDueToCaching() {
		for (Iterator<PrefetchInfo> it = prefetchItems.iterator(); it.hasNext();) {
			PrefetchInfo entry = it.next();
			ModelAssociationFieldInfo pathField = ModelAssociationFieldInfoRegistry.getForField(entry.getFieldType());
			if (pathField.isReadOnly() && pathField.isAlreadyCached()) {
				it.remove();
			}
		}

		for (PrefetchInfo item : prefetchItems) {
			ModelAssociationFieldInfo pathField = ModelAssociationFieldInfoRegistry.getForField(item.getFieldType());
			if (pathField.isReadOnly()) {
				pathField.setCached();
			}
		}
	}

	public void applyQueryModifications(Query q) {
		if (startIndex != null) {
			q.setFirstResult(getStartIndex());
		}

		if (rowLimitation != null) {
			q.setMaxResults(getRowLimitation());
		}
	}

	private void logOuterJoinFetchDupsWarningIfNecessary(ModelAssociationFieldType field) {
		ModelAssociationFieldInfo info = ModelAssociationFieldInfoRegistry.getForField(field);
		if (info.getPropertyType().isArray() || (Collection.class.isAssignableFrom(info.getPropertyType())
				&& !Set.class.isAssignableFrom(info.getPropertyType()))) {
			String msg = "ERROR: duplicates will likely be generated in the " + field.getModelClass().getName() + "."
					+ field.getFieldName()
					+ " collection since you are performing an outer join fetch and it is not a subclass of "
					+ Set.class.getName() + "; see Hibernate FAQs for details.";
			throw new RuntimeException(msg);
		}
	}

	public QueryCustomization prefetchField(ModelAssociationFieldType prefetchField, String alias) {
		return prefetchField(PrefetchInfo.forExistingJoins(prefetchField, alias));
	}

	public QueryCustomization prefetchField(ModelAssociationFieldType prefetchField, String selectAlias, String alias) {
		return prefetchField(PrefetchInfo.forExistingJoins(selectAlias, prefetchField, alias));
	}

	public QueryCustomization prefetchField(PrefetchInfo item) {
		getPrefetchFields().add(item);
		return this;
	}

	public QueryCustomization addExtraJoin(String joinFragment) {
		getExtraJoins().add(joinFragment);
		return this;
	}

	public QueryCustomization setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	// ------------------------------------------ Accessor Methods

	public List<PrefetchInfo> getPrefetchFields() {
		if (prefetchItems == null)
			prefetchItems = new ArrayList<>();
		return prefetchItems;
	}

	public Integer getRowLimitation() {
		return rowLimitation;
	}

	public QueryCustomization setRowLimitation(Integer rowLimitation) {
		this.rowLimitation = rowLimitation;
		return this;
	}

	public Integer getStartIndex() {
		return startIndex;
	}

	public QueryCustomization setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
		return this;
	}

	public List<String> getExtraJoins() {
		if (extraJoins == null)
			extraJoins = new ArrayList<>();
		return extraJoins;
	}

	public String getOrderBy() {
		return orderBy;
	}

}
