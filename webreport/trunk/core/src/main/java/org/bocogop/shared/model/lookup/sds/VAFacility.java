package org.bocogop.shared.model.lookup.sds;

import java.time.ZonedDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.util.StationsUtil;

@Entity
@Table(name = "STD_INSTITUTION", schema = "SDSADM")
@AttributeOverrides({ @AttributeOverride(name = "version", column = @Column(name = "version")),
		@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy")),
		@AttributeOverride(name = "createdDate", column = @Column(name = "created")),
		@AttributeOverride(name = "modifiedBy", column = @Column(name = "updatedBy")),
		@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated")), })
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
// FIXWR convert to named query
// select l from org.bocogop.med.ciss.model.terminology.VAFacility l where
// l.active = true and
// (l.facilityType.code in ('CBOC', 'Dent', 'Dom', 'M&ROC', 'NHC', 'OC', 'OPC',
// 'ORC', 'VAMC', 'RO-OC')
// and l.facilityType.medicalTreating = true)) order by l.name asc
public class VAFacility extends AbstractAuditedVersionedPersistent<VAFacility> implements Comparable<VAFacility> {
	private static final long serialVersionUID = 4429854732234092653L;

	public static final String SDS_FACILITY_TYPE_VISN = "VISN";

	public static String getDisplayName(String name, String stationNumber) {
		return name + " (#" + stationNumber + ")";
	}

	// --------------------------------------- Fields

	private String name;
	private String stationNumber;
	private VAFacilityType facilityType;
	private VAFacility parent;
	private VAFacility visn;
	private boolean mfnzegRecipient;

	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String city;
	private State state;
	private String zip;

	private ZonedDateTime activationDate;
	private ZonedDateTime deactivationDate;

	// --------------------------------------- Business Methods

	@Transient
	public boolean isActive() {
		return getDeactivationDate() == null
				|| (getActivationDate() != null && getActivationDate().isAfter(getDeactivationDate()));
	}

	@Transient
	@JsonProperty
	public String getSta3n() {
		String sn = getStationNumber();
		return sn == null ? null : StationsUtil.getThreeDigitStationNumber(sn);
	}

	@JsonProperty
	@Transient
	public String getDisplayName() {
		// String name = WordUtils.capitalizeFully(getName());
		String name = getName();

		if (name == null)
			return "(Unknown)";

		String stationNumber = getStationNumber();
		if (stationNumber == null)
			return name;

		/*
		 * If we are already including the three-digit station number at the end
		 * of the name, no need to append it again, so just return the name -
		 * CPB
		 */
		// Matcher matcher = NAME_INCLUDING_STATION_IN_PARENS.matcher(name);
		if (name.contains(stationNumber))
			return name;

		return getDisplayName(name, stationNumber);
	}

	@Transient
	public boolean isVISN() {
		VAFacilityType ft = getFacilityType();
		if (ft == null)
			return false;

		return SDS_FACILITY_TYPE_VISN.equals(ft.getCode());
	}

	@Transient
	public boolean isCentralOffice() {
		return VAFacilityValue.CENTRAL_OFFICE.getId() == getId();
	}

	// --------------------------------------- Common Methods

	private String expandVISNNameForSorting(String name) {
		try {
			if (name != null && name.startsWith("VISN ")) {
				String num = name.substring("VISN ".length());
				name = "VISN " + ("000" + num).substring(num.length());
			}
		} catch (Exception e) {
		}
		return name;
	}

	@Override
	public int compareTo(VAFacility o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(expandVISNNameForSorting(getName()), expandVISNNameForSorting(o.getName()))
				.append(getStationNumber(), o.getStationNumber()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(VAFacility o) {
		return new EqualsBuilder().append(getStationNumber(), o.getStationNumber()).append(getName(), o.getName())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getStationNumber()).append(getName()).toHashCode();
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	// --------------------------------------- Accessor Methods

	@Column(name = "mfn_zeg_recipient")
	public boolean getMfnzegRecipient() {
		return mfnzegRecipient;
	}

	public void setMfnzegRecipient(boolean mfnzegRecipient) {
		this.mfnzegRecipient = mfnzegRecipient;
	}

	public ZonedDateTime getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(ZonedDateTime activationDate) {
		this.activationDate = activationDate;
	}

	public ZonedDateTime getDeactivationDate() {
		return deactivationDate;
	}

	public void setDeactivationDate(ZonedDateTime deactivationDate) {
		this.deactivationDate = deactivationDate;
	}

	@Column(length = 80)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "facilityType_id")
	public VAFacilityType getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(VAFacilityType facilityType) {
		this.facilityType = facilityType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@BatchSize(size = 500)
	public VAFacility getParent() {
		return parent;
	}

	public void setParent(VAFacility parent) {
		this.parent = parent;
	}

	@Column(name = "stationNumber")
	@JsonProperty
	public String getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(String stationNumber) {
		this.stationNumber = stationNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visn_id")
	@BatchSize(size = 500)
	public VAFacility getVisn() {
		return visn;
	}

	public void setVisn(VAFacility visn) {
		this.visn = visn;
	}

	@Column(name = "streetAddressLine1", length = 64)
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "streetAddressLine2", length = 64)
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "streetAddressLine3", length = 64)
	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Column(name = "streetCity", length = 50)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "streetState_id")
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Column(name = "streetPostalCode", length = 10)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public static enum VAFacilityValue implements LookupType {
		/*
		 * Add more here, sorted by code, if they are needed in application
		 * logic - CPB
		 */
		CENTRAL_OFFICE(1000001, "CENTRAL OFFICE", "101"), //
		CHEYENNE(1000098, "CHEYENNE VAMC", "442"), //
		;

		private long id;
		private String name;
		private String sta3n;

		private VAFacilityValue(long id, String name, String sta3n) {
			this.id = id;
			this.name = name;
			this.sta3n = sta3n;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getSta3n() {
			return sta3n;
		}

	}

}
