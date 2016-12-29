package org.bocogop.wr.model.award;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.persistence.conversion.AwardTypeConverter;

@Entity
@Table(name = "VolunteerAwards", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Award extends AbstractAuditedVersionedPersistent<Award> implements Comparable<Award> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class AwardCodeView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	private String name;
	private String code;
	private Integer hoursRequired;
	private AwardType type;
	private Integer awardHours;
	private boolean inactive;
	
	// -------------------------------------- Business Methods

	@Transient
	public String getDisplayName() {
		return name;
	}
	
	@Transient
	public String getDisplayNameAbbreviated() {
		return StringUtils.abbreviate(getDisplayName(), 35);
	}
	
	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Award oo) {
		/*
		 * doubt all three of these are necessary but don't have data to confirm
		 * yet - CPB
		 */
		return new EqualsBuilder().append(code, oo.getCode()).append(name, oo.getName()).append(type, oo.getType())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(code).append(name).append(type).toHashCode();
	}

	@Override
	public int compareTo(Award o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(name, o.getName()).append(hoursRequired, o.getHoursRequired())
				.toComparison() > 0 ? 1 : -1;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "AwardName", length = 45, nullable = false)
	@JsonView(AwardCodeView.Basic.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "AwardCode", length = 2, nullable = false)
	@JsonView(AwardCodeView.Basic.class)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "RequiredHours", nullable = false)
	@JsonView(AwardCodeView.Basic.class)
	public Integer getHoursRequired() {
		return hoursRequired;
	}

	public void setHoursRequired(Integer hoursRequired) {
		this.hoursRequired = hoursRequired;
	}

	@Column(name = "AwardType", nullable = false)
	@Convert(converter = AwardTypeConverter.class)
	@JsonView(AwardCodeView.Basic.class)
	public AwardType getType() {
		return type;
	}

	public void setType(AwardType type) {
		this.type = type;
	}

	@Column(nullable = false)
	@JsonView(AwardCodeView.Basic.class)
	public Integer getAwardHours() {
		return awardHours;
	}

	public void setAwardHours(Integer awardHours) {
		this.awardHours = awardHours;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(AwardCodeView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}
}
