package org.bocogop.wr.persistence.impl.precinct;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.AbstractSortedDAOTest;

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
