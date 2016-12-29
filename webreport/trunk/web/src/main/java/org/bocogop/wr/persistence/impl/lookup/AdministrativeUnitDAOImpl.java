package org.bocogop.wr.persistence.impl.lookup;

import java.util.List;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.lookup.AdministrativeUnitDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class AdministrativeUnitDAOImpl extends GenericHibernateSortedDAOImpl<AdministrativeUnit>
		implements AdministrativeUnitDAO {

	public Facility findByStationNumber(String stationNumber) {
		@SuppressWarnings("unchecked")
		List<Facility> results = query(
				"from " + Facility.class.getName() + " where stationNumber = :stationNumber")
						.setParameter("stationNumber", stationNumber).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public AdministrativeUnit findBySDSInstitution(long vaFacilityId) {
		@SuppressWarnings("unchecked")
		List<AdministrativeUnit> results = query(
				"from " + AdministrativeUnit.class.getName() + " where visnFacility.id = :vaFacilityId")
						.setParameter("vaFacilityId", vaFacilityId).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
