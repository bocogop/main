package org.bocogop.wr.persistence;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.core.AuditedPersistent;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.junit.Test;

public abstract class AbstractSortedDAOTest<T extends Comparable<? super T> & AuditedPersistent>
		extends AbstractTransactionalWebDAOTest<T> {

	/**
	 * Subclasses must override this method to return the DAO under test.
	 * 
	 * @return
	 */
	protected abstract CustomizableSortedDAO<T> getSortedDAO();

	protected final CustomizableAppDAO<T> getDAO() {
		return getSortedDAO();
	}

	@Test
	public void testFindAllSorted() {
		getSortedDAO().findAllSorted();
	}

	@Test
	public void testFindAllSortedWithCustomization() {
		getSortedDAO().findAllSorted(new QueryCustomization(0, 1));
	}

}
