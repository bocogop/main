package org.bocogop.wr.service.scheduledJobs;

import java.util.SortedSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.GrantableRoleDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;

@Component
public class CacheAutoRefresher {

	@Autowired
	private GrantableRoleDAO grantableRoleDAO;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private StateDAO stateDAO;
	@Autowired
	private VAFacilityDAO vaFacilityDAO;
	@Autowired
	private FacilityDAO facilityDAO;

	@Scheduled(initialDelayString = "${cache.autoRefresh.startupDelayMillis}", //
			fixedDelayString = "${cache.autoRefresh.fixedDelayMillis}")
	@Transactional(readOnly = true)
	public void refresh() {
		// ensure this is cached via Spring as well as the State objects
		SortedSet<State> allStates = stateDAO.findAllSorted();
		for (State s : allStates) {
			s.getName();
		}

		SortedSet<VAFacility> all = vaFacilityDAO.findAllSorted();
		for (VAFacility f : all) {
			f.getStationNumber();
			f.getCity();
		}
		vaFacilityDAO.findAllThreeDigitStationsSorted();
		vaFacilityDAO.findAllActiveTreatingFacilities();

		SortedSet<Facility> allFacilities = facilityDAO.findAllSorted();
		for (Facility f : allFacilities) {
			// reattach if cached
			f = facilityDAO.findRequiredByPrimaryKey(f.getId());
			
			f.getStationNumber();
			State s = f.getState();
			if (s != null)
				s.getDisplayName();

			VAFacility vaFacility = f.getVaFacility();
			if (vaFacility != null)
				vaFacility.getDisplayName();
		}

		SortedSet<Role> allRoles = roleDAO.findAllSorted(true);
		for (Role r : allRoles) {
			grantableRoleDAO.findGrantableRolesForRole(r.getId());
		}

	}

}
