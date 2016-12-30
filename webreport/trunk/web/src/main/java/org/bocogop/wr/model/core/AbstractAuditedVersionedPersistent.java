package org.bocogop.wr.model.core;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public abstract class AbstractAuditedVersionedPersistent<T> extends AbstractAuditedPersistent<T>
		implements AuditedVersionedPersistent {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractAuditedVersionedPersistent.class);
	private static final long serialVersionUID = 9081660873563592483L;

	// ---------------------------------------- Fields

	private int version;

	// ---------------------------------------- Accessor Methods

	@Version
	@Column(name = "Ver")
	@JsonIgnore
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
