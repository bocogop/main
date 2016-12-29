package org.bocogop.wr.web.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AvailableAssignment {
	
	long id;
	BenefitingService benefitingService;
	BenefitingServiceRole benefitingServiceRole;

	public AvailableAssignment(long id, BenefitingService benefitingService,
			BenefitingServiceRole benefitingServiceRole) {
		this.id = id;
		this.benefitingService = benefitingService;
		this.benefitingServiceRole = benefitingServiceRole;
	}

	public long getId() {
		return id;
	}

	public BenefitingService getBenefitingService() {
		return benefitingService;
	}

	public BenefitingServiceRole getBenefitingServiceRole() {
		return benefitingServiceRole;
	}

}