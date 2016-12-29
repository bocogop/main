package org.bocogop.wr.model.expenditure;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUser.AppUserView;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.expenditure.LedgerAdjustment.LedgerAdjustmentView;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.conversion.UnitTypeConverter;

@Entity
@Table(name = "Expenditure", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Expenditure extends AbstractAuditedVersionedPersistent<Expenditure> implements Comparable<Expenditure> {
	private static final long serialVersionUID = 2676191929993376944L;

	public static class ExpenditureView {
		public interface Basic {
		}

		public interface Search extends Basic, AppUserView.Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Facility facility;
	private String purchaseOrderNumber;
	private String transactionId;
	private DonGenPostFund donGenPostFund;
	private AppUser originator;
	private LocalDate requestDate;
	private BigDecimal amount;

	private String description;
	private String vendor;
	private Integer quantity;
	private UnitType unit;
	private BigDecimal unitPrice;
	private String comments;

	private List<ExpenditureDonationAssociation> donationAssociations;

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(ExpenditureView.Search.class)
	public List<DonationSummary> getDonations() {
		return getDonationAssociations().stream().map(p -> p.getDonation()).collect(Collectors.toList());
	}

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(Expenditure oo) {
		if (equals(oo))
			return 0;

		return new CompareToBuilder() //
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())) //
				.append(getRequestDate(), oo.getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount()), nullSafeGetDoubleValue(oo.getAmount())) //
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(Expenditure oo) {
		// I don't see a more efficient way to do this since we don't have a
		// simpler business key - CPB
		return new EqualsBuilder() //
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())) //
				.append(getRequestDate(), oo.getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount()), nullSafeGetDoubleValue(oo.getAmount())) //
				.append(nullSafeGetId(getDonGenPostFund()), nullSafeGetId(oo.getDonGenPostFund())) //
				.append(nullSafeGetId(getOriginator()), nullSafeGetId(oo.getOriginator())) //
				.append(getTransactionId(), oo.getTransactionId()) //
				.append(getPurchaseOrderNumber(), oo.getPurchaseOrderNumber()) //
				.append(getDescription(), oo.getDescription()) //
				.append(getVendor(), oo.getVendor()) //
				.append(getQuantity(), oo.getQuantity()) //
				.append(getUnitPrice(), oo.getUnitPrice()) //
				.append(getComments(), oo.getComments()) //
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder() //
				.append(nullSafeGetId(getFacility())) //
				.append(getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount())) //
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@NotNull
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GeneralPostFundFK", nullable = false)
	@NotNull
	public DonGenPostFund getDonGenPostFund() {
		return donGenPostFund;
	}

	public void setDonGenPostFund(DonGenPostFund donGenPostFund) {
		this.donGenPostFund = donGenPostFund;
	}

	@Column(nullable = false)
	@NotNull
	public LocalDate getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(LocalDate requestDate) {
		this.requestDate = requestDate;
	}

	@Column(nullable = false)
	@NotNull
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Column(nullable = false)
	@NotNull
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AppUserFK")
	@NotNull
	@JsonView({ ExpenditureView.Extended.class, ExpenditureView.Search.class, LedgerAdjustmentView.Search.class })
	public AppUser getOriginator() {
		return originator;
	}

	public void setOriginator(AppUser originator) {
		this.originator = originator;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Convert(converter = UnitTypeConverter.class)
	public UnitType getUnit() {
		return unit;
	}

	public void setUnit(UnitType unit) {
		this.unit = unit;
	}

	@OneToMany(mappedBy = "expenditure", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<ExpenditureDonationAssociation> getDonationAssociations() {
		if (donationAssociations == null)
			donationAssociations = new ArrayList<>();
		return donationAssociations;
	}

	public void setDonationAssociations(List<ExpenditureDonationAssociation> donationAssociations) {
		this.donationAssociations = donationAssociations;
	}

}
