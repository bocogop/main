package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "RequirementDetailField", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class RequirementDetailField extends AbstractAuditedVersionedPersistent<RequirementDetailField>
		implements Comparable<RequirementDetailField> {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	private AbstractRequirement requirement;
	private String name;
	private boolean inactive;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(RequirementDetailField oo) {
		/*
		 * doubt all three of these are necessary but don't have data to confirm
		 * yet - CPB
		 */
		return new EqualsBuilder().append(name, oo.getName())
				.append(nullSafeGetId(getRequirement()), nullSafeGetId(oo.getRequirement())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(name).append(nullSafeGetId(getRequirement())).toHashCode();
	}

	@Override
	public int compareTo(RequirementDetailField o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(name, o.getName()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return getName();
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "Name", length = 50, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RequirementFK", nullable = false)
	public AbstractRequirement getRequirement() {
		return requirement;
	}

	public void setRequirement(AbstractRequirement requirement) {
		this.requirement = requirement;
	}

	@Column(name = "IsInactive", nullable = false)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

}
