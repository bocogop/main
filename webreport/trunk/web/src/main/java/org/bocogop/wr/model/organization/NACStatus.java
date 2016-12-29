package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Immutable
@Table(name = "NACStatuses", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class NACStatus extends AbstractAuditedVersionedPersistent<NACStatus> {
	private static final long serialVersionUID = -8678395783438462990L;

	@NotBlank
	private String membershipStatus;
	private String description;

	@Override
	protected boolean requiredEquals(NACStatus oo) {
		return new EqualsBuilder().append(membershipStatus, oo.getMembershipStatus()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(membershipStatus).toHashCode();
	}

	@Column(name = "MembershipStatus", length = 30, nullable = false)
	public String getMembershipStatus() {
		return membershipStatus;
	}

	public void setMembershipStatus(String abbreviation) {
		this.membershipStatus = abbreviation;
	}

	@Column(length = 800)
	public String getDescription() {
		return description;
	}

	public void setDescription(String vaCode) {
		this.description = vaCode;
	}

}
