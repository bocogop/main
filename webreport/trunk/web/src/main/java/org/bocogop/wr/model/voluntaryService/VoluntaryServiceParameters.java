package org.bocogop.wr.model.voluntaryService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.ValidationUtil;

@Entity
@Table(name = "VoluntaryServices", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoluntaryServiceParameters extends AbstractAuditedVersionedPersistent<VoluntaryServiceParameters> {

	private static final long serialVersionUID = 3900607532835213411L;
	// -------------------------------------- Fields

	@NotNull
	private Facility facility;
	@NotNull(message = "Service title is required.")
	@NotEmpty
	@Size(max = 120)
	private String serviceTitle;
	@Size(max = 6)
	private String mailStop;
	private VoluntaryServiceType voluntaryServiceType;
	private Boolean isVAVSCommittee;
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String primaryPhone;
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String secondaryPhone;
	private String faxNumber;
	@NumberFormat(style = Style.NUMBER, pattern = "#,##0.00")
	@Max(999999999)
	private BigDecimal operatingCosts;
	@NumberFormat(style = Style.NUMBER, pattern = "#,##0.00")
	@Max(999999999)
	private BigDecimal staffCosts;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private ZonedDateTime lastUpdated;
	@Size(max = 150)
	private String chiefManager;
	@Size(max = 255)
	private String chiefTitle;
	@Size(max = 20)
	private String chiefUserName;

	// -------------------------------------- Constructors

	public VoluntaryServiceParameters() {
	}

	/**
	 * Constructor for new VoluntaryServiceParameters - populates required
	 * fields and creates new empty @NotNull children
	 * 
	 * @param facility
	 */
	public VoluntaryServiceParameters(String serviceTitle) {
		this.serviceTitle = serviceTitle;
	}

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(VoluntaryServiceParameters oo) {
		return new EqualsBuilder().append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getFacility())).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", unique = true)
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Column(name = "ServiceTitle", length = 120)
	public String getServiceTitle() {
		return serviceTitle;
	}

	public void setServiceTitle(String serviceTitle) {
		this.serviceTitle = serviceTitle;
	}

	@Column(name = "MailStop", length = 6)
	public String getMailStop() {
		return mailStop;
	}

	public void setMailStop(String mailStop) {
		this.mailStop = mailStop;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVoluntaryServiceTypesFK")
	public VoluntaryServiceType getVoluntaryServiceType() {
		return voluntaryServiceType;
	}

	public void setVoluntaryServiceType(VoluntaryServiceType voluntaryServiceType) {
		this.voluntaryServiceType = voluntaryServiceType;
	}

	@Column(name = "IsOwnVAVSCommittee")
	public Boolean getIsVAVSCommittee() {
		return isVAVSCommittee;
	}

	public void setIsVAVSCommittee(Boolean isVAVSCommittee) {
		if (isVAVSCommittee == null)
			isVAVSCommittee = false;
		this.isVAVSCommittee = isVAVSCommittee;
	}

	@Column(name = "PrimaryPhone", length = 30)
	public String getPrimaryPhone() {
		return primaryPhone;
	}

	public void setPrimaryPhone(String primaryPhone) {
		this.primaryPhone = primaryPhone;
	}

	@Column(name = "SecondaryPhone", length = 30)
	public String getSecondaryPhone() {
		return secondaryPhone;
	}

	public void setSecondaryPhone(String secondaryPhone) {
		this.secondaryPhone = secondaryPhone;
	}

	@Column(name = "FaxNumber", length = 30)
	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	@Column(name = "OperatingCosts")
	public BigDecimal getOperatingCosts() {
		return operatingCosts;
	}

	public void setOperatingCosts(BigDecimal operatingCosts) {
		this.operatingCosts = operatingCosts;
	}

	@Column(name = "StaffCosts")
	public BigDecimal getStaffCosts() {
		return staffCosts;
	}

	public void setStaffCosts(BigDecimal staffCosts) {
		this.staffCosts = staffCosts;
	}

	@Column(name = "LastUpdated")
	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Column(name = "ChiefManager", length = 30)
	public String getChiefManager() {
		return chiefManager;
	}

	public void setChiefManager(String chiefManager) {
		this.chiefManager = chiefManager;
	}

	@Column(name = "ChiefTitle", length = 120)
	public String getChiefTitle() {
		return chiefTitle;
	}

	public void setChiefTitle(String chiefTitle) {
		this.chiefTitle = chiefTitle;
	}

	@Column(name = "ChiefUserName", length = 120)
	public String getChiefUserName() {
		return chiefUserName;
	}

	public void setChiefUserName(String chiefUserName) {
		this.chiefUserName = chiefUserName;
	}
	
	

}
