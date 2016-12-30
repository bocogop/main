package org.bocogop.wr.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.ZonedDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.model.core.AbstractAuditedPersistent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Represents an instance where a user called an audited method.
 */
@Entity
@Table(name = "AUDIT_LOG", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "AUDIT_LOG_ID") ) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class AuditLogEntry extends AbstractAuditedPersistent<AuditLogEntry>implements Comparable<AuditLogEntry> {
	private static final long serialVersionUID = -1207845227302313398L;

	// ---------------------------------------- Fields

	private ZonedDateTime date;
	private String appUserId;
	private String method;
	private String paramValues;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AuditLogEntry oo) {
		return new EqualsBuilder().append(getDate(), oo.getDate()).append(getAppUserId(), oo.getAppUserId())
				.append(getMethod(), oo.getMethod()).append(getParamValues(), oo.getParamValues()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDate()).append(getAppUserId()).append(getMethod())
				.append(getParamValues()).toHashCode();
	}

	public int compareTo(AuditLogEntry o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDate(), o.getDate()).append(getAppUserId(), o.getAppUserId())
				.append(getMethod(), o.getMethod()).append(getParamValues(), o.getParamValues()).toComparison() > 0 ? 1
						: -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(name = "APP_USER_ID", nullable = false)
	public String getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(String appUserId) {
		this.appUserId = appUserId;
	}

	@Column(name = "EXECUTION_DATE", nullable = false)
	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	@Column(name = "METHOD_NAME", length = 100, nullable = false)
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Column(name = "PARAMETER_VALUES")
	public String getParamValues() {
		return paramValues;
	}

	public void setParamValues(String paramValues) {
		this.paramValues = paramValues;
	}

}
