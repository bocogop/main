package org.bocogop.shared.persistence.dao.voter.demographics;

import java.util.List;

import org.bocogop.shared.model.voter.VoterDemographics;
import org.bocogop.shared.persistence.AppDAO;

public interface VolDemoDAO extends AppDAO<VoterDemographics> {

	List<VoterDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length);

	int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams);
}
