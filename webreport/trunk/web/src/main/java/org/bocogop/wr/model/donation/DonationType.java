package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;

@Entity
@Table(name = "DonationType", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
@AttributeOverride(name = "name", column = @Column(name = "DonationType"))
public class DonationType extends AbstractAuditedVersionedPersistent<DonationType> implements Comparable<DonationType> {

	private static final long serialVersionUID = -1359460697821859488L;

	public static enum DonationTypeValue implements LookupType {
		CASH(1), //
		CHECK(2), //
		ITEM(3), //
		ACTIVITY(4), //
		EDONATION(5), //
		CREDIT_CARD(6);

		private long id;

		private DonationTypeValue(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}

		public static DonationTypeValue getById(long id) {
			for (DonationTypeValue v : values())
				if (v.getId() == id)
					return v;
			return null;
		}
	}

	private String donationType;

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(DonationType oo) {
		return new EqualsBuilder().append(getDonationType(), oo.getDonationType()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDonationType()).toHashCode();
	}

	@Override
	public int compareTo(DonationType o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(getDonationType(), o.getDonationType()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "DonationType", nullable = false)
	public String getDonationType() {
		return donationType;
	}

	public void setDonationType(String donationType) {
		this.donationType = donationType;
	}

}
