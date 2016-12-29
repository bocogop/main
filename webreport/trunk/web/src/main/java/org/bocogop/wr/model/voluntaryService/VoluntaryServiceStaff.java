package org.bocogop.wr.model.voluntaryService;

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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.BasicUserFields;
import org.bocogop.wr.model.BasicUserFields.AppUserAdapter;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "VoluntaryServiceStaff", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoluntaryServiceStaff extends AbstractAuditedVersionedPersistent<VoluntaryServiceStaff> {
	private static final long serialVersionUID = -838492419896709672L;

	// -------------------------------------- Fields

	private Facility facility;
	private Integer reportOrder = 0; // reportOrder is required, but there is no
										// input for it. assign zero as default
	private String comment;
	private String namePrefix;
	private String nickName;
	private String grade;
	private LocalDate vavsStartDate;
	private LocalDate vavsEndDate;
	private LocalDate retirementEstimateDate;
	private LocalDate retirementEligibleDate;
	private AppUser appUser;
	private StaffTitle staffTitle;
	private boolean chief;
	private boolean vavsLeadership;
	private boolean emailNotifications;

	// -------------------------------------- Constructors

	public VoluntaryServiceStaff() {
	}

	public VoluntaryServiceStaff(Facility facility) {
		this.facility = facility;
	}
	
	// -------------------------------------- Business Methods

	@Transient
	public BasicUserFields getUserInfo() {
		return new BasicUserFields.AppUserAdapter(appUser);
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(VoluntaryServiceStaff oo) {
		return new EqualsBuilder().append(nullSafeGetId(getAppUser()), nullSafeGetId(oo.getAppUser()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getAppUser())).append(nullSafeGetId(getFacility()))
				.toHashCode();
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

	@Column(nullable = false)
	public Integer getReportOrder() {
		return reportOrder;
	}

	public void setReportOrder(Integer reportOrder) {
		this.reportOrder = reportOrder;
	}

	@Column(length = 250)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Column(length = 10)
	public String getNamePrefix() {
		return namePrefix;
	}

	public void setNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	@Column(length = 20)
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Column(length = 6)
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getVavsStartDate() {
		return vavsStartDate;
	}

	public void setVavsStartDate(LocalDate vavsStartDate) {
		this.vavsStartDate = vavsStartDate;
	}

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getVavsEndDate() {
		return vavsEndDate;
	}

	public void setVavsEndDate(LocalDate vavsEndDate) {
		this.vavsEndDate = vavsEndDate;
	}

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getRetirementEstimateDate() {
		return retirementEstimateDate;
	}

	public void setRetirementEstimateDate(LocalDate retirementEstimateDate) {
		this.retirementEstimateDate = retirementEstimateDate;
	}

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getRetirementEligibleDate() {
		return retirementEligibleDate;
	}

	public void setRetirementEligibleDate(LocalDate retirementEligibleDate) {
		this.retirementEligibleDate = retirementEligibleDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_USER_IDFK", unique = true, nullable = false)
	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrStaffTitlesFK")
	public StaffTitle getStaffTitle() {
		return staffTitle;
	}

	public void setStaffTitle(StaffTitle staffTitle) {
		this.staffTitle = staffTitle;
	}

	@Column(name="IsChief", nullable = false)
	public boolean isChief() {
		return chief;
	}

	public void setChief(boolean chief) {
		this.chief = chief;
	}

	@Column(name = "ISVISNLIAISON", nullable = false)
	public boolean isVavsLeadership() {
		return vavsLeadership;
	}

	public void setVavsLeadership(boolean vavsLeadership) {
		this.vavsLeadership = vavsLeadership;
	}

	@Column(name = "EmailNotifications", nullable = false)
	public boolean isEmailNotifications() {
		return emailNotifications;
	}

	public void setEmailNotifications(boolean emailNotifications) {
		this.emailNotifications = emailNotifications;
	}
	
	

}
