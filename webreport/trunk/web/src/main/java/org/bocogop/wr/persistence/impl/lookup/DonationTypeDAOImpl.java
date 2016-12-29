package org.bocogop.wr.persistence.impl.lookup;

import java.util.List;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationType;
import org.bocogop.wr.model.donation.DonationType.DonationTypeValue;
import org.bocogop.wr.persistence.dao.lookup.DonationTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class DonationTypeDAOImpl extends GenericHibernateSortedDAOImpl<DonationType>
		implements DonationTypeDAO {

	
	 public DonationType findByLookup(DonationTypeValue lookup) {
	 
		@SuppressWarnings("unchecked")
		List<DonationType> results = query("from " + DonationType.class.getName() + " where id = :id")
				.setParameter("id", lookup.getId()).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
