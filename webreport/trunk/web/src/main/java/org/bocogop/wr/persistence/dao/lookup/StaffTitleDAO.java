package org.bocogop.wr.persistence.dao.lookup;


import java.util.SortedSet;

import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;


public interface StaffTitleDAO extends CustomizableSortedDAO<StaffTitle> {

	public SortedSet<StaffTitle> findAllActiveSorted();
}

