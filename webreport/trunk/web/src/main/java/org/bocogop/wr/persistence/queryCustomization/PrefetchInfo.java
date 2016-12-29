package org.bocogop.wr.persistence.queryCustomization;

import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public class PrefetchInfo {
	/*
	 * The field relative to an item being selected that we want to prefetch
	 */
	private ModelAssociationFieldType fieldType;
	/*
	 * If we are selecting multiple items and we only want to prefetch one of
	 * those items, specify the alias of the selected item here.
	 */
	private String selectAlias;
	/*
	 * The alias of the new item being prefetched, if not already specified by
	 * the DAO via appendRequiredJoin(). May be null if no alias is needed.
	 */
	private String alias;
	/*
	 * Whether we should add a whole new "left join fetch xyz" at the end of
	 * this query or just add "fetch" to an existing join in the query (the
	 * default)
	 */
	private boolean forceNewJoin = false;

	private boolean applied = false;

	public static PrefetchInfo asNewJoin(ModelAssociationFieldType fieldType, String alias) {
		return new PrefetchInfo(fieldType, null, alias, true);
	}

	public static PrefetchInfo asNewJoin(String selectAlias, ModelAssociationFieldType fieldType, String alias) {
		return new PrefetchInfo(fieldType, selectAlias, alias, true);
	}

	public static PrefetchInfo forExistingJoins(ModelAssociationFieldType fieldType, String alias) {
		return new PrefetchInfo(fieldType, null, alias, false);
	}

	public static PrefetchInfo forExistingJoins(String selectAlias, ModelAssociationFieldType fieldType, String alias) {
		return new PrefetchInfo(fieldType, selectAlias, alias, false);
	}

	private PrefetchInfo(ModelAssociationFieldType fieldType, String selectAlias, String alias, boolean forceNewJoin) {
		this.fieldType = fieldType;
		this.selectAlias = selectAlias;
		this.alias = alias;
		this.forceNewJoin = forceNewJoin;
	}

	public void flagAsApplied() {
		this.applied = true;
	}

	public ModelAssociationFieldType getFieldType() {
		return fieldType;
	}

	public String getSelectAlias() {
		return selectAlias;
	}

	public String getAlias() {
		return alias;
	}

	public boolean isForceNewJoin() {
		return forceNewJoin;
	}

	public boolean isApplied() {
		return applied;
	}

}