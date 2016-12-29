package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.donation.StdCreditCardType;
import org.bocogop.wr.persistence.dao.lookup.StdCreditCardTypeDAO;

@Component
public class StdCreditCardTypeConverter extends AbstractStringToPersistentConverter<StdCreditCardType> {

	@Autowired
	protected StdCreditCardTypeConverter(StdCreditCardTypeDAO dao) {
		super(dao);
	}
}
