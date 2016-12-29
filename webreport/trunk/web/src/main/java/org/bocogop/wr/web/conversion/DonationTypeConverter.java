package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.donation.DonationType;
import org.bocogop.wr.persistence.dao.lookup.DonationTypeDAO;

@Component
public class DonationTypeConverter extends AbstractStringToPersistentConverter<DonationType> {

	@Autowired
	public DonationTypeConverter(DonationTypeDAO dao) {
		super(dao);
	}
}
