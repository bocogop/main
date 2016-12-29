package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "DonationLog", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonationLog extends AbstractIdentifiedPersistent<DonationLog> implements Comparable<DonationLog> {
	private static final long serialVersionUID = 2676191929993376944L;

	public static class DonationLogSummaryView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}
	}

	// -------------------------------------- Fields

	private String status; // "success" or other
	private String type; // credit_card, ach_debit, etc
	private String trackingId; // paygov_tracking_id
	private LocalDateTime transactionDate;
	private String facility;
	private String name;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String email;
	private String phone;
	private BigDecimal donationAmount;
	private String programField;
	private String additionalInfo;
	private String depositNumber;
	private ZonedDateTime createdDate;
	
	private DonationLogFile donationLogFile; 

	//
	private State matchedState;
	
	// -------------------------------------- Business Methods

	@PrePersist
	public void prePersist() {
		ZonedDateTime cd = getCreatedDate();

		if (cd == null)
			setCreatedDate(ZonedDateTime.now(ZoneId.of("Z")));
	}

	@Transient
	public String getAddressMultilineDisplay() {
		return StringUtil.getAddressDisplay(getAddress(), null, null, getCity(),
				getState(), getZip(), "\n");
	}

	@Transient
	public String parseFirstName() {
		String firstName = "";

		if (!name.isEmpty()) {
			String[] tokens = name.split("[\\s]+");
			firstName = tokens[0];
		}
		
		return firstName;
	}

	@Transient
	public String parseLastName() {
		String lastName = "";
		if (!name.isEmpty()) {
			String[] tokens = name.split("[\\s]+");
			lastName = tokens[tokens.length-1];
		} 
		return lastName;
	}
	
	@Transient
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	@JsonView(DonationLogSummaryView.Basic.class)
	public LocalDate getTransactionDateOnly() {
		return transactionDate.toLocalDate();		
	}

	@Transient
	@JsonView(DonationLogSummaryView.Basic.class)
	public State getMatchedState() {
		return matchedState;
	}
	
	public void setMatchedState(State matchedState) {
		this.matchedState = matchedState;
	}

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(DonationLog oo) {
		if (equals(oo))
			return 0;

		return new CompareToBuilder().append(getFacility(), oo.getFacility())
				.append(getTransactionDate(), oo.getTransactionDate()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(DonationLog oo) {
		return new EqualsBuilder().append(getTrackingId(), oo.getTrackingId())
				.append(getTransactionDate(), oo.getTransactionDate())
				.append(nullSafeGetDoubleValue(getDonationAmount()), nullSafeGetDoubleValue(oo.getDonationAmount()))
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getTrackingId()).append(getTransactionDate())
				.append(nullSafeGetDoubleValue(getDonationAmount())).toHashCode();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 30, nullable = false)
	public String getTrackingId() {
		return trackingId;
	}

	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}

	@Column(nullable = false)
	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}
	
	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}

	@Column(nullable = false, length = 10)
	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	@Column(nullable = false, length = 255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 255)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(length = 255)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(length = 30)
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(length = 10)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(length = 255)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(length = 30)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(nullable = false, scale = 8, precision = 2)
	public BigDecimal getDonationAmount() {
		return donationAmount;
	}

	public void setDonationAmount(BigDecimal donationAmount) {
		this.donationAmount = donationAmount;
	}

	@Column(length = 4000)
	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	@Column(length = 15)
	public String getProgramField() {
		return programField;
	}

	public void setProgramField(String programField) {
		this.programField = programField;
	}

	@Column(length = 50)
	public String getDepositNumber() {
		return depositNumber;
	}

	public void setDepositNumber(String depositNumber) {
		this.depositNumber = depositNumber;
	}

	@Transient
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(length = 20, nullable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(nullable = false, name = "CREATED_DATE_UTC")
	public ZonedDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(ZonedDateTime createdDate) {
		this.createdDate = createdDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationLogFileFK")
	@JsonIgnore
	public DonationLogFile getDonationLogFile() {
		return donationLogFile;
	}

	public void setDonationLogFile(DonationLogFile donationLogFile) {
		this.donationLogFile = donationLogFile;
	}
	
	

}
