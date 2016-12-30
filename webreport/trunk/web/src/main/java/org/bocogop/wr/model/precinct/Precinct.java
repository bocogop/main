package org.bocogop.wr.model.precinct;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@DiscriminatorValue("F")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Precinct extends AbstractAuditedVersionedPersistent<Precinct> implements Comparable<Precinct> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class PrecinctView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

	}

	// ---------------------------------------- Fields

	@NotBlank(message = "Please enter a code.")
	private String code;
	@NotBlank(message = "Please enter a name.")
	private String name;

	private String precinctNum;
	private String congressionalDistrict;
	private String senateDistrict;
	private String houseDistrict;
	private String countyCode;

	// ---------------------------------------- Constructors

	public Precinct() {
	}

	public Precinct(String code, String name) {
		this.code = code;
		this.name = name;
	}

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Precinct oo) {
		/*
		 * Safe cast due to AbstractBusinessKeyOwner logic; since we need a
		 * field in the concrete class, we need to ensure we aren't trying to
		 * cast a proxy, which would fail - CPB
		 */
		return new EqualsBuilder().append(getCode(), oo.getCode()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public int compareTo(Precinct o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getCode(), o.getCode()).toComparison() > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(length = 20, nullable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(length = 40, nullable = false)
	@JsonView(PrecinctView.Basic.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 3, nullable = false, insertable = false, updatable = false)
	@JsonView(PrecinctView.Extended.class)
	public String getPrecinctNum() {
		return precinctNum;
	}

	public void setPrecinctNum(String precinctNum) {
		this.precinctNum = precinctNum;
	}

	@Column(length = 1, nullable = false, insertable = false, updatable = false)
	@JsonView(PrecinctView.Extended.class)
	public String getCongressionalDistrict() {
		return congressionalDistrict;
	}

	public void setCongressionalDistrict(String congressionalDistrict) {
		this.congressionalDistrict = congressionalDistrict;
	}

	@Column(length = 2, nullable = false, insertable = false, updatable = false)
	@JsonView(PrecinctView.Extended.class)
	public String getSenateDistrict() {
		return senateDistrict;
	}

	public void setSenateDistrict(String senateDistrict) {
		this.senateDistrict = senateDistrict;
	}

	@Column(length = 2, nullable = false, insertable = false, updatable = false)
	@JsonView(PrecinctView.Extended.class)
	public String getHouseDistrict() {
		return houseDistrict;
	}

	public void setHouseDistrict(String houseDistrict) {
		this.houseDistrict = houseDistrict;
	}

	@Column(length = 2, nullable = false, insertable = false, updatable = false)
	@JsonView(PrecinctView.Extended.class)
	public String getCountyCode() {
		return countyCode;
	}

	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}

}
