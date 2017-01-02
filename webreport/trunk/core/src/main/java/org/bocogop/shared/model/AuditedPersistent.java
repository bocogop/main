package org.bocogop.shared.model;

import java.time.ZonedDateTime;

public interface AuditedPersistent extends IdentifiedPersistent {

	String getCreatedBy();

	void setCreatedBy(String createdBy);

	ZonedDateTime getCreatedDate();

	void setCreatedDate(ZonedDateTime createdDate);

	String getModifiedBy();

	void setModifiedBy(String modifiedBy);

	ZonedDateTime getModifiedDate();

	void setModifiedDateOverride(ZonedDateTime modifiedDate);

}