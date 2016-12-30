package org.bocogop.wr.persistence.dao.voter;

import java.util.Map;

import org.bocogop.wr.model.voter.VoterHistoryEntry;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface VoterHistoryEntryDAO extends CustomizableSortedDAO<VoterHistoryEntry> {

	Map<Integer, VoterHistoryEntry> findByVersions(long voterId, int[] versions);

}
