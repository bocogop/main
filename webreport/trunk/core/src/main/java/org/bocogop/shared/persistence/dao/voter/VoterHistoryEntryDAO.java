package org.bocogop.shared.persistence.dao.voter;

import java.util.Map;

import org.bocogop.shared.model.voter.VoterHistoryEntry;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;

public interface VoterHistoryEntryDAO extends CustomizableSortedDAO<VoterHistoryEntry> {

	Map<Integer, VoterHistoryEntry> findByVersions(long voterId, int[] versions);

}
