package org.bocogop.shared.persistence;

import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.AbstractTransactionalCoreDAOTest;
import org.bocogop.shared.model.lookup.sds.Gender;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;

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
