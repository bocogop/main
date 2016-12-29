package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;

@Component
public class DonorTypeConverter extends AbstractStringToPersistentConverter<DonorType> {

	@Autowired
	public DonorTypeConverter(DonorTypeDAO dao) {
		super(dao);
	}
}
