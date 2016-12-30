package org.bocogop.wr.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "Holidays", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Holiday extends AbstractAuditedVersionedPersistent<Holiday> implements Comparable<Holiday> {

	private static final long serialVersionUID = -8719706544473483784L;

	// -------------------------------------- Fields

	private LocalDate observanceDate;
	private String holidayName;
	private boolean federalHoliday;

	// -------------------------------------- Business Methods

	@Transient
	public String getDisplayName() {
		return getHolidayName();
	}

	// -------------------------------------- Common Methods

	@Override
	public int compareTo(Holiday o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getObservanceDate(), o.getObservanceDate())
				.append(getDisplayName(), o.getDisplayName()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(Holiday oo) {
		return new EqualsBuilder().append(getObservanceDate(), oo.getObservanceDate())
				.append(getHolidayName(), oo.getHolidayName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getObservanceDate()).append(getHolidayName()).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@Column(nullable = false)
	public LocalDate getObservanceDate() {
		return observanceDate;
	}

	public void setObservanceDate(LocalDate observanceDate) {
		this.observanceDate = observanceDate;
	}

	@Column(length = 40, nullable = false)
	public String getHolidayName() {
		return holidayName;
	}

	public void setHolidayName(String holidayName) {
		this.holidayName = holidayName;
	}

	@Column(name = "IsFederalHoliday", nullable = false)
	public boolean isFederalHoliday() {
		return federalHoliday;
	}

	public void setFederalHoliday(boolean federalHoliday) {
		this.federalHoliday = federalHoliday;
	}

}
