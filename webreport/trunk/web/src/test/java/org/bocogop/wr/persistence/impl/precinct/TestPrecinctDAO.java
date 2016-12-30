package org.bocogop.wr.persistence.impl.precinct;

import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.AbstractSortedDAOTest;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public class TestPrecinctDAO extends AbstractSortedDAOTest<Precinct> {

	@Override
	protected CustomizableSortedDAO<Precinct> getSortedDAO() {
		return precinctDAO;
	}

	@Override
	protected Precinct getInstanceToSave() throws Exception {
		Precinct precinct = new Precinct("1234", "1234");
		return precinct;
	}

}
