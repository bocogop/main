package org.bocogop.wr.persistence.dao;

import java.util.SortedSet;

import org.bocogop.wr.model.volunteer.ShirtSize;
import org.bocogop.wr.model.volunteer.Uniform;

public interface UniformDAO extends CustomizableSortedDAO<Uniform> {

	SortedSet<ShirtSize> findAllShirtSizes();

}
