package org.bocogop.shared.service.scheduledJobs;

import java.util.SortedSet;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CacheAutoRefresher {

	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private PrecinctDAO precinctDAO;

	@Scheduled(initialDelayString = "${cache.autoRefresh.startupDelayMillis}", //
			fixedDelayString = "${cache.autoRefresh.fixedDelayMillis}")
	@Transactional(readOnly = true)
	public void refresh() {
		SortedSet<Precinct> allPrecincts = precinctDAO.findAllSorted();
		for (Precinct f : allPrecincts) {
			// reattach if cached
			f = precinctDAO.findRequiredByPrimaryKey(f.getId());
		}

		roleDAO.findAllSorted(true);
	}

}
