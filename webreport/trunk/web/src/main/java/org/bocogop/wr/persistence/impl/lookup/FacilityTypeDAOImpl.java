package org.bocogop.wr.persistence.impl.lookup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.util.LookupUtil;
import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.persistence.dao.lookup.FacilityTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;

@Repository
public class FacilityTypeDAOImpl extends GenericHibernateDAOImpl<FacilityType> implements FacilityTypeDAO {

	@Override
	public Map<FacilityTypeValue, FacilityType> findByLookups(FacilityTypeValue... lookups) {
		if (ArrayUtils.isEmpty(lookups))
			throw new IllegalArgumentException("No lookups specified");

		@SuppressWarnings("unchecked")
		List<FacilityType> results = query("from " + FacilityType.class.getName() + " where id in (:ids)")
				.setParameter("ids", LookupUtil.translateTypesToIDs(lookups)).getResultList();

		Map<FacilityTypeValue, FacilityType> resultMap = new LinkedHashMap<>();
		for (FacilityType r : results) {
			resultMap.put(FacilityTypeValue.getById(r.getId()), r);
		}
		return resultMap;
	}

	@Override
	public FacilityType findByLookup(FacilityTypeValue lookup) {
		return findByLookups(lookup).get(lookup);
	}

}
