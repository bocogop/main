package org.bocogop.wr.persistence.impl.facility;

import java.time.ZoneId;

import org.junit.Test;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.persistence.AbstractSortedDAOTest;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public class TestFacilityDAO extends AbstractSortedDAOTest<Facility> {

	@Override
	protected CustomizableSortedDAO<Facility> getSortedDAO() {
		return facilityDAO;
	}

	@Override
	protected Facility getInstanceToSave() throws Exception {
		VAFacility facility = createNewVAFacility("999_UT");
		State state = stateDAO.findStateByPostalCode("CO");
		FacilityType type = facilityTypeDAO.findByLookup(FacilityTypeValue.TIMEKEEPING);
		
		Facility newInstitution = Facility.createNew(facility, state, type, null, ZoneId.of("US/Central"));
		return newInstitution;
	}

	@Test
	public void testFindBySDSFacility() throws Exception {
		facilityDAO.findByVAFacility(getVAFacility().getId());
	}

}
