package org.bocogop.wr.persistence.impl;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.ShirtSize;
import org.bocogop.wr.model.volunteer.Uniform;
import org.bocogop.wr.persistence.dao.UniformDAO;

@Repository
public class UniformDAOImpl extends GenericHibernateSortedDAOImpl<Uniform> implements UniformDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UniformDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<ShirtSize> findAllShirtSizes() {
		List<ShirtSize> sizes = query("from " + ShirtSize.class.getName()).getResultList();
		return new TreeSet<>(sizes);
	}

}
