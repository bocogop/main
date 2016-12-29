package org.bocogop.wr.persistence.dao.volunteer.demographics;

import java.util.List;

import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.wr.model.volunteer.VolunteerDemographics;

public interface VolDemoDAO extends AppDAO<VolunteerDemographics> {

	List<VolunteerDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length);

	int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams);
}
