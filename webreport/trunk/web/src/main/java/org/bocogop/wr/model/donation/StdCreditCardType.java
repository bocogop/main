package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.donation.StdCreditCardType.StdCreditCardTypeValue;

@Entity
@Immutable
@Table(name = "WR_STD_CreditCardType", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class StdCreditCardType extends AbstractLookup<StdCreditCardType, StdCreditCardTypeValue> {

	private static final long serialVersionUID = -8388647366469652515L;

	public static enum StdCreditCardTypeValue implements LookupType {
		AMEX(1);

		private long id;

		private StdCreditCardTypeValue(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}

	}

}
