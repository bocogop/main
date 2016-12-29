package org.bocogop.wr.persistence.impl.lookup;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.persistence.dao.lookup.StaffTitleDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class StaffTitleDAOImpl extends GenericHibernateSortedDAOImpl<StaffTitle> implements StaffTitleDAO {
	
	@Override
	@SuppressWarnings("unchecked")
	public SortedSet<StaffTitle> findAllActiveSorted() {
		return new TreeSet<>(
				query("from " + StaffTitle.class.getName() + " s where s.inactive IS FALSE").getResultList());
	}
}
