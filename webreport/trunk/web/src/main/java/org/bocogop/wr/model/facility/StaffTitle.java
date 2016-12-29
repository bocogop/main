package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;


import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.requirement.AbstractRequirement.RequirementView;

@Entity
@Table(name = "StaffTitles", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
@AttributeOverride(name = "name", column = @Column(name = "title"))
public class StaffTitle extends AbstractAuditedVersionedPersistent<StaffTitle> implements Comparable<StaffTitle> {

	private static final long serialVersionUID = 9103380479149326407L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class StaffTitleView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	private String name;
	private boolean chiefSupervisor;
	private String description;
	private Facility facility;
	private boolean chief;
	private boolean inactive;
	
	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(StaffTitle oo) {
		return new EqualsBuilder().append(getName(), oo.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).toHashCode();
	}

	@Override
	@JsonView(RequirementView.Basic.class)
	public int compareTo(StaffTitle o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName())).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 50, nullable = false, name = "Title")
	@JsonView(StaffTitleView.Basic.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "IsVolChiefSupervisor")
	@JsonView(StaffTitleView.Basic.class)
	public boolean isChiefSupervisor() {
		return chiefSupervisor;
	}

	public void setChiefSupervisor(boolean chiefSupervisor) {
		this.chiefSupervisor = chiefSupervisor;
	}

	@Column(length = 250)
	@JsonView(StaffTitleView.Basic.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", unique = true)
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Column(name = "IsChief", nullable = false)
	@JsonView(StaffTitleView.Basic.class)
	public boolean isChief() {
		return chief;
	}

	public void setChief(boolean chief) {
		this.chief = chief;
	}
	
	@Column(name = "IsInactive", nullable = false)
	@JsonView(StaffTitleView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

}
