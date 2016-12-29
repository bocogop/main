package org.bocogop.wr.persistence.dao.volunteer;

import java.util.Map;

import org.bocogop.wr.model.volunteer.VolunteerHistoryEntry;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface VolunteerHistoryEntryDAO extends CustomizableSortedDAO<VolunteerHistoryEntry> {

	Map<Integer, VolunteerHistoryEntry> findByVersions(long volunteerId, int[] versions);

}
