package org.bocogop.shared.persistence.criteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.util.LookupUtil;

public class LookupCriteria<T extends LookupType> {

	static enum LookupCriteriaType {
		MATCHES_EXACTLY, MATCHES_ANY_OF, IS_NULL;
	}

	private LookupCriteriaType type;
	private Collection<T> vals;

	private LookupCriteria(LookupCriteriaType type, Collection<T> vals) {
		this.type = type;
		this.vals = vals;
	}

	public void append(List<String> whereClauseItems, Map<String, Object> params, String lookupIdPath) {
		String alias = "lookupCriteria" + lookupIdPath.replaceAll("\\W", "");
		if (type == LookupCriteriaType.MATCHES_EXACTLY) {
			whereClauseItems.add(lookupIdPath + " = :" + alias);
			params.put(alias, vals.iterator().next().getId());
		} else if (type == LookupCriteriaType.MATCHES_ANY_OF) {
			List<Long> codes = LookupUtil.translateTypesToIDs(vals);
			whereClauseItems.add(lookupIdPath + " in (:" + alias + ")");
			params.put(alias, codes);
		}
	}

	public Collection<T> getVals() {
		return vals;
	}

	public static <T extends AbstractLookup<T, U>, U extends LookupType> LookupCriteria<U> is(AbstractLookup<T, U> l) {
		return (LookupCriteria<U>) is(l.getLookupType());
	}

	public static <T extends LookupType> LookupCriteria<T> is(T type) {
		/*
		 * Do not confuse this with requiring that the lookup type be null; this
		 * returns null to signify there is no lookup criteria - CPB
		 */
		if (type == null)
			return null;
		return new LookupCriteria<T>(LookupCriteriaType.MATCHES_EXACTLY, Arrays.asList(type));
	}

	public static <T extends LookupType> LookupCriteria<T> anyOf(@SuppressWarnings("unchecked") T... types) {
		/*
		 * Do not confuse this with requiring that the lookup type be null; this
		 * returns null to signify there is no lookup criteria - CPB
		 */
		return anyOf(Arrays.asList(types));
	}

	public static <T extends LookupType> LookupCriteria<T> anyOf(Collection<T> types) {
		/*
		 * Do not confuse this with requiring that the lookup type be null; this
		 * returns null to signify there is no lookup criteria - CPB
		 */
		if (types == null)
			return null;
		return new LookupCriteria<T>(LookupCriteriaType.MATCHES_ANY_OF, types);
	}

}
