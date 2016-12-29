package org.bocogop.wr.persistence.dao;

import java.util.List;
import java.util.Map;

import org.bocogop.wr.model.letterTemplate.LetterTemplate;
import org.bocogop.wr.model.letterTemplate.LetterType;

public interface LetterTemplateDAO extends CustomizableSortedDAO<LetterTemplate> {

	List<LetterTemplate> findByCriteria(LetterType type, Long facilityId, String stationNumber);

	Map<LetterType, LetterTemplate> findByFacilityId(long facilityId);

	Map<LetterType, LetterTemplate> findByStationNumber(String stationNumber);

}
