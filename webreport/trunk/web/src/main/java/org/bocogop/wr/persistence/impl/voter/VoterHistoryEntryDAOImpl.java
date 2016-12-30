package org.bocogop.wr.persistence.impl.voter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.bocogop.wr.model.voter.VoterHistoryEntry;
import org.bocogop.wr.persistence.dao.voter.VoterHistoryEntryDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class VoterHistoryEntryDAOImpl extends GenericHibernateSortedDAOImpl<VoterHistoryEntry>
		implements VoterHistoryEntryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VoterHistoryEntryDAOImpl.class);

	@Override
	public Map<Integer, VoterHistoryEntry> findByVersions(long voterId, int[] versions) {
		if (versions.length == 0)
			return new HashMap<>();

		@SuppressWarnings("unchecked")
		List<VoterHistoryEntry> queryResults = query("select h from " + VoterHistoryEntry.class.getName()
				+ " h where h.voterId = :voterId and h.version in (:vers)")
						.setParameter("voterId", voterId)
						.setParameter("vers", Arrays.asList(ArrayUtils.toObject(versions))).getResultList();

		Map<Integer, VoterHistoryEntry> results = queryResults.stream()
				.collect(Collectors.toMap(VoterHistoryEntry::getVersion, Function.identity(), (p, q) -> q));
		return results;
	}

}
