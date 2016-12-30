package org.bocogop.wr.persistence;

import org.bocogop.wr.model.lookup.AbstractLookup;
import org.bocogop.wr.model.lookup.LookupType;
import org.bocogop.wr.persistence.dao.CustomizableLookupDAO;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractLookupDAOTest<T extends AbstractLookup<T, ?>> extends AbstractSortedDAOTest<T> {

	/**
	 * Subclasses must override this method to return the DAO under test.
	 * 
	 * @return
	 */
	protected abstract CustomizableLookupDAO<T> getLookupDAO();

	protected final CustomizableSortedDAO<T> getSortedDAO() {
		return getLookupDAO();
	}

	protected abstract LookupType getExampleLookupType();

	@Test
	public void testFindByLookup() {
		getLookupDAO().findByLookup(getExampleLookupType());
	}

	@Test
	public void testFindById() {
		T valid = getLookupDAO().findByPrimaryKey(getExampleLookupType().getId());
		Assert.assertNotNull(valid);
		T invalid = getLookupDAO().findByPrimaryKey(0L);
		Assert.assertNull(invalid);
	}

	@Test
	public void testFindRequiredByCode() {
		getLookupDAO().findRequiredByPrimaryKey(getExampleLookupType().getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindRequiredByInvalidCode() {
		getLookupDAO().findRequiredByPrimaryKey(0L);
	}

	@Test
	public void testFindAllSortedActive() {
		getLookupDAO().findAllSorted(true);
		getLookupDAO().findAllSorted(false);
		getLookupDAO().findAllSorted((Boolean) null);
	}

	@Test
	public void testFindAllSortedActiveWithCustomization() {
		getLookupDAO().findAllSorted(true, new QueryCustomization(0, 1));
		getLookupDAO().findAllSorted(false, new QueryCustomization(0, 1));
		getLookupDAO().findAllSorted(null, new QueryCustomization(0, 1));
	}

	@Test
	public void testFindAllActiveWithCustomization() {
		getLookupDAO().findAll(true, new QueryCustomization(0, 1));
		getLookupDAO().findAll(false, new QueryCustomization(0, 1));
		getLookupDAO().findAll(null, new QueryCustomization(0, 1));
	}

	@Test
	public void testFindAllActive() {
		getLookupDAO().findAll(true);
		getLookupDAO().findAll(false);
		getLookupDAO().findAll((Boolean) null);
	}

}
