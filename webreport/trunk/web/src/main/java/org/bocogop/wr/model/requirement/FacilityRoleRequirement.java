package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;

@Entity
@DiscriminatorValue("FR")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class FacilityRoleRequirement extends FacilityRequirement {
	private static final long serialVersionUID = 5406082811830107442L;

	// ---------------------------------------- Fields

	private List<BenefitingServiceRoleRequirementAssociation> benefitingServiceRoleRequirementAssociations;

	// ---------------------------------------- Constructors

	// ---------------------------------------- Business Methods

	@Override
	@Transient
	public RequirementApplicationType getApplicationType() {
		return RequirementApplicationType.SPECIFIC_ROLES;
	}

	@Transient
	public List<BenefitingServiceRole> getBenefitingServiceRoles() {
		return getBenefitingServiceRoleAssociations().stream().map(p -> p.getBenefitingServiceRole())
				.collect(Collectors.toList());
	}

	// ---------------------------------------- Accessor Methods

	@OneToMany(mappedBy = "requirement", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<BenefitingServiceRoleRequirementAssociation> getBenefitingServiceRoleAssociations() {
		if (benefitingServiceRoleRequirementAssociations == null)
			benefitingServiceRoleRequirementAssociations = new ArrayList<>();
		return benefitingServiceRoleRequirementAssociations;
	}

	public void setBenefitingServiceRoleAssociations(
			List<BenefitingServiceRoleRequirementAssociation> benefitingServiceRoleRequirementAssociations) {
		this.benefitingServiceRoleRequirementAssociations = benefitingServiceRoleRequirementAssociations;
	}

}
