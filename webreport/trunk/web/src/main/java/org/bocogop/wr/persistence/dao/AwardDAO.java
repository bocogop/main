package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.award.AwardResult;

public interface AwardDAO extends CustomizableSortedDAO<Award> {

	List<AwardResult> findPotentialAwards(Long facilityId, boolean includeAdult, boolean includeYouth,
			boolean includeActive, boolean includeSeparated);

	List<AwardResult> findProcessedAwards(Long facilityId, boolean includeAdult, boolean includeYouth,
			boolean includeOther, boolean includeActive, boolean includeSeparated, LocalDate lastAwardOnOrAfter,
			LocalDate lastAwardOnOrBefore);

}
