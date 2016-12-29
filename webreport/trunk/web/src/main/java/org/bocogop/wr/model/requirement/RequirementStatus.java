package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue;

@Entity
@Immutable
@Table(name = "WR_STD_RequirementStatus", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class RequirementStatus extends AbstractLookup<RequirementStatus, RequirementStatusValue> {

	private static final long serialVersionUID = 8503403148811184322L;

	public static enum RequirementStatusValue implements LookupType {
		MET(1, "Met"), //
		UNMET(2, "Not Met"), //
		INPROGRESS(3, "In Progress"), //
		BLOCKED(4, "Blocked"), //
		NOT_APPLICABLE(5, "Not Applicable"), //
		NEW(6, "New"), //
		;

		private long id;
		private String name;

		private RequirementStatusValue(long id, String name) {
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