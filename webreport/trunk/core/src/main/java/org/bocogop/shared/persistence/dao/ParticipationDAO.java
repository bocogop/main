package org.bocogop.shared.persistence.dao;

import java.util.List;

import org.bocogop.shared.model.Participation;

public interface ParticipationDAO extends CustomizableAppDAO<Participation> {

	List<Participation> findByCriteria(Long voterId, Long eventId);

	int logParticipation(long voterId, long eventId);

}
