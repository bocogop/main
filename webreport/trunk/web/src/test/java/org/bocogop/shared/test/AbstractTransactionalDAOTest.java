package org.bocogop.shared.test;

import java.util.List;

import org.bocogop.wr.model.core.IdentifiedPersistent;
import org.bocogop.wr.persistence.AppDAO;
import org.bocogop.wr.util.TypeUtil;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTransactionalDAOTest<T extends IdentifiedPersistent> extends AbstractTransactionalAppTest {

	@Test
	public void testFindByPrimaryKey() throws Exception {
		List<T> list = getDAO().findAtMost(1);
		T instance = null;
		if (!list.isEmpty()) {
			instance = list.get(0);
		} else {
			T instanceToSave = getInstanceToSave();
			if (instanceToSave != null) {
				instance = getDAO().saveOrUpdate(instanceToSave);
			} else {
				@SuppressWarnings("unchecked")
				Class<? extends T> type = (Class<? extends T>) TypeUtil.getFirstTypeParameterClass(this);
				Assert.fail("No instances of " + type.getSimpleName() + " in database and no instance to save");
			}
		}
		/*
		 * This should also test findRequiredByPrimaryKey since that just tests
		 * to ensure the value exists
		 */
		if (instance != null)
			getDAO().findByPrimaryKey(instance.getId());
	}

	// @Test
	// public void testFindAll() {
	// getDAO().findAll();
	// }

	@Test
	public void testSaveOrUpdate() throws Exception {
		T instanceToSave = getInstanceToSave();
		if (instanceToSave != null)
			instanceToSave = getDAO().saveOrUpdate(instanceToSave);
	}

	@Test
	public void testDeleteCall() throws Exception {
		if (!testDelete())
			return;

		T instanceToSave = getInstanceToSave();
		if (instanceToSave == null)
			return;

		instanceToSave = getDAO().saveOrUpdate(instanceToSave);

		if (instanceToSave.getId() == null)
			throw new IllegalStateException(
					"Couldn't delete item " + instanceToSave + " from database because it has a null identifier");
		getDAO().delete(instanceToSave.getId());
	}

	@Test
	public void testGetTotalNumber() {
		getDAO().getTotalNumber();
	}

	/**
	 * Subclasses must override this method to return the DAO under test.
	 * 
	 * @return
	 */
	protected abstract AppDAO<T> getDAO();

	/**
	 * Subclasses must override this method with an example instance to save in
	 * the database. If no save should be attempted (such as for SDS tables),
	 * return null.
	 * 
	 * @return
	 * @throws Exception
	 *             If there was any exception while building the example
	 *             instance (such as a problem retrieving data from the DB)
	 */
	protected abstract T getInstanceToSave() throws Exception;

	/**
	 * Override this in subclasses to return false if we don't want to attempt
	 * to delete a row (for cases like the SDS lookup tables)
	 * 
	 * @return
	 */
	protected boolean testDelete() {
		return true;
	}

}
