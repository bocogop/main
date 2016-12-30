package org.bocogop.shared.model.lookup;

import java.time.ZonedDateTime;

import org.bocogop.shared.model.core.AuditedPersistent;

public interface Lookup<T, U> extends AuditedPersistent {

	String getDescription();

	String getName();

	Integer getSortOrder();

	ZonedDateTime getEffectiveDate();

	ZonedDateTime getExpirationDate();

	U getLookupType();

}
