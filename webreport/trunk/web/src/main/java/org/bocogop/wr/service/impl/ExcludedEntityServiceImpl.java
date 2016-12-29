package org.bocogop.wr.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO.ImportDataCallback;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityMatch;
import org.bocogop.wr.service.ExcludedEntityService;
import org.bocogop.wr.service.volunteer.VolunteerService;
import org.bocogop.wr.service.volunteer.VolunteerService.LEIETerminationParams;

@Service
public class ExcludedEntityServiceImpl extends AbstractServiceImpl implements ExcludedEntityService {
	private static final Logger log = LoggerFactory.getLogger(ExcludedEntityServiceImpl.class);

	@Autowired
	private ExcludedEntityDAO excludedEntityDAO;
	@Autowired
	private ExcludedEntityServiceHelper helper;
	@Autowired
	private VolunteerService volunteerService;

	@Override
	public int refreshDataAndUpdateVolunteers() throws IOException {
		synchronized (ExcludedEntityServiceImpl.class) {
			int numResults = refreshData();
			updateVolunteers();
			return numResults;
		}
	}

	private int refreshData() throws ClientProtocolException, IOException {
		List<ExcludedEntity> all = excludedEntityDAO.findAll();
		HashSet<ExcludedEntity> existing = new HashSet<>(all);

		final HashSet<ExcludedEntity> newVals = new HashSet<>();
		int numResults = excludedEntityDAO.importData(new ImportDataCallback() {
			@Override
			public void processRecord(ExcludedEntity e) {
				newVals.add(e);
			}
		});

		SetView<ExcludedEntity> itemsToRemove = Sets.difference(existing, newVals);
		SetView<ExcludedEntity> itemsToAdd = Sets.difference(newVals, existing);
		boolean changed = false;

		for (List<ExcludedEntity> batch : Lists.partition(new ArrayList<>(itemsToAdd), 50)) {
			changed = true;
			helper.addValues(batch);
		}

		for (List<ExcludedEntity> batch : Lists.partition(new ArrayList<>(itemsToRemove), 50)) {
			changed = true;
			helper.deleteValues(batch);
		}

		helper.updateExecutedDate();
		if (changed)
			helper.updateDataChangedDate();
		return numResults;
	}

	@Override
	public void updateVolunteers() {
		synchronized (ExcludedEntityServiceImpl.class) {
			List<ExcludedEntityMatch> newMatches = excludedEntityDAO.findNewVolunteerMatches();

			for (ExcludedEntityMatch m : newMatches) {
				try {
					Volunteer v = m.getVolunteer();
					v = volunteerService.terminateVolunteerForLEIEMatch(m, new LEIETerminationParams(
							"Volunteer terminated due to LEIE match",
							"The volunteer \"" + v.getDisplayName() + "\" was terminated due to a new LEIE match.",
							"Volunteer terminated due to LEIE match",
							"The volunteer \"" + v.getDisplayName() + "\" was terminated due to a new LEIE match."));
					log.debug("Volunteer {} terminated due to LEIE match", v.getDisplayName());
				} catch (Exception e1) {
					log.error("Couldn't terminate volunteer after LEIE match", e1);
				}

			}
		}
	}

}
