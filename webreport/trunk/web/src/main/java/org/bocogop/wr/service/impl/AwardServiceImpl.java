package org.bocogop.wr.service.impl;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.service.AwardService;

@Service
public class AwardServiceImpl extends AbstractServiceImpl implements AwardService {
	private static final Logger log = LoggerFactory.getLogger(AwardServiceImpl.class);

	@Override
	public Award saveOrUpdate(Award award) {
		return awardCodeDAO.saveOrUpdate(award);
	}

	@Override
	public void saveMultipleVolunteers(Map<Long, Long> volIdToAwardMap, LocalDate awardDate) {
		Map<Long, Volunteer> vols = volunteerDAO.findByPrimaryKeys(volIdToAwardMap.keySet());
		Map<Long, Award> awards = awardDAO.findByPrimaryKeys(volIdToAwardMap.values());

		for (Long volId : volIdToAwardMap.keySet()) {
			Volunteer vol = vols.get(volId);
			Long awardId = volIdToAwardMap.get(volId);
			Award award = awards.get(awardId);
			vol.setLastAward(award);
			vol.setLastAwardHours(award.getAwardHours());
			vol.setLastAwardDate(awardDate);
			vol = volunteerDAO.saveOrUpdate(vol);
		}
	}
}
