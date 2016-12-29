package org.bocogop.wr.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.service.AwardCodeService;

@Service
public class AwardCodeServiceImpl extends AbstractServiceImpl implements AwardCodeService {
	private static final Logger log = LoggerFactory.getLogger(AwardCodeServiceImpl.class);

	@Override
	public Award saveOrUpdate(Long awardCodeId, Award awardCode) throws ServiceValidationException {
	
		Award onFileAward = null;
		if (awardCodeId != null)
			onFileAward = awardCodeDAO.findRequiredByPrimaryKey(awardCodeId);
		
		List<Award> potentialDuplicates = null;
		
		// prevent duplicate - name
		if (awardCodeId == null || (!onFileAward.getName().equals(awardCode.getName()))) {
			potentialDuplicates = awardCodeDAO.findByCriteria(awardCode.getName(), null);
	
			if (!potentialDuplicates.isEmpty())
				throw new ServiceValidationException("awardCode.error.duplicateName",
						new Serializable[] { potentialDuplicates.get(0).getName() });
		}
		
		// prevent duplicate - code
		if (awardCodeId == null || (!onFileAward.getCode().equals(awardCode.getCode()))) {
			potentialDuplicates = awardCodeDAO.findByCriteria(null, awardCode.getCode());
	
			if (!potentialDuplicates.isEmpty())
				throw new ServiceValidationException("awardCode.error.duplicateCode",
						new Serializable[] { potentialDuplicates.get(0).getCode() });
		}
		
		if (awardCodeId != null) {
			onFileAward.setCode(awardCode.getCode());
			onFileAward.setName(awardCode.getName());
			onFileAward.setHoursRequired(awardCode.getHoursRequired());
			onFileAward.setType(awardCode.getType());
			onFileAward.setAwardHours(awardCode.getAwardHours());
			onFileAward.setInactive(awardCode.isInactive());
			awardCode = onFileAward;
		}
		
		return awardCodeDAO.saveOrUpdate(awardCode);
	}
	
	@Override
	public void delete(long awardCodeId) throws ServiceValidationException {
		
		// An award code can be deleted if it has not been utilized.
		List<Volunteer> linkedVolunteer = volunteerDAO.findByAwardCode(awardCodeId);
		if (!linkedVolunteer.isEmpty())
			throw new ServiceValidationException("awardCode.error.awardCodeUsed");

		awardCodeDAO.delete(awardCodeId);
	}

}
