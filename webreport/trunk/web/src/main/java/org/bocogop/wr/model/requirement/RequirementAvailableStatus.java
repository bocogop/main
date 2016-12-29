package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;

@Entity
@Table(name = "RequirementAvailableStatus", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class RequirementAvailableStatus extends AbstractAuditedPersistent<RequirementAvailableStatus> {
	private static final long serialVersionUID = 583796042812902141L;

	// -------------------------------------- Fields

	private AbstractRequirement requirement;
	private RequirementStatus status;

	// -------------------------------------- Constructors

	public RequirementAvailableStatus() {
	}

	public RequirementAvailableStatus(AbstractRequirement requirement, RequirementStatus status) {
		this.requirement = requirement;
		this.status = status;
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(RequirementAvailableStatus oo) {
		return new EqualsBuilder().append(nullSafeGetId(getStatus()), nullSafeGetId(oo.getStatus()))
				.append(nullSafeGetId(getRequirement()), nullSafeGetId(oo.getRequirement())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getStatus())).append(nullSafeGetId(getRequirement()))
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_RequirementStatusFK", nullable = false)
	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RequirementFK", nullable = false)
	@JsonIgnore
	public AbstractRequirement getRequirement() {
		return requirement;
	}

	public void setRequirement(AbstractRequirement requirement) {
		this.requirement = requirement;
	}

}
