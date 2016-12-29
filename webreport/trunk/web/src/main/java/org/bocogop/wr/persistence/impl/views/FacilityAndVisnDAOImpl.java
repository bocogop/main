package org.bocogop.wr.persistence.impl.views;

import javax.persistence.NoResultException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.views.FacilityAndVisn;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.util.cache.CacheNames;

@Repository
public class FacilityAndVisnDAOImpl extends AbstractDerivedPersistentDAOImpl implements FacilityAndVisnDAO {

	@Override
	@Cacheable(value = CacheNames.QUERIES_FACILITY_AND_VISN_DAO)
	public FacilityAndVisn getForFacilityId(long facilityId) {
		try {
			return (FacilityAndVisn) query("from " + FacilityAndVisn.class.getName() + " i where i.id = :facilityId")
					.setParameter("facilityId", facilityId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
