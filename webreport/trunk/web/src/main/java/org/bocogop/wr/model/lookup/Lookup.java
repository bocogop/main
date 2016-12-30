package org.bocogop.wr.model.lookup;

import java.time.ZonedDateTime;

import org.bocogop.wr.model.core.AuditedPersistent;

public interface Lookup<T, U> extends AuditedPersistent {

	String getDescription();

	String getName();

	Integer getSortOrder();

	ZonedDateTime getEffectiveDate();

	ZonedDateTime getExpirationDate();

	U getLookupType();

}
