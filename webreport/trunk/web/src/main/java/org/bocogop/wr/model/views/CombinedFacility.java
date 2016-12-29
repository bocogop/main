package org.bocogop.wr.model.views;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.wr.model.facility.AbstractLocation;
import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.model.facility.Facility.FacilityView;

@Entity
@Immutable
// leaving this class as an example of a View, uncomment this if used later -
// CPB
// @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Table(name = "CombinedInstitutions", schema = "dbo")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class CombinedFacility extends AbstractLocation {
	private static final long serialVersionUID = 3254887002195956807L;

	// --------------------------------------------- Fields

	@NotNull
	private String stationNumber;
	private FacilityType type;

	private VAFacility vaFacility;

	// ---------------------------------------- Business Methods

	@Transient
	public String getDisplayName() {
		VAFacility facility = getVaFacility();
		if (facility == null || StringUtils.isNotBlank(getName())) {
			String stationNumber = getStationNumber();
			boolean hasStationNumber = StringUtils.isNotBlank(stationNumber);
			return getName() + (hasStationNumber ? " (" + getStationNumber() + ")" : "");
		}
		return getVaFacility().getDisplayName();
	}

	@Transient
	public String getDisplayNameAbbreviated() {
		return StringUtils.abbreviateMiddle(getDisplayName(), "..", 50);
	}
	
	@Override
	@Transient
	@JsonView(FacilityView.Extended.class)
	public String getScale() {
		return "Facility";
	}

	// ---------------------------------------- Common Methods

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	@Override
	protected boolean requiredEquals(AbstractLocation o) {
		return new EqualsBuilder().append(getId(), o.getId()).isEquals();
	}

	// --------------------------------------------- Accessor Methods

	@Column(length = 7, nullable = false)
	@JsonView(FacilityView.Basic.class)
	public String getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(String stationNumber) {
		this.stationNumber = stationNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityTypeFK", nullable = false)
	@BatchSize(size = 500)
	@JsonView(FacilityView.Extended.class)
	public FacilityType getType() {
		return type;
	}

	public void setType(FacilityType type) {
		this.type = type;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_InstitutionFK")
	@BatchSize(size = 500)
	@JsonIgnore
	public VAFacility getVaFacility() {
		return vaFacility;
	}

	public void setVaFacility(VAFacility facility) {
		this.vaFacility = facility;
	}

}
