package org.bocogop.wr.model.leie;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.util.StringUtil;

@Entity
@Table(name = "ExcludedEntity", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ExcludedEntity extends AbstractAuditedPersistent<ExcludedEntity> implements Comparable<ExcludedEntity> {
	private static final long serialVersionUID = 6904844123870655771L;

	static final DateTimeFormatter SHORT_DOB_FORMATTER = new DateTimeFormatterBuilder().appendPattern("MM/dd/")
			.appendValueReduced(ChronoField.YEAR, 2, 2, 1900).toFormatter();
	static final DateTimeFormatter FULL_DOB_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	static final DateTimeFormatter FULL_MSD_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	// -------------------------------------- Fields

	private String lastName;
	private String firstName;
	private String middleName;
	private String businessName;
	private String general;
	private String specialty;
	private String upin;
	private String npi;
	private LocalDate dob;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String exclusionTypeCode;
	private LocalDate exclusionDate;
	private LocalDate reinstateDate;
	private LocalDate waiverDate;
	private String waiverState;

	private ExclusionType exclusionType;

	// -------------------------------------- Constructors

	public ExcludedEntity() {
	}

	public ExcludedEntity(Map<String, String> map) {
		this.lastName = nullOrStr(map.get("LASTNAME"));
		this.firstName = nullOrStr(map.get("FIRSTNAME"));
		this.middleName = nullOrStr(map.get("MIDNAME"));
		this.businessName = nullOrStr(map.get("BUSNAME"));
		this.general = nullOrStr(map.get("GENERAL"));
		this.specialty = nullOrStr(map.get("SPECIALTY"));
		this.upin = nullOrStr(map.get("UPIN"));
		this.npi = nullOrStr(map.get("NPI"));

		String dob = nullOrStr(map.get("DOB"));
		this.dob = dob == null ? null
				: LocalDate.parse(dob, dob.length() == 8 ? FULL_MSD_DATE_FORMATTER : FULL_DOB_FORMATTER);

		this.address = nullOrStr(map.get("ADDRESS"));
		this.city = nullOrStr(map.get("CITY"));
		this.state = nullOrStr(map.get("STATE"));
		this.zip = nullOrStr(map.get("ZIP"));
		this.exclusionTypeCode = nullOrStr(map.get("EXCLTYPE"));

		String exclusionDate = nullOrStr(map.get("EXCLDATE"));
		this.exclusionDate = exclusionDate == null || "".equals(exclusionDate.replace("0", "").trim()) ? null
				: LocalDate.parse(exclusionDate, FULL_MSD_DATE_FORMATTER);

		String reinstateDate = nullOrStr(map.get("REINDATE"));
		this.reinstateDate = reinstateDate == null || "".equals(reinstateDate.replace("0", "").trim()) ? null
				: LocalDate.parse(reinstateDate, FULL_MSD_DATE_FORMATTER);

		String waiverDate = nullOrStr(map.get("WAIVERDATE"));
		this.waiverDate = waiverDate == null || "".equals(waiverDate.replace("0", "").trim()) ? null
				: LocalDate.parse(waiverDate, FULL_MSD_DATE_FORMATTER);

		this.waiverState = nullOrStr(map.get("WVRSTATE"));
	}

	// -------------------------------------- Business Methods

	private String nullOrStr(Object o) {
		return o == null ? null : StringUtils.trimToNull(o.toString());
	}

	@Transient
	public String getDisplayName() {
		if (StringUtils.isNotEmpty(businessName))
			return businessName;
		return StringUtil.getDisplayName(true, firstName, middleName, lastName, null);
	}

	@Transient
	public String getAddressMultilineDisplay() {
		return getAddressDisplay(false);
	}

	private String getAddressDisplay(boolean useIdForState) {
		return StringUtil.getAddressDisplay(address, null, null, city, state, zip, "\n");
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(ExcludedEntity o) {
		/*
		 * Ugly but they don't provide a unique key and I see some very close
		 * similarities in the data - CPB
		 */
		return new EqualsBuilder().append(lastName, o.getLastName()).append(firstName, o.getFirstName())
				.append(middleName, o.getMiddleName()).append(businessName, o.getBusinessName())
				.append(general, o.getGeneral()).append(specialty, o.getSpecialty()).append(upin, o.getUpin())
				.append(npi, o.getNpi()).append(dob, o.getDob()).append(address, o.getAddress())
				.append(city, o.getCity()).append(state, o.getState()).append(zip, o.getZip())
				.append(exclusionTypeCode, o.getExclusionTypeCode()).append(exclusionDate, o.getExclusionDate())
				.append(reinstateDate, o.getReinstateDate()).append(waiverDate, o.getWaiverDate())
				.append(waiverState, o.getWaiverState()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(lastName).append(businessName).append(address).append(exclusionDate)
				.toHashCode();
	}

	@Override
	public int compareTo(ExcludedEntity o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDisplayName(), o.getDisplayName()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 20)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(length = 15)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(length = 15)
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Column(length = 30)
	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	@Column(length = 20)
	public String getGeneral() {
		return general;
	}

	public void setGeneral(String general) {
		this.general = general;
	}

	@Column(length = 20)
	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	@Column(length = 6)
	public String getUpin() {
		return upin;
	}

	public void setUpin(String upin) {
		this.upin = upin;
	}

	@Column(length = 10)
	public String getNpi() {
		return npi;
	}

	public void setNpi(String npi) {
		this.npi = npi;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	@Column(length = 30)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(length = 20)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(length = 2)
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(length = 5)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(name = "ExclusionType", length = 9)
	public String getExclusionTypeCode() {
		return exclusionTypeCode;
	}

	public void setExclusionTypeCode(String exclusionTypeCode) {
		this.exclusionTypeCode = exclusionTypeCode;
	}

	public LocalDate getExclusionDate() {
		return exclusionDate;
	}

	public void setExclusionDate(LocalDate exclusionDate) {
		this.exclusionDate = exclusionDate;
	}

	public LocalDate getReinstateDate() {
		return reinstateDate;
	}

	public void setReinstateDate(LocalDate reinstateDate) {
		this.reinstateDate = reinstateDate;
	}

	public LocalDate getWaiverDate() {
		return waiverDate;
	}

	public void setWaiverDate(LocalDate waiverDate) {
		this.waiverDate = waiverDate;
	}

	@Column(length = 2)
	public String getWaiverState() {
		return waiverState;
	}

	public void setWaiverState(String waiverState) {
		this.waiverState = waiverState;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ExclusionTypesFK")
	public ExclusionType getExclusionType() {
		return exclusionType;
	}

	public void setExclusionType(ExclusionType exclusionType) {
		this.exclusionType = exclusionType;
	}

}
