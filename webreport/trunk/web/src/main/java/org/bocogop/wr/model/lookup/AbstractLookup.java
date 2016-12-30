package org.bocogop.wr.model.lookup;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.ZonedDateTime;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unchecked")
@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public abstract class AbstractLookup<T extends AbstractLookup<T, U>, U extends LookupType>
		extends AbstractAuditedPersistent<T> implements Comparable<T>, Lookup<T, U> {
	private static final long serialVersionUID = 5273513406574050724L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractLookup.class);

	// ---------------------------------------- Fields

	@NotNull(message = "The Name is required.")
	@Size(max = 80, message = "The Name must be less than or equal to 80 characters.")
	private String name;

	@NotNull(message = "The Description is required.")
	@Size(max = 250, message = "The Name must be less than or equal to 250 characters.")
	private String description;

	private Integer sortOrder;

	@DateTimeFormat(pattern = "mm/dd/yyyy")
	private ZonedDateTime effectiveDate;

	@DateTimeFormat(pattern = "mm/dd/yyyy")
	private ZonedDateTime expirationDate;

	private Class<U> lookupTypeClass;

	{
		lookupTypeClass = (Class<U>) TypeUtil.getTypeParameterClass(this, 1);
	}

	// ---------------------------------------- Business Methods

	@Transient
	@JsonProperty
	public boolean isActive() {
		ZonedDateTime now = ZonedDateTime.now();
		if (getEffectiveDate() == null && getExpirationDate() == null)
			return true;
		if (getEffectiveDate() == null)
			return now.isBefore(getExpirationDate());
		if (getExpirationDate() == null)
			return !now.isBefore(getEffectiveDate());
		return !now.isBefore(getEffectiveDate()) && getExpirationDate().isAfter(now);
	}

	@Transient
	public U getLookupType() {
		return Arrays.stream(lookupTypeClass.getEnumConstants()).filter(x -> x.getId() == getId()).findAny()
				.orElse(null);
	}

	// ---------------------------------------- Common Methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bocogop.wr.persistence.AbstractPersistent#requiredEquals(java.lang
	 * .Object)
	 */
	@Override
	protected boolean requiredEquals(T o) {
		return new EqualsBuilder().append(getId(), o.getId()).isEquals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bocogop.wr.persistence.AbstractPersistent#requiredHashCode()
	 */
	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(T u) {
		if (equals(u))
			return 0;
		return new CompareToBuilder().append(getSortOrder(), u.getSortOrder()).append(getName(), u.getName())
				.toComparison() > 0 ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": id=" + getId() + ", name=" + getName();
	}

	@Column(length = 80)
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 250)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "SORT_ORDER")
	@XmlTransient
	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Column(name = "EFFECTIVE_DATE", nullable = false)
	@XmlTransient
	public ZonedDateTime getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(ZonedDateTime effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Column(name = "EXPIRATION_DATE")
	public ZonedDateTime getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(ZonedDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

}
