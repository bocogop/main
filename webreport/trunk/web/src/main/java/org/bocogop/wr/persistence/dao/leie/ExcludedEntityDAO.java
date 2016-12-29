package org.bocogop.wr.persistence.dao.leie;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface ExcludedEntityDAO extends CustomizableSortedDAO<ExcludedEntity> {

	public static interface ImportDataCallback {
		void processRecord(ExcludedEntity e);
	}

	int importData(ImportDataCallback callback) throws ClientProtocolException, IOException;

	List<ExcludedEntity> findByCriteria(String searchValue, int start, int length, String orderBy);

	int[] getTotalAndFilteredNumber(String searchValue);

	List<ExcludedEntityMatch> findExcludedEntitiesForFacilities(Collection<Long> vaFacilityIds);

	List<ExcludedEntityMatch> findExcludedEntitiesForVolunteer(long volunteerId, LocalDate exclusionDateNewerThan);

	List<ExcludedEntity> findExcludedEntitiesForVolunteerInfo(String lastName, String firstName, LocalDate dateOfBirth, LocalDate exclusionDateGreaterThan);

	List<ExcludedEntityMatch> findNewVolunteerMatches();

}
