package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;

@Entity
@Table(name = "DonorType", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonorType extends AbstractIdentifiedPersistent<DonorType> {

	private static final long serialVersionUID = -5062512168142239943L;

	private String donorType;

	@Transient
	@JsonIgnore
	public DonorTypeValue getLookupType() {
		return Arrays.stream(DonorTypeValue.class.getEnumConstants()).filter(x -> x.getId() == getId()).findAny()
				.orElse(null);
	}

	@Override
	protected boolean requiredEquals(DonorType oo) {
		return new EqualsBuilder().append(getDonorType(), oo.getDonorType()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDonorType()).toHashCode();
	}

	@Column(length = 50)
	public String getDonorType() {
		return donorType;
	}

	public void setDonorType(String donorType) {
		this.donorType = donorType;
	}

	public static enum DonorTypeValue implements LookupType {
		INDIVIDUAL(1, "Individual", false), //
		ORG_AND_INDIVIDUAL(2, "Organization & Individual", true), //
		OTHER_AND_INDIVIDUAL(3, "Other Groups & Individual", true), //
		ORGANIZATION(4, "Organization", false), //
		OTHER_GROUPS(5, "Other Groups", true), //
		ANONYMOUS(6, "Anonymous", false);

		private long id;
		private String name;
		private boolean legacy;

		private DonorTypeValue(long id, String name, boolean legacy) {
			this.id = id;
			this.name = name;
			this.legacy = legacy;
		}

		public String getName() {
			return name;
		}

		@Override
		public long getId() {
			return id;
		}

		public boolean isLegacy() {
			return legacy;
		}
	}

}
