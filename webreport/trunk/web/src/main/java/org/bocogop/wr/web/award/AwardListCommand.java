package org.bocogop.wr.web.award;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import org.bocogop.wr.model.award.AwardResult;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.util.DateUtil;

public class AwardListCommand implements Serializable {
	private static final long serialVersionUID = 5846293774568882892L;

	// ----------------------------------- Fields

	private long facilityId;

	private boolean includeAdult;
	private boolean includeYouth;
	private boolean includeOther;
	private boolean includeActive;
	private boolean includeSeparated;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate startDate;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate endDate;
	private List<AwardResult> processedAwardResults;
	private List<AwardResult> eligibleAwardResults;
	private Map<Long, Volunteer> volunteersMap;
	private boolean processedSearched;
	private boolean eligibleSearched;
	Integer awardsProcessed = 0;

	// ----------------------------------- Constructors

	public AwardListCommand() {
	}

	public AwardListCommand(long facilityId) {
		this.facilityId = facilityId;

		this.includeAdult = true;
		this.includeYouth = true;
		this.includeOther = true;
		this.includeActive = true;
		this.includeSeparated = false;
	}

	// ----------------------------------- Accessor Methods

	public long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}

	public boolean isIncludeAdult() {
		return includeAdult;
	}

	public void setIncludeAdult(boolean includeAdult) {
		this.includeAdult = includeAdult;
	}

	public boolean isIncludeYouth() {
		return includeYouth;
	}

	public void setIncludeYouth(boolean includeYouth) {
		this.includeYouth = includeYouth;
	}

	public boolean isIncludeOther() {
		return includeOther;
	}

	public void setIncludeOther(boolean includeOther) {
		this.includeOther = includeOther;
	}

	public boolean isIncludeActive() {
		return includeActive;
	}

	public void setIncludeActive(boolean includeActive) {
		this.includeActive = includeActive;
	}

	public boolean isIncludeSeparated() {
		return includeSeparated;
	}

	public void setIncludeSeparated(boolean includeSeparated) {
		this.includeSeparated = includeSeparated;
	}

	public Map<Long, Volunteer> getVolunteersMap() {
		return volunteersMap;
	}

	public void setVolunteersMap(Map<Long, Volunteer> volunteersMap) {
		this.volunteersMap = volunteersMap;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Integer getAwardsProcessed() {
		return awardsProcessed;
	}

	public void setAwardsProcessed(Integer awardsProcessed) {
		this.awardsProcessed = awardsProcessed;
	}

	public List<AwardResult> getProcessedAwardResults() {
		return processedAwardResults;
	}

	public void setProcessedAwardResults(List<AwardResult> processedAwardResults) {
		this.processedAwardResults = processedAwardResults;
	}

	public List<AwardResult> getEligibleAwardResults() {
		return eligibleAwardResults;
	}

	public void setEligibleAwardResults(List<AwardResult> eligibleAwardResults) {
		this.eligibleAwardResults = eligibleAwardResults;
	}

	public boolean isProcessedSearched() {
		return processedSearched;
	}

	public void setProcessedSearched(boolean processedSearched) {
		this.processedSearched = processedSearched;
	}

	public boolean isEligibleSearched() {
		return eligibleSearched;
	}

	public void setEligibleSearched(boolean eligibleSearched) {
		this.eligibleSearched = eligibleSearched;
	}

}
