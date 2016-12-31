package org.bocogop.wr.persistence;

import org.bocogop.wr.AbstractTransactionalCoreDAOTest;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.AppDAO;
import org.junit.Test;

public class TestPrecinctDAO extends AbstractTransactionalCoreDAOTest<Precinct> {

	@Override
	protected Precinct getInstanceToSave() {
		return null;
	}

	@Override
	protected AppDAO<Precinct> getDAO() {
		return precinctDAO;
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

	@Test
	public void testFindAllSorted() {
		precinctDAO.findAllSorted();
	}

}
