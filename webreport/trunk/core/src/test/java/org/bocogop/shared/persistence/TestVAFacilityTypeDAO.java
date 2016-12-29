package org.bocogop.shared.persistence;

import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.AbstractTransactionalCoreDAOTest;
import org.bocogop.shared.model.lookup.sds.VAFacilityType;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityTypeDAO;

public class TestVAFacilityTypeDAO extends AbstractTransactionalCoreDAOTest<VAFacilityType> {

	@Autowired
	private VAFacilityTypeDAO vaFacilityTypeDAO;

	@Override
	protected VAFacilityType getInstanceToSave() {
		return null;
	}

	@Override
	protected AppDAO<VAFacilityType> getDAO() {
		return vaFacilityTypeDAO;
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

}
