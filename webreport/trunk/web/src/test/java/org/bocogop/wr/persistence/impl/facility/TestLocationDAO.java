package org.bocogop.wr.persistence.impl.facility;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.persistence.AbstractSortedDAOTest;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public class TestLocationDAO extends AbstractSortedDAOTest<Location> {

	@Override
	protected CustomizableSortedDAO<Location> getSortedDAO() {
		return locationDAO;
	}

	@Override
	protected Location getInstanceToSave() throws Exception {
		VAFacility vaFacility = createNewVAFacility("999_UT");
		State state = stateDAO.findStateByPostalCode("CO");
		FacilityType type = facilityTypeDAO.findByLookup(FacilityTypeValue.TIMEKEEPING);
		Facility facility = Facility.createNew(vaFacility, state, type, null, ZoneId.of("US/Central"));
		Location l = new Location();
		l.setParent(facility);
		l.setName("unitTestLocation");
		return l;
	}

	@Test
	public void testCountVolunteersForLocations() throws Exception {
		VAFacility vaFacility = createNewVAFacility("999_UT");
		State state = stateDAO.findStateByPostalCode("CO");
		FacilityType type = facilityTypeDAO.findByLookup(FacilityTypeValue.TIMEKEEPING);
		Facility facility = Facility.createNew(vaFacility, state, type, null, ZoneId.of("US/Central"));
		facility = facilityDAO.saveOrUpdate(facility);
		
		Location l = new Location();
		l.setParent(facility);
		l.setName("unitTestLocation");
		saveAndFlush(l);
		
		l = new Location();
		l.setParent(facility);
		l.setName("unitTestLocation2");
		saveAndFlush(l);
		
		Set<Long> locationIds = locationDAO.findSome(2).stream().map(p -> p.getId()).collect(Collectors.toSet());
		Map<Long, Integer[]> counts = locationDAO.countVolunteersForLocations(locationIds);
		System.out.println(Arrays.toString(counts.values().iterator().next()));
	}

}
