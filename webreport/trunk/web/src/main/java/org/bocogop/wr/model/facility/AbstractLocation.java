package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.facility.Facility.FacilityValue;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractLocation extends AbstractAuditedVersionedPersistent<AbstractLocation>
		implements Comparable<AbstractLocation>, FacilityNode<AbstractLocation> {
	private static final long serialVersionUID = 3810180355498406255L;

	public static class BasicLocationView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface ShowHierarchy extends Basic {
		}
	}

	// ---------------------------------------- Fields

	@NotBlank(message = "Please enter a name.")
	private String name;
	private AbstractUpdateableLocation<?> parent;
	private AdministrativeUnit administrativeUnit;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private State state;
	private String zip;

	private boolean inactive;

	private SortedSet<AbstractUpdateableLocation<?>> children;

	// ---------------------------------------- Constructors

	protected AbstractLocation() {
	}

	protected AbstractLocation(String name, String addressLine1, String addressLine2, String city, State state,
			String zip, AbstractUpdateableLocation<?> parent) {
		this.name = name;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.state = state;
		this.zip = zip;

		this.parent = parent;
		if (parent != null)
			this.administrativeUnit = parent.getAdministrativeUnit();
	}

	// ---------------------------------------- Business Methods

	@Transient
	public String getDisplayName() {
		return getName();
	}

	@Transient
	public boolean isCentralOffice() {
		return FacilityValue.CENTRAL_OFFICE.getId() == getId();
	}

	@Transient
	@JsonView(BasicLocationView.ShowHierarchy.class)
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	public SortedSet<AbstractLocation> getFacilityChildren() {
		return getChildrenIncludingSelf().stream()
				.filter(p -> p.equals(this) || !"Facility".equals(p.getScale()) ? false : true)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Transient
	@JsonView(BasicLocationView.ShowHierarchy.class)
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	public SortedSet<AbstractLocation> getLocationChildren() {
		return getChildrenIncludingSelf().stream()
				.filter(p -> p.equals(this) || !"Location".equals(p.getScale()) ? false : true)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Transient
	@JsonView(BasicLocationView.Extended.class)
	public abstract String getScale();

	@Transient
	@JsonView(BasicLocationView.Extended.class)
	public String getAddressMultilineDisplay() {
		return getAddressDisplay(false);
	}

	private String getAddressDisplay(boolean useIdForState) {
		String state = "";
		if (getState() != null) {
			state = useIdForState ? String.valueOf(nullSafeGetId(getState())) : getState().getPostalName();
		}
		return StringUtil.getAddressDisplay(addressLine1, addressLine2, null, city, state, zip, "\n");
	}

	@Transient
	@JsonView(BasicLocationView.Basic.class)
	public boolean isActive() {
		return !isInactive();
	}

	public void setActive(boolean active) {
		setInactive(!active);
	}

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(AbstractLocation o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDisplayName(), o.getDisplayName()).toComparison() > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(name = "NameOfInstitution", length = 80, nullable = false)
	@JsonView(BasicLocationView.Extended.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	@BatchSize(size = 1000)
	@JsonIgnore
	@SortNatural
	public SortedSet<AbstractUpdateableLocation<?>> getChildrenIncludingSelf() {
		if (children == null)
			children = new TreeSet<>();
		return children;
	}

	public void setChildrenIncludingSelf(SortedSet<AbstractUpdateableLocation<?>> children) {
		this.children = children;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ParentFacilityFK")
	@BatchSize(size = 500)
	@JsonIgnore
	public AbstractUpdateableLocation<?> getParent() {
		return parent;
	}

	public void setParent(AbstractUpdateableLocation<?> parent) {
		this.parent = parent;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AdministrativeParentFK")
	@BatchSize(size = 500)
	@JsonIgnore
	public AdministrativeUnit getAdministrativeUnit() {
		return administrativeUnit;
	}

	public void setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		this.administrativeUnit = administrativeUnit;
	}

	@Column(name = "StreetAddress1", length = 35)
	@JsonView(BasicLocationView.Extended.class)
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "StreetAddress2", length = 35)
	@JsonView(BasicLocationView.Extended.class)
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(length = 30)
	@JsonView(BasicLocationView.Extended.class)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_StateFK", nullable = false)
	@BatchSize(size = 500)
	@JsonView(BasicLocationView.Extended.class)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Column(name = "ZipCode", length = 10)
	@JsonView(BasicLocationView.Extended.class)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
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
