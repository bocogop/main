package org.bocogop.wr.model.time;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.FacilityUtil;

@Entity
@Table(name = "OccasionalHours", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class OccasionalWorkEntry extends AbstractAuditedVersionedPersistent<OccasionalWorkEntry>
		implements Comparable<OccasionalWorkEntry> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class OccasionalWorkEntryView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface TimeReport extends Basic {
		}
	}

	// -------------------------------------- Fields

	@NotNull
	private AbstractBasicOrganization organization;
	@NotNull
	private BenefitingService benefitingService;
	@NotNull
	private BenefitingServiceRole benefitingServiceRole;
	@NotNull
	private AbstractUpdateableLocation<?> facility;

	private int numberInGroup;
	@NotNull
	private LocalDate dateWorked;
	private double hoursWorked;
	private String comments;
	private int fiscalYear;

	// -------------------------------------- Constructors

	public OccasionalWorkEntry() {
	}

	public OccasionalWorkEntry(AbstractBasicOrganization organization, BenefitingServiceRole bsr, LocalDate dateWorked,
			int numberInGroup, double hoursWorked, String comments) {
		setOrganization(organization);
		setFacility(bsr.getFacility());
		setBenefitingService(bsr.getBenefitingService());
		setBenefitingServiceRole(bsr);
		setDateWorked(dateWorked);
		setNumberInGroup(numberInGroup);
		setHoursWorked(hoursWorked);
		setComments(comments);
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public long getLocationId() {
		return FacilityUtil.getLocationId(getFacility());
	}

	@Transient
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public String getLocationDisplayName() {
		return FacilityUtil.getLocationDisplayName(getFacility());
	}

	@Transient
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public String getFacilityDisplayName() {
		return FacilityUtil.getFacilityDisplayName(getFacility());
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(OccasionalWorkEntry oo) {
		return new EqualsBuilder().append(getDateWorked(), oo.getDateWorked())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(nullSafeGetId(getOrganization()), nullSafeGetId(oo.getOrganization()))
				.append(nullSafeGetId(getBenefitingService()), nullSafeGetId(oo.getBenefitingService()))
				.append(getHoursWorked(), oo.getHoursWorked()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDateWorked()).append(nullSafeGetId(getFacility()))
				.append(nullSafeGetId(getOrganization())).toHashCode();
	}

	@Override
	public int compareTo(OccasionalWorkEntry o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDateWorked(), o.getDateWorked())
				.append(getCreatedDate(), o.getCreatedDate()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return "{" + getHoursWorked() + " hours on " + getDateWorked().format(DateUtil.DATE_ONLY_FORMAT) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationFK")
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceFK", nullable = false)
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public BenefitingService getBenefitingService() {
		return benefitingService;
	}

	public void setBenefitingService(BenefitingService benefitingService) {
		this.benefitingService = benefitingService;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public AbstractUpdateableLocation<?> getFacility() {
		return facility;
	}

	public void setFacility(AbstractUpdateableLocation<?> facility) {
		this.facility = facility;
	}

	@Column(nullable = false)
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public int getNumberInGroup() {
		return numberInGroup;
	}

	public void setNumberInGroup(int numberInGroup) {
		this.numberInGroup = numberInGroup;
	}

	@JsonView(OccasionalWorkEntryView.Basic.class)
	@Column(nullable = false)
	public LocalDate getDateWorked() {
		return dateWorked;
	}

	public void setDateWorked(LocalDate dateWorked) {
		this.dateWorked = dateWorked;
	}

	@JsonView(OccasionalWorkEntryView.Basic.class)
	@Column(nullable = false, scale = 6, precision = 2, name = "TotalHours")
	public double getHoursWorked() {
		return hoursWorked;
	}

	public void setHoursWorked(double hoursWorked) {
		this.hoursWorked = hoursWorked;
	}

	@Column(insertable = false, updatable = false)
	public int getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(int fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceRoleFK", nullable = false)
	@JsonView(OccasionalWorkEntryView.Basic.class)
	public BenefitingServiceRole getBenefitingServiceRole() {
		return benefitingServiceRole;
	}

	public void setBenefitingServiceRole(BenefitingServiceRole benefitingServiceRole) {
		this.benefitingServiceRole = benefitingServiceRole;
	}

	@JsonView(OccasionalWorkEntryView.Basic.class)
	@Column(length = 40)
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
