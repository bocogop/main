package org.bocogop.wr.persistence.dao.views;

import java.util.SortedSet;

import org.bocogop.wr.model.views.CombinedFacility;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface CombinedFacilityDAO extends CustomizableSortedDAO<CombinedFacility> {

	SortedSet<CombinedFacility> findActiveWithLinkToVAFacility();
	
}
