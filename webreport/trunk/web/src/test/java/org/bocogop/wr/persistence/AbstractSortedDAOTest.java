package org.bocogop.wr.persistence;

import org.bocogop.shared.model.AuditedPersistent;
import org.bocogop.shared.persistence.dao.CustomizableAppDAO;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
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
