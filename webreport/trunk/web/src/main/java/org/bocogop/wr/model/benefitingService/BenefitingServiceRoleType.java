package org.bocogop.wr.model.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType.BenefitingServiceRoleTypeValue;

@Entity
@Immutable
@Table(name = "WR_STD_BenefitingServiceRoleTypes", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class BenefitingServiceRoleType
		extends AbstractLookup<BenefitingServiceRoleType, BenefitingServiceRoleTypeValue> {
	private static final long serialVersionUID = -6332598924348883474L;

	public static enum BenefitingServiceRoleTypeValue implements LookupType {
		GENERAL(1);

		private long id;

		private BenefitingServiceRoleTypeValue(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

	}

}
