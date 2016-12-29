package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.printing.PrintRequest;

@Entity
@Table(name = "Kiosk", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Kiosk extends AbstractAuditedVersionedPersistent<Kiosk> implements Comparable<Kiosk> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class KioskView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	public static class KioskAssignmentsAndOrgsView {
		public interface Combined {
		}
	}

	// ---------------------------------------- Fields

	@NotBlank
	private String location;
	@NotNull
	private Facility facility;

	private String lastPrinterError;
	private ZonedDateTime lastPrinterStatusCheck;
	private List<PrintRequest> printRequests;
	private boolean registered;

	// ---------------------------------------- Business Methods

	public boolean isPrinterOnline(int maxQuietPrinterStatusCheckMinutes) {
		return lastPrinterStatusCheck != null
				&& lastPrinterStatusCheck.isAfter(ZonedDateTime.now().minusMinutes(maxQuietPrinterStatusCheckMinutes));
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Kiosk oo) {
		return new EqualsBuilder().append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getLocation(), oo.getLocation()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getFacility())).append(getLocation()).toHashCode();
	}

	@Override
	public int compareTo(Kiosk o) {
		if (equals(o))
			return 0;

		return getLocation().compareToIgnoreCase(o.getLocation()) > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(nullable = false, length = 50)
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	public String getLastPrinterError() {
		return lastPrinterError;
	}

	public void setLastPrinterError(String lastPrinterError) {
		this.lastPrinterError = lastPrinterError;
	}

	public ZonedDateTime getLastPrinterStatusCheck() {
		return lastPrinterStatusCheck;
	}

	public void setLastPrinterStatusCheck(ZonedDateTime lastPrinterStatusCheck) {
		this.lastPrinterStatusCheck = lastPrinterStatusCheck;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "kiosk")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<PrintRequest> getPrintRequests() {
		return printRequests;
	}

	public void setPrintRequests(List<PrintRequest> printRequests) {
		this.printRequests = printRequests;
	}

	@Column(name = "IsRegistered", nullable = false)
	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

}
