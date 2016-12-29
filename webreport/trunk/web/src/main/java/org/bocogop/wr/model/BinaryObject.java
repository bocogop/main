package org.bocogop.wr.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "BINARY_OBJECTS", schema = "CORE")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class BinaryObject extends AbstractAuditedVersionedPersistent<BinaryObject> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BinaryObject.class);
	private static final long serialVersionUID = -5324184680455657519L;

	// ---------------------------------------- Fields

	@NotNull
	private Serializable data;

	// ---------------------------------------- Constructors

	public BinaryObject() {
	}

	public BinaryObject(Serializable data) {
		setData(data);
	}

	// ---------------------------------------- Common Methods

	/*
	 * Implement business-key equality (Hibernate requirement) in both these
	 * methods; both should include the same fields and in the same order
	 */
	@Override
	protected boolean requiredEquals(BinaryObject o) {
		return new EqualsBuilder().append(getData(), o.getData()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getData()).toHashCode();
	}

	// ---------------------------------------- Accessor Methods

	@Lob
	@Type(type = "org.bocogop.wr.persistence.impl.hibernateType.SerializableToCompressedBlobType")
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "BINARY_OBJECT")
	public Serializable getData() {
		return data;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

}
