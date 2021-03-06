package org.bocogop.shared.model.lookup;

import java.io.UnsupportedEncodingException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractAuditedVersionedPersistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "Template", schema = "Core")
public class Template extends AbstractAuditedVersionedPersistent<Template> implements Comparable<Template> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Template.class);
	private static final long serialVersionUID = -6309831088155859971L;

	// ---------------------------------------- Fields

	private String name;
	private byte[] bodyBytes;

	// ---------------------------------------- Business Methods

	@Transient
	public String getBody() {
		try {
			return new String(bodyBytes, "ISO=8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding", e);
		}
	}

	public void setBody(String body) {
		bodyBytes = body.getBytes();
	}

	// ---------------------------------------- Common Methods

	/*
	 * Implement business-key equality (Hibernate requirement) in both these
	 * methods; both should include the same fields and in the same order
	 */
	@Override
	protected boolean requiredEquals(Template o) {
		return new EqualsBuilder().append(getName(), o.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).toHashCode();
	}

	public int compareTo(Template u) {
		if (equals(u))
			return 0;
		return new CompareToBuilder().append(getName(), u.getName()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": name=" + getName();
	}

	// ---------------------------------------- Accessor Methods

	@Column(length = 40, nullable = false)
	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(nullable = false)
	public byte[] getBodyBytes() {
		return bodyBytes;
	}

	public void setBodyBytes(byte[] bodyBytes) {
		this.bodyBytes = bodyBytes;
	}

}
