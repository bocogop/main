package org.bocogop.wr.model.volunteer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;

@Entity
@Table(name = "VolunteerAvailableIdentifyingCodes", schema = "wr")
public class AvailableIdentifyingCode extends AbstractIdentifiedPersistent<AvailableIdentifyingCode> {

	private static final long serialVersionUID = -5062512168142239943L;

	private String code;

	@Override
	protected boolean requiredEquals(AvailableIdentifyingCode oo) {
		return new EqualsBuilder().append(getCode(), oo.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}

	@Column(name = "value", length = 6)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
