package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.letterTemplate.LetterTemplate;
import org.bocogop.wr.model.letterTemplate.LetterType;
import org.bocogop.wr.persistence.dao.LetterTemplateDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class LetterTemplateDAOImpl extends GenericHibernateSortedDAOImpl<LetterTemplate> implements LetterTemplateDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LetterTemplateDAOImpl.class);

	@Override
	public List<LetterTemplate> findByCriteria(LetterType type, Long facilityId, String stationNumber) {
		StringBuilder sb = new StringBuilder("select c from ").append(LetterTemplate.class.getName()).append(" c");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (facilityId != null) {
			whereClauseItems.add("c.facility.id = :facilityId");
			params.put("facilityId", facilityId);
		}
		
		if (StringUtils.isNotBlank(stationNumber)) {
			whereClauseItems.add("c.facility.stationNumber = :stationNumber");
			params.put("stationNumber", stationNumber);
		}

		if (type != null) {
			whereClauseItems.add("c.type = :type");
			params.put("type", type);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<LetterTemplate> list = q.getResultList();
		return list;
	}

	@Override
	public Map<LetterType, LetterTemplate> findByFacilityId(long facilityId) {
		List<LetterTemplate> list = findByCriteria(null, facilityId, null);
		Map<LetterType, LetterTemplate> results = new HashMap<>();
		for (LetterTemplate t : list)
			results.put(t.getType(), t);
		return results;
	}

	@Override
	public Map<LetterType, LetterTemplate> findByStationNumber(String stationNumber) {
		List<LetterTemplate> list = findByCriteria(null, null, stationNumber);
		Map<LetterType, LetterTemplate> results = new HashMap<>();
		for (LetterTemplate t : list)
			results.put(t.getType(), t);
		return results;
	}

}
