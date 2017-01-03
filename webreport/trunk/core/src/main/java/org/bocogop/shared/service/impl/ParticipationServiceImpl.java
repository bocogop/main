package org.bocogop.shared.service.impl;

import org.bocogop.shared.model.Participation;
import org.bocogop.shared.service.ParticipationService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParticipationServiceImpl extends AbstractServiceImpl implements ParticipationService {
	private static final Logger log = LoggerFactory.getLogger(ParticipationServiceImpl.class);

	@Override
	public Participation saveOrUpdate(Participation p) throws ServiceValidationException {
		p = participationDAO.saveOrUpdate(p);
		return p;
	}

	@Override
	public void delete(long eventId) {
		eventDAO.delete(eventId);
	}

	@Override
	public void logParticipation(long voterId, long eventId) {
		participationDAO.logParticipation(voterId, eventId);
	}

}
