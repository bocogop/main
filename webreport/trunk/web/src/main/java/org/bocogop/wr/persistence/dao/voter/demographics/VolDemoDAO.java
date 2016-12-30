package org.bocogop.wr.persistence.dao.voter.demographics;

import java.util.List;

import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.wr.model.voter.VoterDemographics;

public interface VolDemoDAO extends AppDAO<VoterDemographics> {

	List<VoterDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length);

	int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams);
}
