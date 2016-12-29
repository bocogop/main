package org.bocogop.wr.persistence.impl.lookup;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import org.bocogop.shared.util.LookupUtil;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;

@Repository
public class DonorTypeDAOImpl extends GenericHibernateDAOImpl<DonorType> implements DonorTypeDAO {

	@Override
	public DonorType findByLookup(DonorTypeValue lookup) {
		@SuppressWarnings("unchecked")
		List<DonorType> results = query("from " + DonorType.class.getName() + " where id = :id")
				.setParameter("id", lookup.getId()).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public Map<DonorTypeValue, DonorType> findByLookups(DonorTypeValue... lookups) {
		Map<DonorTypeValue, DonorType> results = new EnumMap<DonorTypeValue, DonorType>(DonorTypeValue.class);
		if (lookups == null)
			return results;

		for (DonorTypeValue v : lookups)
			results.put(v, null);

		@SuppressWarnings("unchecked")
		List<DonorType> r = query("from " + DonorType.class.getName() + " where id in (:ids)")
				.setParameter("ids", LookupUtil.translateTypesToIDs(lookups)).getResultList();
		for (DonorType dt : r)
			results.put(dt.getLookupType(), dt);

		return results;
	}

}
