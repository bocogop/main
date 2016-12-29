package org.bocogop.shared.model.lookup.sds;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.model.lookup.LookupType;

@Entity
@Immutable
@Table(name = "STD_FacilityType", schema = "SDSADM")
@AttributeOverrides({ @AttributeOverride(name = "version", column = @Column(name = "version") ),
		@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy") ),
		@AttributeOverride(name = "createdDate", column = @Column(name = "created") ),
		@AttributeOverride(name = "modifiedBy", column = @Column(name = "updatedBy") ),
		@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated") ) })
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class VAFacilityType extends AbstractAuditedPersistent<VAFacilityType> {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VAFacilityType.class);
	private static final long serialVersionUID = -1624129088703368361L;

	// --------------------------------- Fields

	private boolean medicalTreating;
	private String code;
	private String name;
	private String description;
	private List<VAFacility> facilityList;

	// --------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(VAFacilityType o) {
		return new EqualsBuilder().append(getName(), o.getName()).append(getCode(), o.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(getCode()).toHashCode();
	}

	// --------------------------------- Accessor Methods

	@Type(type = "numeric_boolean")
	@Column(name = "isMedicalTreating")
	public boolean isMedicalTreating() {
		return medicalTreating;
	}

	public void setMedicalTreating(boolean medicalTreating) {
		this.medicalTreating = medicalTreating;
	}

	@Column(length = 11)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(length = 45)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 60)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@OneToMany(mappedBy = "facilityType", fetch = FetchType.LAZY)
	public List<VAFacility> getFacilityList() {
		if (facilityList == null)
			facilityList = new ArrayList<>();
		return facilityList;
	}

	public void setFacilityList(List<VAFacility> facilityList) {
		this.facilityList = facilityList;
	}

	public static enum VAFacilityTypeValue implements LookupType {
		/*
		 * Add more here, sorted by code, if they are needed in application
		 * logic - CPB
		 */
		CBOC(1009148, "COMMUNITY BASED OUTPATIENT CLINIC"), //
		;

		private long id;
		private String name;

		private VAFacilityTypeValue(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}
}
