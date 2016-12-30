package org.bocogop.shared.model.lookup.sds;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * State Lookup object from SDS State table
 */
@Entity
@Immutable
@Table(name = "STD_State", schema = "SDSADM")
@AttributeOverrides({ @AttributeOverride(name = "version", column = @Column(name = "version") ),
		@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy") ),
		@AttributeOverride(name = "createdDate", column = @Column(name = "created") ),
		@AttributeOverride(name = "modifiedBy", column = @Column(name = "updatedBy") ),
		@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated") ) })
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class State extends AbstractAuditedVersionedPersistent<State> implements Comparable<State> {
	private static final long serialVersionUID = -6786672001398191799L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class StateView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	@JsonProperty
	private String name;
	@JsonProperty
	private String postalName;
	@JsonProperty
	private String fipsCode;
	private long country_id;

	@Transient
	@JsonView(StateView.Basic.class)
	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bocogop.wr.persistence.AbstractPersistent#requiredEquals(java.lang
	 * .Object)
	 */
	@Override
	protected boolean requiredEquals(State o) {
		return new EqualsBuilder().append(getFipsCode(), o.getFipsCode()).append(getPostalName(), o.getPostalName())
				.isEquals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bocogop.wr.persistence.AbstractPersistent#requiredHashCode()
	 */
	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getFipsCode()).append(getPostalName()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(State o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getName().toLowerCase(), o.getName().toLowerCase()).toComparison() > 0 ? 1 : -1;
	}

	/**
	 * @return the name
	 */
	@Column(name = "name", length = 50)
	@JsonIgnore
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the postalName
	 */
	@Column(name = "postalName", length = 30)
	@JsonView(StateView.Basic.class)
	public String getPostalName() {
		return postalName;
	}

	/**
	 * @param postalName
	 *            the postalName to set
	 */
	public void setPostalName(String postalName) {
		this.postalName = postalName;
	}

	/**
	 * @return the fipsCode
	 */
	@Column(name = "fipsCode", length = 2)
	@JsonView(StateView.Extended.class)
	public String getFipsCode() {
		return fipsCode;
	}

	/**
	 * @param fipsCode
	 *            the fipsCode to set
	 */
	public void setFipsCode(String fipsCode) {
		this.fipsCode = fipsCode;
	}

	/**
	 * @return the country_id
	 */
	@Column(name = "country_id")
	@JsonView(StateView.Extended.class)
	public long getCountry_id() {
		return country_id;
	}

	/**
	 * @param country_id
	 *            the country_id to set
	 */
	public void setCountry_id(long country_id) {
		this.country_id = country_id;
	}

}
