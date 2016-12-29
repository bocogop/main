package org.bocogop.wr.persistence.dao.lookup;

import java.util.Map;

import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface DonorTypeDAO extends CustomizableAppDAO<DonorType> {

	DonorType findByLookup(DonorTypeValue lookup);
	
	Map<DonorTypeValue, DonorType> findByLookups(DonorTypeValue... lookups);

}
