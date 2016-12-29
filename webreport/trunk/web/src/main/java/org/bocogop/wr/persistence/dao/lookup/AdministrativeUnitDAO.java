package org.bocogop.wr.persistence.dao.lookup;

import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface AdministrativeUnitDAO extends CustomizableSortedDAO<AdministrativeUnit> {

	AdministrativeUnit findBySDSInstitution(long vaFacilityId);

}
