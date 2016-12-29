package org.bocogop.wr.model.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.requirement.BenefitingServiceRoleTemplateRequirementAssociation;

@Entity
@Table(name = "BenefitingServiceRoleTemplates", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingServiceRoleTemplate extends AbstractAuditedVersionedPersistent<BenefitingServiceRoleTemplate>
		implements Comparable<BenefitingServiceRoleTemplate> {
	private static final long serialVersionUID = 583796042812902141L;

	public static class BenefitingServiceRoleTemplateView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface SearchUnused extends Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	@Length(max = 50)
	private String name;

	private BenefitingServiceRoleType roleType;
	private BenefitingServiceTemplate benefitingServiceTemplate;
	private boolean inactive;
	private boolean requiredAndReadOnly;

	private List<BenefitingServiceRole> benefitingServiceRoles;
	private List<BenefitingServiceRoleTemplateRequirementAssociation> benefitingServiceRoleTemplateRequirementAssociations;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	public int compareTo(BenefitingServiceRoleTemplate o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getName()), nullSafeLowercase(o.getName()))
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(BenefitingServiceRoleTemplate oo) {
		return new EqualsBuilder()
				.append(nullSafeGetId(getBenefitingServiceTemplate()), nullSafeGetId(oo.getBenefitingServiceTemplate()))
				.append(getName(), oo.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getBenefitingServiceTemplate())).append(getName())
				.toHashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 50, nullable = false)
	@JsonView(BenefitingServiceRoleTemplateView.Basic.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceTemplatesFK", nullable = false)
	@JsonIgnore
	public BenefitingServiceTemplate getBenefitingServiceTemplate() {
		return benefitingServiceTemplate;
	}

	public void setBenefitingServiceTemplate(BenefitingServiceTemplate serviceTemplate) {
		this.benefitingServiceTemplate = serviceTemplate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_BenefitingServiceRoleTypesFK")
	@JsonView(BenefitingServiceRoleTemplateView.Basic.class)
	public BenefitingServiceRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(BenefitingServiceRoleType serviceRoleType) {
		this.roleType = serviceRoleType;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(BenefitingServiceRoleTemplateView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@Column(name = "IsRequiredAndReadOnly", nullable = false)
	@JsonView(BenefitingServiceRoleTemplateView.Basic.class)
	public boolean isRequiredAndReadOnly() {
		return requiredAndReadOnly;
	}

	public void setRequiredAndReadOnly(boolean requiredAndReadOnly) {
		this.requiredAndReadOnly = requiredAndReadOnly;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<BenefitingServiceRole> getBenefitingServiceRoles() {
		if (benefitingServiceRoles == null)
			benefitingServiceRoles = new ArrayList<>();
		return benefitingServiceRoles;
	}

	public void setBenefitingServiceRoles(List<BenefitingServiceRole> benefitingServiceRoles) {
		this.benefitingServiceRoles = benefitingServiceRoles;
	}

	@OneToMany(mappedBy = "benefitingServiceRoleTemplate", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	private List<BenefitingServiceRoleTemplateRequirementAssociation> getBenefitingServiceRoleTemplateAssociations() {
		if (benefitingServiceRoleTemplateRequirementAssociations == null)
			benefitingServiceRoleTemplateRequirementAssociations = new ArrayList<>();
		return benefitingServiceRoleTemplateRequirementAssociations;
	}

	public void setBenefitingServiceRoleTemplateAssociations(
			List<BenefitingServiceRoleTemplateRequirementAssociation> benefitingServiceRoleTemplateRequirementAssociations) {
		this.benefitingServiceRoleTemplateRequirementAssociations = benefitingServiceRoleTemplateRequirementAssociations;
	}

}
