package org.bocogop.wr.model.views;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.bocogop.shared.model.core.AbstractDerivedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;

@Entity
@Immutable
// leaving this class as an example of a View, uncomment this if used later - CPB
// @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Table(name = "FACILITIES_AND_VISNS", schema = "CORE")
public class FacilityAndVisn extends AbstractDerivedPersistent<FacilityAndVisn> {
	private static final long serialVersionUID = 3254887002195956807L;

	// --------------------------------------------- Fields

	private Long id;

	private VAFacility parentFacility;

	private String facilityName;
	private String facilityStationNumber;
	private String facilityTypeCode;
	private boolean facilityMedicalTreating;
	private VAFacility visn;
	private String visnName;

	// ---------------------------------------- Common Methods

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	@Override
	protected boolean requiredEquals(FacilityAndVisn o) {
		return new EqualsBuilder().append(getId(), o.getId()).isEquals();
	}

	// --------------------------------------------- Accessor Methods

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	public VAFacility getParentFacility() {
		return parentFacility;
	}

	public void setParentFacility(VAFacility parentFacility) {
		this.parentFacility = parentFacility;
	}

	@Column(name = "name")
	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String institutionName) {
		this.facilityName = institutionName;
	}

	@Column(name = "stationNumber")
	public String getFacilityStationNumber() {
		return facilityStationNumber;
	}

	public void setFacilityStationNumber(String institutionStationNumber) {
		this.facilityStationNumber = institutionStationNumber;
	}

	@Column(name = "institutionTypeCode")
	public String getFacilityTypeCode() {
		return facilityTypeCode;
	}

	public void setFacilityTypeCode(String institutionTypeCode) {
		this.facilityTypeCode = institutionTypeCode;
	}

	@Type(type = "numeric_boolean")
	@Column(name = "isMedicalTreating")
	public boolean isFacilityMedicalTreating() {
		return facilityMedicalTreating;
	}

	public void setFacilityMedicalTreating(boolean institutionMedicalTreating) {
		this.facilityMedicalTreating = institutionMedicalTreating;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "visn_id")
	public VAFacility getVisn() {
		return visn;
	}

	@Column(name = "visnName")
	public void setVisn(VAFacility visn) {
		this.visn = visn;
	}

	public String getVisnName() {
		return visnName;
	}

	public void setVisnName(String visnName) {
		this.visnName = visnName;
	}

}
