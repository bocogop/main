package org.bocogop.shared.persistence.dao.voter.demographics;

import java.util.List;

import org.bocogop.shared.model.voter.VoterDemographics;
import org.bocogop.shared.persistence.AppDAO;

public interface VoterDemographicsDAO extends AppDAO<VoterDemographics> {

	List<VoterDemographics> findDemographics(VoterDemographicsSearchParams searchParams, int start, int length, long appUserId);

	int[] findDemographicsTotalAndFilteredNumber(VoterDemographicsSearchParams searchParams, long appUserId);
}
