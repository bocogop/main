package org.bocogop.wr.persistence;

import java.time.LocalDate;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.mealTicket.MealTicket;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.MealTicketDAO;

public class TestMealTicketDAO extends AbstractTransactionalWebDAOTest<MealTicket> {

	@Autowired
	private MealTicketDAO mealTicketDAO;

	@Override
	protected CustomizableAppDAO<MealTicket> getDAO() {
		return mealTicketDAO;
	}

	@Test
	public void testFindByCriteria() {
		Long facilityId = new Long(218L);
		// test list meal ticket by facility and date
		mealTicketDAO.findByCriteria(facilityId, null, LocalDate.now());
		// Assert.assertFalse("Failed search for Meal Ticket - ",
		// mealTicketList.isEmpty());
	}

	@Override
	protected MealTicket getInstanceToSave() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
