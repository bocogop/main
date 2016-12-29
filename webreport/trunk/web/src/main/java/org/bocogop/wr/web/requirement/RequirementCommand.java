package org.bocogop.wr.web.requirement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleTypeRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleTypeRequirement;
import org.bocogop.wr.model.requirement.RequirementApplicationType;
import org.bocogop.wr.model.requirement.RequirementDateType;
import org.bocogop.wr.model.requirement.RequirementScopeType;
import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.model.requirement.RequirementType;

public class RequirementCommand {

	// ------------------------------------- Fields

	private Long requirementId;

	@Length(max = 50)
	@NotBlank(message = "Name is required.")
	private String name;
	@Length(max = 250)
	private String description;
	@Length(max = 30)
	private String tmsCourseId;
	private boolean active = true;
	private RequirementDateType dateType;
	private Integer daysNotification;
	private RequirementScopeType scope;
	private RequirementApplicationType applicationType;
	private RequirementType type;

	private Set<RequirementStatus> validStatuses;

	private BenefitingServiceRoleType roleType;
	private Collection<BenefitingServiceRole> specificRoles;
	private Collection<BenefitingServiceRoleTemplate> specificRoleTemplates;
	private boolean preventTimeposting;

	// ------------------------------------- Constructors

	public RequirementCommand(RequirementScopeType scope, RequirementDateType dateType) {
		this.scope = scope;
		this.dateType = dateType;
		this.applicationType = RequirementApplicationType.ROLE_TYPE;
	}

	public RequirementCommand(AbstractRequirement r) {
		this(r.getScope(), r.getDateType());
		this.daysNotification = r.getDaysNotification();
		this.applicationType = r.getApplicationType();
		this.type = r.getType();
		this.requirementId = r.getId();
		this.name = r.getName();
		this.description = r.getDescription();
		this.tmsCourseId = r.getTmsCourseId();
		this.active = r.isActive();
		this.validStatuses = r.getAvailableStatuses().stream().map(p -> p.getStatus()).collect(Collectors.toSet());
		this.preventTimeposting = r.isPreventTimeposting();

		Class<?> clazz = Hibernate.getClass(r);

		if (clazz == FacilityRoleRequirement.class) {
			specificRoles = ((FacilityRoleRequirement) r).getBenefitingServiceRoles();
		} else if (clazz == FacilityRoleTypeRequirement.class) {
			roleType = ((FacilityRoleTypeRequirement) r).getRoleType();
		} else if (clazz == GlobalRoleRequirement.class) {
			specificRoleTemplates = ((GlobalRoleRequirement) r).getBenefitingServiceRoleTemplates();
		} else if (clazz == GlobalRoleTypeRequirement.class) {
			roleType = ((GlobalRoleTypeRequirement) r).getRoleType();
		}
	}

	// ------------------------------------- Business Methods

	public boolean isEdit() {
		return requirementId != null;
	}

	// ------------------------------------- Accessor Methods

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTmsCourseId() {
		return tmsCourseId;
	}

	public void setTmsCourseId(String tmsCourseId) {
		this.tmsCourseId = tmsCourseId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Long getRequirementId() {
		return requirementId;
	}

	public void setRequirementId(Long requirementId) {
		this.requirementId = requirementId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RequirementDateType getDateType() {
		return dateType;
	}

	public void setDateType(RequirementDateType dateType) {
		this.dateType = dateType;
	}

	public RequirementScopeType getScope() {
		return scope;
	}

	public void setScope(RequirementScopeType scope) {
		this.scope = scope;
	}

	public RequirementType getType() {
		return type;
	}

	public void setType(RequirementType type) {
		this.type = type;
	}

	public RequirementApplicationType getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(RequirementApplicationType applicationType) {
		this.applicationType = applicationType;
	}

	public Set<RequirementStatus> getValidStatuses() {
		if (validStatuses == null)
			validStatuses = new HashSet<>();
		return validStatuses;
	}

	public void setValidStatuses(Set<RequirementStatus> validStatuses) {
		this.validStatuses = validStatuses;
	}

	public BenefitingServiceRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(BenefitingServiceRoleType roleType) {
		this.roleType = roleType;
	}

	public Collection<BenefitingServiceRole> getSpecificRoles() {
		return specificRoles;
	}

	public void setSpecificRoles(Collection<BenefitingServiceRole> specificRoles) {
		this.specificRoles = specificRoles;
	}

	public Collection<BenefitingServiceRoleTemplate> getSpecificRoleTemplates() {
		return specificRoleTemplates;
	}

	public void setSpecificRoleTemplates(Collection<BenefitingServiceRoleTemplate> specificRoleTemplates) {
		this.specificRoleTemplates = specificRoleTemplates;
	}

	public boolean isPreventTimeposting() {
		return preventTimeposting;
	}

	public void setPreventTimeposting(boolean preventTimeposting) {
		this.preventTimeposting = preventTimeposting;
	}

	public Integer getDaysNotification() {
		return daysNotification;
	}

	public void setDaysNotification(Integer daysNotification) {
		this.daysNotification = daysNotification;
	}

}
