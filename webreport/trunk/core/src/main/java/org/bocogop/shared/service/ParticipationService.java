package org.bocogop.shared.service;

import org.bocogop.shared.model.Participation;
import org.bocogop.shared.service.validation.ServiceValidationException;

public interface ParticipationService {

	Participation saveOrUpdate(Participation participation) throws ServiceValidationException;

	void delete(long id);
	
	void logParticipation(long voterId, long eventId);

}
