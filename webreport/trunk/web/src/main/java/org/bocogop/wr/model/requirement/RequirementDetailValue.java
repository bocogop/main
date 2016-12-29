package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "VolunteerRequirementDetail", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class RequirementDetailValue extends AbstractAuditedVersionedPersistent<RequirementDetailValue> {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	private VolunteerRequirement volunteerRequirement;
	private RequirementDetailField field;
	private String value;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(RequirementDetailValue oo) {
		/*
		 * doubt all three of these are necessary but don't have data to confirm
		 * yet - CPB
		 */
		return new EqualsBuilder()
				.append(nullSafeGetId(getVolunteerRequirement()), nullSafeGetId(oo.getVolunteerRequirement()))
				.append(nullSafeGetId(getField()), nullSafeGetId(oo.getField())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteerRequirement())).append(nullSafeGetId(getField()))
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RequirementDetailFieldFK", nullable = false)
	public RequirementDetailField getField() {
		return field;
	}

	public void setField(RequirementDetailField field) {
		this.field = field;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VolunteerRequirementFK", nullable = false)
	public VolunteerRequirement getVolunteerRequirement() {
		return volunteerRequirement;
	}

	public void setVolunteerRequirement(VolunteerRequirement volunteerRequirement) {
		this.volunteerRequirement = volunteerRequirement;
	}

	@Column(length = 250)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
