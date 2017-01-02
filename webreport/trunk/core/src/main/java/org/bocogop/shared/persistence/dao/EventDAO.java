package org.bocogop.shared.persistence.dao;

import java.util.List;

import org.bocogop.shared.model.Event;

public interface EventDAO extends CustomizableSortedDAO<Event> {

	List<Event> findByCriteria(Boolean registrationStatus);

}
