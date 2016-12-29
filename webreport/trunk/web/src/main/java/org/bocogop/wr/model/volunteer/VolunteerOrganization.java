package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.AbstractBasicOrganization.OrganizationView;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;
import org.bocogop.wr.model.volunteer.VolunteerOrganization.VolunteerOrganizationView.Basic;
import org.bocogop.wr.model.volunteer.VolunteerOrganization.VolunteerOrganizationView.SearchForOrganizations;
import org.bocogop.wr.model.volunteer.VolunteerOrganization.VolunteerOrganizationView.SearchForVolunteers;

@Entity
@Table(name = "VolunteerOrganizations", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VolunteerOrganization extends AbstractAuditedVersionedPersistent<VolunteerOrganization> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class VolunteerOrganizationView {
		public interface Basic {
		}

		public interface SearchForOrganizations extends Basic, OrganizationView.Search {
		}

		public interface SearchForVolunteers extends Basic, VolunteerView.Search {
		}
	}

	public static final class CompareByOrganization implements Comparator<VolunteerOrganization> {
		@Override
		public int compare(VolunteerOrganization o1, VolunteerOrganization o2) {
			return new CompareToBuilder().append(o1.getOrganization(), o2.getOrganization()).toComparison();
		}
	}

	// -------------------------------------- Fields

	private AbstractBasicOrganization organization;
	private Volunteer volunteer;

	private boolean inactive;

	// -------------------------------------- Constructors

	public VolunteerOrganization() {
	}

	public VolunteerOrganization(Volunteer v, AbstractBasicOrganization o) {
		setVolunteer(v);
		setOrganization(o);
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView({ Basic.class, KioskAssignmentsAndOrgsView.Combined.class })
	public boolean isActive() {
		return !inactive;
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(VolunteerOrganization oo) {
		return new EqualsBuilder().append(nullSafeGetId(getOrganization()), nullSafeGetId(oo.getOrganization()))
				.append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getOrganization())).append(nullSafeGetId(getVolunteer()))
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationFK")
	@BatchSize(size = 500)
	@JsonView({ SearchForOrganizations.class, KioskAssignmentsAndOrgsView.Combined.class })
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK")
	@BatchSize(size = 500)
	@JsonView(SearchForVolunteers.class)
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonIgnore
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

}
