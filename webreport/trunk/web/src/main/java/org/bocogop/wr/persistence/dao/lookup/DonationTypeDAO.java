package org.bocogop.wr.persistence.dao.lookup;

import org.bocogop.wr.model.donation.DonationType;
import org.bocogop.wr.model.donation.DonationType.DonationTypeValue;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface DonationTypeDAO extends CustomizableSortedDAO<DonationType> {

	DonationType findByLookup(DonationTypeValue lookup);

}
