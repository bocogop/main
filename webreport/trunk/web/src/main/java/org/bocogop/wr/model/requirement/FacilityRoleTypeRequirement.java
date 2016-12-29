package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;

@Entity
@DiscriminatorValue("FT")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class FacilityRoleTypeRequirement extends FacilityRequirement {
	private static final long serialVersionUID = 5406082811830107442L;

	// ---------------------------------------- Fields

	private BenefitingServiceRoleType roleType;

	// ---------------------------------------- Constructors

	// ---------------------------------------- Business Methods

	@Override
	@Transient
	public RequirementApplicationType getApplicationType() {
		return RequirementApplicationType.ROLE_TYPE;
	}

	// ---------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_BenefitingServiceRoleTypeFK")
	// @JsonView(RequirementView.Basic.class)
	public BenefitingServiceRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(BenefitingServiceRoleType roleType) {
		this.roleType = roleType;
	}

}
