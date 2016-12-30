package org.bocogop.wr.service.scheduledJobs;

import java.util.SortedSet;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CacheAutoRefresher {

	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private StateDAO stateDAO;
	@Autowired
	private PrecinctDAO precinctDAO;

	@Scheduled(initialDelayString = "${cache.autoRefresh.startupDelayMillis}", //
			fixedDelayString = "${cache.autoRefresh.fixedDelayMillis}")
	@Transactional(readOnly = true)
	public void refresh() {
		// ensure this is cached via Spring as well as the State objects
		SortedSet<State> allStates = stateDAO.findAllSorted();
		for (State s : allStates) {
			s.getName();
		}

		SortedSet<Precinct> allPrecincts = precinctDAO.findAllSorted();
		for (Precinct f : allPrecincts) {
			// reattach if cached
			f = precinctDAO.findRequiredByPrimaryKey(f.getId());
		}

		roleDAO.findAllSorted(true);
	}

}
