package org.bocogop.wr.persistence.dao.voter.demographics;

import java.util.List;

import org.bocogop.wr.model.voter.VoterDemographics;
import org.bocogop.wr.persistence.AppDAO;

public interface VolDemoDAO extends AppDAO<VoterDemographics> {

	List<VoterDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length);

	int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams);
}
