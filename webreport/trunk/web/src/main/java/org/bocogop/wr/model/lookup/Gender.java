package org.bocogop.wr.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.model.core.AbstractAuditedVersionedPersistent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity(name = "org.bocogop.wr.model.sds.Gender")
@Immutable
@Table(name = "STD_Gender", schema = "SDSADM")
@AttributeOverrides({ @AttributeOverride(name = "version", column = @Column(name = "version") ),
		@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy") ),
		@AttributeOverride(name = "createdDate", column = @Column(name = "created") ),
		@AttributeOverride(name = "modifiedBy", column = @Column(name = "updatedBy") ),
		@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated") ) })
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class Gender extends AbstractAuditedVersionedPersistent<Gender> implements Comparable<Gender> {
	private static final long serialVersionUID = -6785221727303119387L;

	// -------------------------------- Fields

	private String name;
	private String description;
	private String code;

	// -------------------------------- Business Methods

	// -------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Gender o) {
		return new EqualsBuilder().append(getName(), o.getName()).append(getDescription(), o.getDescription())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(getDescription()).toHashCode();
	}

	@Override
	public int compareTo(Gender o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(name, o.getName()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------- Accessor Methods

	@Column(name = "NAME", length = 100)
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "CODE", length = 4)
	@JsonProperty
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "DESCRIPTION", length = 60)
	@JsonProperty
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static enum GenderType implements LookupType {
		AMBIGUOUS(1009251, "A", "Ambiguous"), //
		FEMALE(1009252, "F", "Female"), //
		MALE(1009253, "M", "Male"), //
		NOT_APPLICABLE(1009254, "N", "Not Applicable"), //
		OTHER(1009255, "O", "Other"), //
		UNKNOWN(1009256, "U", "Unknown"), //
		UNDIFFERENTIATED(1009257, "UN", "Undifferentiated");

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
