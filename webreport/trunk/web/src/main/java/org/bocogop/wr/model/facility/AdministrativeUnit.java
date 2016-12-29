package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.model.lookup.sds.VAFacilityType;

@Entity
@Table(name = "AdministrativeUnits", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class AdministrativeUnit extends AbstractAuditedVersionedPersistent<AdministrativeUnit>
		implements Comparable<AdministrativeUnit>, FacilityNode<AdministrativeUnit> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class AdministrativeUnitView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface ShowHierarchy extends Basic, Facility.FacilityView.ShowHierarchy {
		}
	}

	// ---------------------------------------- Constants

	public static final String TYPE_VISN = "VISN";

	// ---------------------------------------- Fields

	@NotNull
	private String name;
	private int number;
	@NotNull
	private String type;

	private VAFacility visnFacility;

	private SortedSet<AbstractUpdateableLocation<?>> children;

	// ---------------------------------------- Constructors

	public AdministrativeUnit() {
	}

	public AdministrativeUnit(VAFacility facility) {
		if (!facility.isVISN()) {
			VAFacilityType facilityType = facility.getFacilityType();
			throw new IllegalArgumentException("Can't create a " + AdministrativeUnit.class.getName() + " from a "
					+ VAFacility.class.getName() + " of type " + facilityType.getCode());
		}

		this.name = facility.getName();
		this.number = facility.getId().intValue();
		this.type = TYPE_VISN;
		this.visnFacility = facility;
	}

	// ---------------------------------------- Business Methods

	@Transient
	@JsonProperty
	public String getDisplayName() {
		return "VISN " + getNumber() + " - " + getName();
	}

	@Transient
	@JsonProperty
	public boolean isActive() {
		return true;
	}

	@Transient
	@Override
	@JsonView(AdministrativeUnitView.ShowHierarchy.class)
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	public SortedSet<AbstractUpdateableLocation<?>> getFacilityChildren() {
		return getChildren().stream().filter(p -> p.getParent() == null || p.getParent().equals(p))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AdministrativeUnit oo) {
		return new EqualsBuilder().append(number, oo.getNumber()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(number).toHashCode();
	}

	@Override
	public int compareTo(AdministrativeUnit o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(number, o.getNumber()).toComparison() > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(name = "UnitName", length = 50, nullable = false)
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "administrativeUnit", fetch = FetchType.LAZY)
	@BatchSize(size = 1000)
	@SortNatural
	public SortedSet<AbstractUpdateableLocation<?>> getChildren() {
		return children;
	}

	public void setChildren(SortedSet<AbstractUpdateableLocation<?>> children) {
		this.children = children;
	}

	@Column(name = "UnitNumber", nullable = false)
	@JsonProperty
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Column(name = "Type", length = 5, nullable = false)
	@JsonProperty
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SDS_INSTITUTION_ID")
	@BatchSize(size = 500)
	public VAFacility getVisnFacility() {
		return visnFacility;
	}

	public void setVisnFacility(VAFacility visnFacility) {
		this.visnFacility = visnFacility;
	}

}
