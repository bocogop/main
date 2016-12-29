package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.model.requirement.FacilityRequirement;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;

@Entity
@DiscriminatorValue("F")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Facility extends AbstractUpdateableLocation<Facility> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static enum FacilityValue implements LookupType {
		CENTRAL_OFFICE(2);

		private long id;

		private FacilityValue(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}
	}

	public static class FacilityView {
		public interface Basic {
		}

		public interface BasicWithScope extends Basic {
		}

		public interface Extended extends Basic {
		}

		public interface ShowHierarchy extends Basic, BasicLocationView.ShowHierarchy {
		}
	}

	/**
	 * Factory method to create a new Facility which reproduces all
	 * trigger-based logic in the DB (creating related entries) - CPB
	 * 
	 * @param facility
	 * @param state
	 * @param type
	 * @param parent
	 * @return
	 */
	public static Facility createNew(VAFacility facility, State state, FacilityType type, Facility parent,
			ZoneId timeZone) {
		Facility newInstitution = new Facility(facility, state, type, null, timeZone);
		newInstitution.populateMissingRequiredFields();
		return newInstitution;
	}

	// ---------------------------------------- Fields

	@NotBlank(message = "Please enter a station number.")
	private String stationNumber;
	private FacilityType type;

	private VAFacility vaFacility;

	/*
	 * Only contains one item, lazy-loaded; mapping like this instead
	 * of @OneToOne so it can be optional and still lazy-load - CPB
	 */
	private List<StationParameters> stationParametersList;

	/*
	 * Only contains one item, lazy-loaded; mapping like this instead
	 * of @OneToOne so it can be optional and still lazy-load - CPB
	 */
	private List<VoluntaryServiceParameters> voluntaryServiceParametersList;

	private SortedSet<DonGenPostFund> donGenPostFunds;

	private SortedSet<DonationReference> donationReferences;

	private List<FacilityRequirement> requirements;

	private List<Kiosk> kiosks;

	@NotNull
	private ZoneId timeZone;

	// ---------------------------------------- Constructors

	public Facility() {
	}

	private Facility(VAFacility facility, State state, FacilityType type, Facility parent, ZoneId timeZone) {
		super(facility.getName(), facility.getAddressLine1(), facility.getAddressLine2(), facility.getCity(), state,
				facility.getZip(), parent);
		this.stationNumber = facility.getStationNumber();
		this.type = type;
		this.vaFacility = facility;
		this.timeZone = timeZone;
	}

	// ---------------------------------------- Business Methods

	public void initializeFacility() {
		getType().getDescription();
		VAFacility vaf = getVaFacility();
		if (vaf != null)
			vaf.getName();
		StationParameters sp = getStationParameters();
		if (sp != null)
			sp.getAlternateLanguage();
		VoluntaryServiceParameters vsp = getVoluntaryServiceParameters();
		if (vsp != null)
			vsp.getChiefManager();
		for (DonGenPostFund f : getDonGenPostFunds())
			f.getGeneralPostFund();
		for (DonationReference dr : getDonationReferences())
			dr.getDonationReference();
		for (FacilityRequirement fr : getRequirements())
			fr.getDescription();
		for (Kiosk k : getKiosks())
			k.getLastPrinterError();
	}

	@Override
	@Transient
	public Facility getFacility() {
		return this;
	}

	@Override
	@Transient
	@JsonView(FacilityView.BasicWithScope.class)
	public String getScale() {
		return "Facility";
	}

	@Transient
	public String getDisplayName() {
		if (StringUtils.isNotBlank(getName()) || getVaFacility() == null)
			return getName() + " (" + getStationNumber() + ")";

		return getVaFacility().getDisplayName();
	}

	@Transient
	@JsonView(FacilityView.Extended.class)
	public String getDisplayNameAbbreviated() {
		return StringUtils.abbreviateMiddle(getDisplayName(), "..", 50);
	}

	@Transient
	@JsonView(FacilityView.Extended.class)
	public boolean isCentralOffice() {
		return FacilityValue.CENTRAL_OFFICE.getId() == getId();
	}

	@Transient
	@JsonView(FacilityView.Extended.class)
	public boolean isLinkedToVAFacility() {
		return getVaFacility() != null;
	}

	@Transient
	@JsonIgnore
	public StationParameters getStationParameters() {
		List<StationParameters> l = getStationParametersList();
		return l.isEmpty() ? null : l.get(0);
	}

	public void setStationParameters(StationParameters stationParameters) {
		getStationParametersList().clear();
		if (stationParameters != null) {
			getStationParametersList().add(stationParameters);
			stationParameters.setFacility(this);
		}
	}

	@Transient
	@JsonIgnore
	public VoluntaryServiceParameters getVoluntaryServiceParameters() {
		List<VoluntaryServiceParameters> l = getVoluntaryServiceParametersList();
		return l.isEmpty() ? null : l.get(0);
	}

	public void setVoluntaryServiceParameters(VoluntaryServiceParameters vsp) {
		getVoluntaryServiceParametersList().clear();
		if (vsp != null) {
			getVoluntaryServiceParametersList().add(vsp);
			vsp.setFacility(this);
		}
	}

	private boolean populateMissingRequiredFields() {
		boolean dataChanged = false;

		StationParameters sp = getStationParameters();
		if (sp == null) {
			sp = new StationParameters(this);
			setStationParameters(sp);
			dataChanged = true;
		}

		VoluntaryServiceParameters vsp = getVoluntaryServiceParameters();
		if (vsp == null) {
			vsp = new VoluntaryServiceParameters(StringUtils.left(getName() + " Voluntary Service", 120));
			setVoluntaryServiceParameters(vsp);
			dataChanged = true;
		}

		return dataChanged;
	}

	public List<DonationReference> getDonationReferencesByStatus(boolean isActive) {
		return getDonationReferences().stream().filter(p -> p.isActive() == isActive).collect(Collectors.toList());
	}

	public List<DonGenPostFund> getDonGenPostfundsByStatus(boolean isActive) {
		return getDonGenPostFunds().stream().filter(p -> p.isActive() == isActive).collect(Collectors.toList());
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AbstractLocation oo) {
		/*
		 * Safe cast due to AbstractBusinessKeyOwner logic; since we need a
		 * field in the concrete class, we need to ensure we aren't trying to
		 * cast a proxy, which would fail - CPB
		 */
		Facility f = (Facility) PersistenceUtil.initializeAndUnproxy(oo);
		return new EqualsBuilder().append(getStationNumber(), f.getStationNumber()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getStationNumber()).toHashCode();
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	// ---------------------------------------- Accessor Methods

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	@JsonIgnore
	private List<StationParameters> getStationParametersList() {
		if (stationParametersList == null)
			stationParametersList = new ArrayList<>();
		return stationParametersList;
	}

	@SuppressWarnings("unused")
	private void setStationParametersList(List<StationParameters> stationParameters) {
		this.stationParametersList = stationParameters;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	private List<VoluntaryServiceParameters> getVoluntaryServiceParametersList() {
		if (voluntaryServiceParametersList == null)
			voluntaryServiceParametersList = new ArrayList<>();
		return voluntaryServiceParametersList;
	}

	@SuppressWarnings("unused")
	private void setVoluntaryServiceParametersList(List<VoluntaryServiceParameters> voluntaryServiceParametersList) {
		this.voluntaryServiceParametersList = voluntaryServiceParametersList;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	@JsonIgnore
	@SortNatural
	public SortedSet<DonGenPostFund> getDonGenPostFunds() {
		if (donGenPostFunds == null)
			donGenPostFunds = new TreeSet<>();
		return donGenPostFunds;
	}

	public void setDonGenPostFunds(SortedSet<DonGenPostFund> donGenPostFunds) {
		this.donGenPostFunds = donGenPostFunds;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	@JsonIgnore
	@SortNatural
	public SortedSet<DonationReference> getDonationReferences() {
		if (donationReferences == null)
			donationReferences = new TreeSet<>();
		return donationReferences;
	}

	public void setDonationReferences(SortedSet<DonationReference> donationReferences) {
		this.donationReferences = donationReferences;
	}

	@Column(length = 7, nullable = false)
	@Override
	@JsonView(FacilityView.Basic.class)
	public String getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(String stationNumber) {
		this.stationNumber = stationNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityTypeFK", nullable = false)
	@BatchSize(size = 500)
	@JsonView(FacilityView.Extended.class)
	public FacilityType getType() {
		return type;
	}

	public void setType(FacilityType type) {
		this.type = type;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_InstitutionFK")
	@BatchSize(size = 500)
	@JsonIgnore
	public VAFacility getVaFacility() {
		return vaFacility;
	}

	public void setVaFacility(VAFacility facility) {
		this.vaFacility = facility;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<FacilityRequirement> getRequirements() {
		if (requirements == null)
			requirements = new ArrayList<>();
		return requirements;
	}

	public void setRequirements(List<FacilityRequirement> requirements) {
		this.requirements = requirements;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "facility")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<Kiosk> getKiosks() {
		if (kiosks == null)
			kiosks = new ArrayList<>();
		return kiosks;
	}

	public void setKiosks(List<Kiosk> kiosks) {
		this.kiosks = kiosks;
	}

	@Column(length = 50)
	public ZoneId getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}

}
