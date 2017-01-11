package org.bocogop.shared.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractIdentifiedPersistent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Immutable
@Table(name = "Party")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class Party extends AbstractIdentifiedPersistent<Party> implements Comparable<Party> {
	private static final long serialVersionUID = -6785221727303119387L;

	// -------------------------------- Fields

	private String code;
	private String name;

	// -------------------------------- Business Methods

	// -------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Party o) {
		return new EqualsBuilder().append(getCode(), o.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}

	@Override
	public int compareTo(Party o) {
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

	public static enum GenderType implements LookupType {
		UNAFFILIATED(1, "UAF", "Unaffiliated"), //
		LIBERTARIAN(2, "LBR", "Libertarian"), //
		GREEN(3, "GRN", "Green"), //
		DEMOCRATIC(4, "DEM", "Democratic"), //
		AMERICAN_CONSTITUTION(5, "ACN", "American Constitution"), //
		UNITY(6, "UNI", "Unity"), //
		REPUBLICAN(7, "REP", "Republican") //
		;

		private long id;
		private String code;
		private String name;

		private GenderType(long id, String code, String name) {
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
