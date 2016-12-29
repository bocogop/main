package org.bocogop.wr.model.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate.BenefitingServiceRoleTemplateView;

@Entity
@Table(name = "BenefitingServiceTemplates", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingServiceTemplate extends AbstractAuditedVersionedPersistent<BenefitingServiceTemplate>
		implements Comparable<BenefitingServiceTemplate> {
	private static final long serialVersionUID = 583796042812902141L;

	public static class BenefitingServiceTemplateView {
		public interface Basic {
		}

		public interface Search extends Basic, BenefitingServiceRoleTemplateView.Basic {
		}

		public interface SearchUnused extends Basic, BenefitingServiceRoleTemplateView.SearchUnused {
		}

		public interface Extended extends Basic {
		}

		public interface ListBenefitingServiceTemplatesWithRoles
				extends Extended, BenefitingServiceRoleTemplateView.Basic {
		}
	}

	// -------------------------------------- Fields

	@NotBlank
	private String name;
	private String subdivision;
	private String abbreviation;
	private boolean gamesRelated;
	private boolean inactive;

	private SortedSet<BenefitingServiceRoleTemplate> serviceRoleTemplates;

	private List<BenefitingService> benefitingServices;

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(BenefitingServiceTemplateView.Extended.class)
	public String getDisplayName() {
		return getName() + (StringUtils.isBlank(subdivision) ? "" : " - " + subdivision);
	}

	// -------------------------------------- Common Methods

	@Override
	public int compareTo(BenefitingServiceTemplate o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(BenefitingServiceTemplate oo) {
		return new EqualsBuilder().append(getName(), oo.getName()).append(getSubdivision(), oo.getSubdivision())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(getSubdivision()).toHashCode();
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "ServiceName", length = 50, nullable = false)
	@JsonView(BenefitingServiceTemplateView.Basic.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 30)
	@JsonView(BenefitingServiceTemplateView.Basic.class)
	public String getSubdivision() {
		return subdivision;
	}

	public void setSubdivision(String subdivision) {
		this.subdivision = subdivision;
	}

	@Column(length = 7)
	@JsonView(BenefitingServiceTemplateView.Basic.class)
	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "benefitingServiceTemplate", cascade = CascadeType.MERGE)
	@BatchSize(size = 500)
	@JsonView(BenefitingServiceTemplateView.ListBenefitingServiceTemplatesWithRoles.class)
	@SortNatural
	public SortedSet<BenefitingServiceRoleTemplate> getServiceRoleTemplates() {
		if (serviceRoleTemplates == null)
			serviceRoleTemplates = new TreeSet<>();
		return serviceRoleTemplates;
	}

	public void setServiceRoleTemplates(SortedSet<BenefitingServiceRoleTemplate> serviceRoleTemplates) {
		this.serviceRoleTemplates = serviceRoleTemplates;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<BenefitingService> getBenefitingServices() {
		if (benefitingServices == null)
			benefitingServices = new ArrayList<>();
		return benefitingServices;
	}

	public void setBenefitingServices(List<BenefitingService> benefitingServices) {
		this.benefitingServices = benefitingServices;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(BenefitingServiceTemplateView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@Column(name = "IsGamesRelated", nullable = false)
	@JsonView(BenefitingServiceTemplateView.Basic.class)
	public boolean isGamesRelated() {
		return gamesRelated;
	}

	public void setGamesRelated(boolean gamesRelated) {
		this.gamesRelated = gamesRelated;
	}

}
