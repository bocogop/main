package org.bocogop.wr.model.printing;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.model.volunteer.Volunteer;

@Entity
@Table(name = "PrintRequest", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class PrintRequest extends AbstractAuditedVersionedPersistent<PrintRequest> implements Comparable<PrintRequest> {
	private static final long serialVersionUID = -8678395783438462990L;

	
	public static String getText(Volunteer volunteer, Facility facility, LocalDate today) {
		return "";
	}
	
	// ---------------------------------------- Fields

	@NotBlank
	private String printText;
	@NotNull
	private Kiosk kiosk;
	@NotNull
	private ZonedDateTime requestTime;
	private ZonedDateTime completionTime;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(PrintRequest oo) {
		return new EqualsBuilder().append(nullSafeGetId(getKiosk()), nullSafeGetId(oo.getKiosk()))
				.append(getRequestTime(), oo.getRequestTime()).append(getPrintText(), oo.getPrintText()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getKiosk())).append(getRequestTime()).toHashCode();
	}

	@Override
	public int compareTo(PrintRequest o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getRequestTime(), o.getRequestTime()).toComparison() > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	public String getPrintText() {
		return printText;
	}

	public void setPrintText(String printText) {
		this.printText = printText;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "KioskFK")
	@JsonIgnore
	public Kiosk getKiosk() {
		return kiosk;
	}

	public void setKiosk(Kiosk kiosk) {
		this.kiosk = kiosk;
	}

	public ZonedDateTime getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(ZonedDateTime requestTime) {
		this.requestTime = requestTime;
	}

	public ZonedDateTime getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(ZonedDateTime completionTime) {
		this.completionTime = completionTime;
	}

}
