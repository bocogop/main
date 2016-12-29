package org.bocogop.wr.persistence.impl.lookup;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.StdCreditCardType;
import org.bocogop.wr.persistence.dao.lookup.StdCreditCardTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class StdCreditCardTypeDAOImpl extends GenericHibernateLookupDAOImpl<StdCreditCardType>
		implements StdCreditCardTypeDAO {

}
