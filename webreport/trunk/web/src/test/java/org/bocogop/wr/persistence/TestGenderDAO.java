package org.bocogop.wr.persistence;

import org.bocogop.shared.model.lookup.Gender;
import org.bocogop.shared.persistence.AppSortedDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.wr.AbstractTransactionalCoreDAOTest;
import org.springframework.beans.factory.annotation.Autowired;

public class TestGenderDAO extends AbstractTransactionalCoreDAOTest<Gender> {

	@Autowired
	private GenderDAO genderDAO;

	@Override
	protected AppSortedDAO<Gender> getDAO() {
		return genderDAO;
	}

	@Override
	protected Gender getInstanceToSave() {
		return null;
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

}
