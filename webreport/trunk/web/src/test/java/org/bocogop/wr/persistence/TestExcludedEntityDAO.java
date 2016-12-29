package org.bocogop.wr.persistence;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;

public class TestExcludedEntityDAO extends AbstractTransactionalWebDAOTest<ExcludedEntity> {

	@Autowired
	private ExcludedEntityDAO dao;

	@Override
	protected ExcludedEntity getInstanceToSave() {
		ExcludedEntity e = new ExcludedEntity();
		return e;
	}

	@Test
	public void testFindByCriteria() {
		dao.findExcludedEntitiesForFacilities(Arrays.asList(218L));
		dao.findExcludedEntitiesForFacilities(null);
		dao.findExcludedEntitiesForVolunteer(volunteerDAO.findSome(1).get(0).getId(), null);
	}
	
	@Test
	public void testFindNewVolunteerMatches() {
		dao.findNewVolunteerMatches();
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

	@Override
	protected AppDAO<ExcludedEntity> getDAO() {
		return dao;
	}

}
