package org.bocogop.shared.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractIdentifiedPersistent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Immutable
@Table(name = "State", schema = "Core")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class State extends AbstractIdentifiedPersistent<State> implements Comparable<State> {
	private static final long serialVersionUID = -6785221727303119387L;

	// -------------------------------- Fields

	private String code;
	private String name;

	private Country country;

	// -------------------------------- Business Methods

	// -------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(State o) {
		return new EqualsBuilder().append(getCode(), o.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}

	@Override
	public int compareTo(State o) {
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CountryFK")
	@JsonIgnore
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public static enum StateType implements LookupType {
		COLORADO(6, "CO", "Colorado"), //
		;

		private long id;
		private String code;
		private String name;

		private StateType(long id, String code, String name) {
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
