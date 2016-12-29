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
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;

@Entity
@Table(name = "BenefitingServiceRoleTemplateRequirement", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingServiceRoleTemplateRequirementAssociation
		extends AbstractAuditedVersionedPersistent<BenefitingServiceRoleTemplateRequirementAssociation> {
	private static final long serialVersionUID = 583796042812902141L;

	// -------------------------------------- Fields

	private AbstractRequirement requirement;
	private BenefitingServiceRoleTemplate benefitingServiceRoleTemplate;

	// -------------------------------------- Constructors

	public BenefitingServiceRoleTemplateRequirementAssociation() {
	}

	public BenefitingServiceRoleTemplateRequirementAssociation(GlobalRoleRequirement grr,
			BenefitingServiceRoleTemplate t) {
		this.requirement = grr;
		this.benefitingServiceRoleTemplate = t;
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(BenefitingServiceRoleTemplateRequirementAssociation oo) {
		return new EqualsBuilder()
				.append(nullSafeGetId(getBenefitingServiceRoleTemplate()),
						nullSafeGetId(oo.getBenefitingServiceRoleTemplate()))
				.append(nullSafeGetId(getRequirement()), nullSafeGetId(oo.getRequirement())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getBenefitingServiceRoleTemplate()))
				.append(nullSafeGetId(getRequirement())).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceRoleTemplateFK", nullable = false)
	public BenefitingServiceRoleTemplate getBenefitingServiceRoleTemplate() {
		return benefitingServiceRoleTemplate;
	}

	public void setBenefitingServiceRoleTemplate(BenefitingServiceRoleTemplate benefitingServiceRoleTemplate) {
		this.benefitingServiceRoleTemplate = benefitingServiceRoleTemplate;
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
