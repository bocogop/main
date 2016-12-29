package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.persistence.dao.DonationReferenceDAO;

@Component
public class DonationReferenceConverter extends AbstractStringToPersistentConverter<DonationReference> {

	@Autowired
	protected DonationReferenceConverter(DonationReferenceDAO dao) {
		super(dao);
	}
}
