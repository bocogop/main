package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "ShirtSizes", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ShirtSize extends AbstractAuditedVersionedPersistent<ShirtSize> implements Comparable<ShirtSize> {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	private String name;
	private int order;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(ShirtSize oo) {
		return new EqualsBuilder().append(name, oo.getName()).append(order, oo.getOrder()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(name).append(order).toHashCode();
	}

	@Override
	public int compareTo(ShirtSize o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(order, o.getOrder()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "SizeName", length = 15, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "SizeOrder", nullable = false)
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
