package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.time.WorkEntry.WorkEntryView;
import org.bocogop.wr.persistence.conversion.OrganizationScopeTypeConverter;

@Entity
@DiscriminatorValue("B")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class OrganizationBranch extends AbstractBasicOrganization {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class OrganizationBranchView {
		public interface Basic extends OrganizationView.Extended {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private AbstractBasicOrganization organization;

	// -------------------------------------- Business Methods

	@Transient
	@JsonIgnore
	public Organization getRootOrganization() {
		return (Organization) PersistenceUtil.initializeAndUnproxy(getOrganization());
	}

	@Override
	@Transient
	@JsonView(OrganizationView.Search.class)
	public String getScale() {
		return "Branch";
	}

	@Transient
	@JsonView({ //
			DonationSummaryView.Search.class, //
			KioskAssignmentsAndOrgsView.Combined.class, //
			OccasionalWorkEntryView.TimeReport.class, //
			OrganizationView.Basic.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class, //
	})
	public String getDisplayName() {
		return getOrganization().getDisplayName() + " - " + getName();
	}

	@Override
	@Transient
	public String getFullName() {
		return getName();
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AbstractBasicOrganization oo) {
		OrganizationBranch o = (OrganizationBranch) PersistenceUtil.initializeAndUnproxy(oo);
		return new EqualsBuilder().append(getName(), oo.getName())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(nullSafeGetId(getOrganization()), nullSafeGetId(o.getOrganization())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(nullSafeGetId(getFacility()))
				.append(nullSafeGetId(getOrganization())).toHashCode();
	}

	public String toString() {
		return getName() + " (organization ID " + nullSafeGetId(getOrganization()) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ParentFK")
	@JsonIgnore
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@Override
	@Convert(converter = OrganizationScopeTypeConverter.class)
	public ScopeType getScope() {
		return ScopeType.LOCAL;
	}

	@SuppressWarnings("unused")
	private void setScope(ScopeType scopeType) {
		;
	}

}
