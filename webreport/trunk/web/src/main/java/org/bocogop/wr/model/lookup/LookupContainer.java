package org.bocogop.wr.model.lookup;

import java.time.ZonedDateTime;

public interface LookupContainer<X> extends Lookup<X, LookupType> {

	Lookup<X, ? extends LookupType> getLookup();

	default String getDescription() {
		return getLookup().getDescription();
	}

	default String getName() {
		return getLookup().getName();
	}

	default Integer getSortOrder() {
		return getLookup().getSortOrder();
	}

	default ZonedDateTime getEffectiveDate() {
		return getLookup().getEffectiveDate();
	}

	default ZonedDateTime getExpirationDate() {
		return getLookup().getExpirationDate();
	}

	default LookupType getLookupType() {
		return getLookup().getLookupType();
	}

}
