package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.requirement.RequirementDateType.RequirementDateTypeValue;

@Entity
@Immutable
@Table(name = "WR_STD_RequirementDateType", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class RequirementDateType extends AbstractLookup<RequirementDateType, RequirementDateTypeValue> {

	private static final long serialVersionUID = 6209089201458715842L;

	private boolean skipNotification;

	@Column(nullable = false)
	@JsonProperty
	public boolean isSkipNotification() {
		return skipNotification;
	}

	public void setSkipNotification(boolean skipNotification) {
		this.skipNotification = skipNotification;
	}

	public static enum RequirementDateTypeValue implements LookupType {
		START_DATE(1, "Start Date"), //
		END_DATE(2, "End Date"), //
		COMPLETION_DATE(3, "Completion Date"), //
		DUE_DATE(4, "Due Date"), //
		EXPIRATION_DATE(5, "Expiration Date"), //
		NOT_APPLICABLE(6, "Not Applicable");

		private long id;
		private String name;

		private RequirementDateTypeValue(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}

}