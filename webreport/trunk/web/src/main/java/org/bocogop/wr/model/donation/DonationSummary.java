package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;
import static org.bocogop.wr.model.donation.DonationType.DonationTypeValue.ACTIVITY;
import static org.bocogop.wr.model.donation.DonationType.DonationTypeValue.ITEM;
import static org.bocogop.wr.model.views.DonationSummaryLetterType.TYPE_1;
import static org.bocogop.wr.model.views.DonationSummaryLetterType.TYPE_2;
import static org.bocogop.wr.model.views.DonationSummaryLetterType.TYPE_3;
import static org.bocogop.wr.model.views.DonationSummaryLetterType.TYPE_4;
import static org.bocogop.wr.model.views.DonationSummaryLetterType.TYPE_5;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.donation.DonationType.DonationTypeValue;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.model.expenditure.Expenditure.ExpenditureView;
import org.bocogop.wr.model.expenditure.ExpenditureDonationAssociation;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.views.DonationSummaryLetterType;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "DonationSummary", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonationSummary extends AbstractAuditedVersionedPersistent<DonationSummary>
		implements Comparable<DonationSummary> {
	private static final long serialVersionUID = 2676191929993376944L;

	public static class DonationSummaryView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}
	}

	// -------------------------------------- Fields

	private Facility facility;
	private DonationType donationType;
	private AbstractBasicOrganization organization;
	private Donor donor;
	private State familyContactState;
	private DonationReference donReference;
	private String ackOverrideNamePrefix;
	private String ackOverrideFirstName;
	private String ackOverrideLastName;
	private String ackOverrideAddress1;
	private String ackOverrideAddress2;
	private String ackOverrideCity;
	private String ackOverrideZip;
	private State ackOverrideState;
	private boolean monetary;
	private String checkNumber;
	private LocalDate checkDate;
	private StdCreditCardType stdCreditCardType;
	private String creditCardTransactionId;
	private String epayTrackingID;
	private LocalDate donationDate;
	private LocalDate acknowledgementDate;
	private String donationDescription;
	private String additionalComments;
	private String fieldServiceReceipt;
	private String designation;
	private String inMemoryOf;
	private String salutation;
	private String cc1;
	private String cc2;
	private String cc3;
	private String cc4;
	private String cc5;
	private String familyContact;
	private String familyContactAddress;
	private String familyContactCity;
	private String familyContactZip;
	private String ackOverrideOrgContactName;
	private String ackOverrideOrgContactTitle;

	private List<DonationDetail> donationDetails;
	private List<ExpenditureDonationAssociation> expenditureAssociations;

	// -------------------------------------- Business Methods

	@Transient
	@JsonView({ ExpenditureView.Search.class, DonationSummaryView.Search.class })
	public BigDecimal getTotalDonationAmount() {
		BigDecimal bd = BigDecimal.ZERO;
		for (DonationDetail d : getDonationDetails()) {
			bd = bd.add(d.getDonationValue());
		}
		return bd;
	}

	@Transient
	public DonationSummaryLetterType getLetterType() {
		DonationType t = getDonationType();
		if (t == null)
			return null;

		DonationTypeValue v = DonationTypeValue.getById(t.getId());
		if (v != ITEM && v != ACTIVITY && isNotBlank(getInMemoryOf()) && isBlank(getFamilyContact())) {
			return TYPE_1;
		} else if (v != ITEM && v != ACTIVITY && isNotBlank(getInMemoryOf()) && isNotBlank(getFamilyContact())) {
			return TYPE_2;
		} else if (v != ITEM && v != ACTIVITY && isBlank(getInMemoryOf()) && isBlank(getFamilyContact())) {
			return TYPE_3;
		} else if (v == ITEM) {
			return TYPE_5;
		} else if (v == ACTIVITY) {
			return TYPE_4;
		}
		return null;
	}

	@Transient
	public String getOrgOrOtherGroup() {
		Donor d = getDonor();
		DonorTypeValue l = d.getDonorType().getLookupType();

		if (l == DonorTypeValue.ORG_AND_INDIVIDUAL) {
			AbstractBasicOrganization org = d.getOrganization();
			return org == null ? "" : org.getDisplayName();
		} else if (l == DonorTypeValue.OTHER_AND_INDIVIDUAL || l == DonorTypeValue.OTHER_GROUPS) {
			return d.getOtherGroup();
		}
		return "";
	}

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(DonationSummary oo) {
		if (equals(oo))
			return 0;

		return new CompareToBuilder().append(getDonationDate(), oo.getDonationDate()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(DonationSummary oo) {
		return new EqualsBuilder().append(nullSafeGetId(getDonor()), nullSafeGetId(oo.getDonor()))
				.append(getDonationDate(), oo.getDonationDate()).append(getDonationType(), oo.getDonationType())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getDesignation(), oo.getDesignation())
				.append(getDonationDescription(), oo.getDonationDescription())
				.append(getCheckNumber(), oo.getCheckNumber()).append(getDonReference(), oo.getDonReference())
				.append(getFieldServiceReceipt(), oo.getFieldServiceReceipt())
				.append(getCreatedDate(), oo.getCreatedDate()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getDonor())).append(getDonationDate())
				.append(getDonationType()).append(nullSafeGetId(getFacility())).append(getDesignation())
				.append(getDonationDescription()).append(getCheckNumber()).append(getDonReference())
				.append(getFieldServiceReceipt()).append(getCreatedDate()).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	@JsonView(DonationSummaryView.Search.class)
	@NotNull
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationFK")
	@JsonView(DonationSummaryView.Search.class)
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationTypeFK")
	@JsonView(DonationSummaryView.Search.class)
	@NotNull
	public DonationType getDonationType() {
		return donationType;
	}

	public void setDonationType(DonationType donationType) {
		this.donationType = donationType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonorFK")
	@JsonView({ DonationSummaryView.Search.class, ExpenditureView.Search.class })
	@NotNull
	public Donor getDonor() {
		return donor;
	}

	public void setDonor(Donor donor) {
		this.donor = donor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FamilyContactStateFK")
	@JsonView(DonationSummaryView.Extended.class)
	public State getFamilyContactState() {
		return familyContactState;
	}

	public void setFamilyContactState(State familyContactState) {
		this.familyContactState = familyContactState;
	}

	@Column(name = "AckOverrideNamePrefix", length = 10)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideNamePrefix() {
		return ackOverrideNamePrefix;
	}

	public void setAckOverrideNamePrefix(String ackOverrideNamePrefix) {
		this.ackOverrideNamePrefix = ackOverrideNamePrefix;
	}

	@Column(name = "AckOverrideFirstName", length = 30)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideFirstName() {
		return ackOverrideFirstName;
	}

	public void setAckOverrideFirstName(String ackOverrideFirstName) {
		this.ackOverrideFirstName = ackOverrideFirstName;
	}

	@Column(name = "ackOverrideLastName", length = 30)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideLastName() {
		return ackOverrideLastName;
	}

	public void setAckOverrideLastName(String ackOverrideLastName) {
		this.ackOverrideLastName = ackOverrideLastName;
	}

	@Column(name = "AckOverrideAddress1", length = 35)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideAddress1() {
		return ackOverrideAddress1;
	}

	public void setAckOverrideAddress1(String ackOverrideAddress1) {
		this.ackOverrideAddress1 = ackOverrideAddress1;
	}

	@Column(name = "AckOverrideAddress2", length = 35)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideAddress2() {
		return ackOverrideAddress2;
	}

	public void setAckOverrideAddress2(String ackOverrideAddress2) {
		this.ackOverrideAddress2 = ackOverrideAddress2;
	}

	@Column(name = "AckOverrideCity", length = 30)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideCity() {
		return ackOverrideCity;
	}

	public void setAckOverrideCity(String ackOverrideCity) {
		this.ackOverrideCity = ackOverrideCity;
	}

	@Column(name = "AckOverrideZip", length = 10)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideZip() {
		return ackOverrideZip;
	}

	public void setAckOverrideZip(String ackOverrideZip) {
		this.ackOverrideZip = ackOverrideZip;
	}

	@Column(name = "AckOverrideOrgContactName", length = 255)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideOrgContactName() {
		return ackOverrideOrgContactName;
	}

	public void setAckOverrideOrgContactName(String ackOverrideOrgContactName) {
		this.ackOverrideOrgContactName = ackOverrideOrgContactName;
	}

	@Column(name = "AckOverrideOrgContactTitle", length = 255)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAckOverrideOrgContactTitle() {
		return ackOverrideOrgContactTitle;
	}

	public void setAckOverrideOrgContactTitle(String ackOverrideOrgContactTitle) {
		this.ackOverrideOrgContactTitle = ackOverrideOrgContactTitle;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AckOverrideStateFK")
	@JsonView(DonationSummaryView.Extended.class)
	public State getAckOverrideState() {
		return ackOverrideState;
	}

	public void setAckOverrideState(State ackOverrideState) {
		this.ackOverrideState = ackOverrideState;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_CreditCardTypeFK")
	@JsonView(DonationSummaryView.Extended.class)
	public StdCreditCardType getStdCreditCardType() {
		return stdCreditCardType;
	}

	public void setStdCreditCardType(StdCreditCardType stdCreditCardType) {
		this.stdCreditCardType = stdCreditCardType;
	}

	@Column(name = "CreditCardTransactionID", length = 20)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCreditCardTransactionId() {
		return creditCardTransactionId;
	}

	public void setCreditCardTransactionId(String creditCardTransactionId) {
		this.creditCardTransactionId = creditCardTransactionId;
	}

	@Column(name = "EpayTrackingID", length = 15)
	@JsonView(DonationSummaryView.Extended.class)
	public String getEpayTrackingID() {
		return epayTrackingID;
	}

	public void setEpayTrackingID(String epayTrackingID) {
		this.epayTrackingID = epayTrackingID;
	}

	@Column(name = "IsMonetary")
	@NotNull
	@JsonView(DonationSummaryView.Extended.class)
	public boolean isMonetary() {
		return monetary;
	}

	public void setMonetary(boolean monetary) {
		this.monetary = monetary;
	}

	@Column(name = "CheckNumber", length = 15)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCheckNumber() {
		return checkNumber;
	}

	public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}

	@Column(name = "CheckDate")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	@JsonView(DonationSummaryView.Extended.class)
	public LocalDate getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(LocalDate checkDate) {
		this.checkDate = checkDate;
	}

	@Column(name = "DonationDate")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	@JsonView({ DonationSummaryView.Search.class, ExpenditureView.Search.class })
	public LocalDate getDonationDate() {
		return donationDate;
	}

	public void setDonationDate(LocalDate donationDate) {
		this.donationDate = donationDate;
	}

	@Column(name = "AcknowledgementDate")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	@JsonView(DonationSummaryView.Extended.class)
	public LocalDate getAcknowledgementDate() {
		return acknowledgementDate;
	}

	public void setAcknowledgementDate(LocalDate acknowledgementDate) {
		this.acknowledgementDate = acknowledgementDate;
	}

	@Column(name = "DonationDescription", length = 250)
	@JsonView({ DonationSummaryView.Extended.class, DonationSummaryView.Search.class, ExpenditureView.Search.class })
	public String getDonationDescription() {
		return donationDescription;
	}

	public void setDonationDescription(String donationDescription) {
		this.donationDescription = donationDescription;
	}

	@Column(name = "AdditionalComments", length = 250)
	@JsonView(DonationSummaryView.Extended.class)
	public String getAdditionalComments() {
		return additionalComments;
	}

	public void setAdditionalComments(String additionalComments) {
		this.additionalComments = additionalComments;
	}

	@Column(name = "FieldServiceReceipt", length = 12)
	@JsonView(DonationSummaryView.Extended.class)
	public String getFieldServiceReceipt() {
		return fieldServiceReceipt;
	}

	public void setFieldServiceReceipt(String fieldServiceReceipt) {
		this.fieldServiceReceipt = fieldServiceReceipt;
	}

	@Column(name = "Designation", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	@Column(name = "InMemoryOf", length = 250)
	@JsonView(DonationSummaryView.Extended.class)
	public String getInMemoryOf() {
		return inMemoryOf;
	}

	public void setInMemoryOf(String inMemoryOf) {
		this.inMemoryOf = inMemoryOf;
	}

	@Column(name = "Salutation", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	@Column(name = "CC1", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCc1() {
		return cc1;
	}

	public void setCc1(String cc1) {
		this.cc1 = cc1;
	}

	@Column(name = "CC2", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCc2() {
		return cc2;
	}

	public void setCc2(String cc2) {
		this.cc2 = cc2;
	}

	@Column(name = "CC3", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCc3() {
		return cc3;
	}

	public void setCc3(String cc3) {
		this.cc3 = cc3;
	}

	@Column(name = "CC4", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCc4() {
		return cc4;
	}

	public void setCc4(String cc4) {
		this.cc4 = cc4;
	}

	@Column(name = "CC5", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getCc5() {
		return cc5;
	}

	public void setCc5(String cc5) {
		this.cc5 = cc5;
	}

	@Column(name = "FamilyContact", length = 50)
	@JsonView(DonationSummaryView.Extended.class)
	public String getFamilyContact() {
		return familyContact;
	}

	public void setFamilyContact(String familyContact) {
		this.familyContact = familyContact;
	}

	@Column(name = "FamilyContactAddress", length = 35)
	@JsonView(DonationSummaryView.Extended.class)
	public String getFamilyContactAddress() {
		return familyContactAddress;
	}

	public void setFamilyContactAddress(String familyContactAddress) {
		this.familyContactAddress = familyContactAddress;
	}

	@Column(name = "FamilyContactCity", length = 30)
	@JsonView(DonationSummaryView.Extended.class)
	public String getFamilyContactCity() {
		return familyContactCity;
	}

	public void setFamilyContactCity(String familyContactCity) {
		this.familyContactCity = familyContactCity;
	}

	@Column(name = "FamilyContactZipCode", length = 10)
	@JsonView(DonationSummaryView.Extended.class)
	public String getFamilyContactZip() {
		return familyContactZip;
	}

	public void setFamilyContactZip(String familyContactZip) {
		this.familyContactZip = familyContactZip;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationReferenceFK", unique = true)
	@JsonIgnore
	public DonationReference getDonReference() {
		return donReference;
	}

	public void setDonReference(DonationReference donReference) {
		this.donReference = donReference;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "donationSummary", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonView({ DonationSummaryView.Extended.class, DonationSummaryView.Search.class, ExpenditureView.Search.class })
	@BatchSize(size = 500)
	public List<DonationDetail> getDonationDetails() {
		if (donationDetails == null)
			donationDetails = new ArrayList<>();
		return donationDetails;
	}

	public void setDonationDetails(List<DonationDetail> donationDetails) {
		this.donationDetails = donationDetails;
	}

	@OneToMany(mappedBy = "donation", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<ExpenditureDonationAssociation> getExpenditureAssociations() {
		if (expenditureAssociations == null)
			expenditureAssociations = new ArrayList<>();
		return expenditureAssociations;
	}

	public void setExpenditureAssociations(List<ExpenditureDonationAssociation> expenditureAssociations) {
		this.expenditureAssociations = expenditureAssociations;
	}

}
