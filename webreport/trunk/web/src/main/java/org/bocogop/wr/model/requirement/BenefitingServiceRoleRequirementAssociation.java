package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;

@Entity
@Table(name = "BenefitingServiceRoleRequirement", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingServiceRoleRequirementAssociation
		extends AbstractAuditedVersionedPersistent<BenefitingServiceRoleRequirementAssociation> {
	private static final long serialVersionUID = 583796042812902141L;

	// -------------------------------------- Fields

	private AbstractRequirement requirement;
	private BenefitingServiceRole benefitingServiceRole;

	// -------------------------------------- Constructors

	public BenefitingServiceRoleRequirementAssociation() {
	}

	public BenefitingServiceRoleRequirementAssociation(FacilityRoleRequirement frr, BenefitingServiceRole bsr) {
		this.requirement = frr;
		this.benefitingServiceRole = bsr;
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(BenefitingServiceRoleRequirementAssociation oo) {
		return new EqualsBuilder()
				.append(nullSafeGetId(getBenefitingServiceRole()), nullSafeGetId(oo.getBenefitingServiceRole()))
				.append(nullSafeGetId(getRequirement()), nullSafeGetId(oo.getRequirement())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getBenefitingServiceRole()))
				.append(nullSafeGetId(getRequirement())).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceRoleFK", nullable = false)
	public BenefitingServiceRole getBenefitingServiceRole() {
		return benefitingServiceRole;
	}

	public void setBenefitingServiceRole(BenefitingServiceRole benefitingServiceRole) {
		this.benefitingServiceRole = benefitingServiceRole;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RequirementFK", nullable = false)
	public AbstractRequirement getRequirement() {
		return requirement;
	}

	public void setRequirement(AbstractRequirement requirement) {
		this.requirement = requirement;
	}

}
