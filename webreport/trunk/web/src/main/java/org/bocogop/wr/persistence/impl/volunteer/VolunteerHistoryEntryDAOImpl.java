package org.bocogop.wr.persistence.impl.volunteer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.VolunteerHistoryEntry;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerHistoryEntryDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class VolunteerHistoryEntryDAOImpl extends GenericHibernateSortedDAOImpl<VolunteerHistoryEntry>
		implements VolunteerHistoryEntryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VolunteerHistoryEntryDAOImpl.class);

	@Override
	public Map<Integer, VolunteerHistoryEntry> findByVersions(long volunteerId, int[] versions) {
		if (versions.length == 0)
			return new HashMap<>();

		@SuppressWarnings("unchecked")
		List<VolunteerHistoryEntry> queryResults = query("select h from " + VolunteerHistoryEntry.class.getName()
				+ " h where h.volunteerId = :volunteerId and h.version in (:vers)")
						.setParameter("volunteerId", volunteerId)
						.setParameter("vers", Arrays.asList(ArrayUtils.toObject(versions))).getResultList();

		Map<Integer, VolunteerHistoryEntry> results = queryResults.stream()
				.collect(Collectors.toMap(VolunteerHistoryEntry::getVersion, Function.identity(), (p, q) -> q));
		return results;
	}

}
