package org.bocogop.shared.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractIdentifiedPersistent;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SortNatural;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Immutable
@Table(name = "Country", schema = "Core")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class Country extends AbstractIdentifiedPersistent<Country> implements Comparable<Country> {
	private static final long serialVersionUID = -6785221727303119387L;

	// -------------------------------- Fields

	private String code;
	private String name;

	private SortedSet<State> states;

	// -------------------------------- Business Methods

	// -------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Country o) {
		return new EqualsBuilder().append(getCode(), o.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}

	@Override
	public int compareTo(Country o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(getName(), o.getName()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------- Accessor Methods

	@Column(length = 100)
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 4)
	@JsonProperty
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	@SortNatural
	public SortedSet<State> getStates() {
		if (states == null)
			states = new TreeSet<>();
		return states;
	}

	public void setStates(SortedSet<State> states) {
		this.states = states;
	}

	public static enum CountryType implements LookupType {
		UNITED_STATES(1, "US", "United States"), //
		;

		private long id;
		private String code;
		private String name;

		private CountryType(long id, String code, String name) {
			this.id = id;
			this.code = code;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

	}

}
