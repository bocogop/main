package org.bocogop.wr.model;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.model.core.AbstractAuditedVersionedPersistent;

@Entity
@Table(name = "APP_PARAMETER", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "APP_PARAMETER_ID") ) })
public class ApplicationParameter extends AbstractAuditedVersionedPersistent<ApplicationParameter> {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	@NotNull
	private String parameterName;

	@NotNull
	private String parameterValue;

	// -------------------------------------- Business Methods

	@Transient
	public BigDecimal getParameterValueAsBigDecimal() {
		return new BigDecimal(getParameterValue());
	}

	// -------------------------------------- Accessor Methods

	@Column(name = "PARAMETER_NAME")
	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@Column(name = "PARAMETER_VALUE")
	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	@Override
	protected boolean requiredEquals(ApplicationParameter oo) {
		return new EqualsBuilder().append(getParameterName(), oo.getParameterName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getParameterName()).toHashCode();
	}

	public static enum ApplicationParameterType {
		LEIE_JOB_LAST_EXECUTED_DATE("LEIE_JOB_LAST_EXECUTED_DATE"),
		LEIE_SOURCE_DATA_CHANGED_DATE("LEIE_SOURCE_DATA_CHANGED_DATE"),
		;

		private ApplicationParameterType(String paramName) {
			this.paramName = paramName;
		}

		private String paramName;

		public String getParamName() {
			return paramName;
		}

		public static ApplicationParameterType getByName(String paramName) {
			for (ApplicationParameterType type : values())
				if (type.getParamName().equals(paramName))
					return type;
			return null;
		}

	}

}
