package org.bocogop.shared.persistence;

import org.bocogop.shared.AbstractTransactionalCoreDAOTest;
import org.bocogop.wr.model.precinct.Precinct;
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
