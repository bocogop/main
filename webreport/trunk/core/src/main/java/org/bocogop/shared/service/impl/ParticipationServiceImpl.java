package org.bocogop.shared.service.impl;

import java.util.List;

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
		List<Participation> existing = participationDAO.findByCriteria(p.getVoter().getId(), p.getEvent().getId());
		if (!existing.isEmpty())
			throw new ServiceValidationException("participation.create.error.alreadyExists");

		p = participationDAO.saveOrUpdate(p);
		return p;
	}

	@Override
	public void delete(long participationId) {
		participationDAO.delete(participationId);
	}

	@Override
	public void logParticipation(long voterId, long eventId) {
		participationDAO.logParticipation(voterId, eventId);
	}

}
