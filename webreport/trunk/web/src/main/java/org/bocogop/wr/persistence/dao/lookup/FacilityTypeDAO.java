package org.bocogop.wr.persistence.dao.lookup;

import java.util.Map;

import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface FacilityTypeDAO extends CustomizableAppDAO<FacilityType> {

	Map<FacilityTypeValue, FacilityType> findByLookups(FacilityTypeValue... lookup);

	FacilityType findByLookup(FacilityTypeValue lookup);

}
