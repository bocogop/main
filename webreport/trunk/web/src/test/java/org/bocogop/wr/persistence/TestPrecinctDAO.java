package org.bocogop.wr.persistence;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.wr.AbstractTransactionalCoreDAOTest;
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
