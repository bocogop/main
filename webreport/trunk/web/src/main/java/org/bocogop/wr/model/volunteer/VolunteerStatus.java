package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;

@Entity
@Immutable
@Table(name = "WR_STD_VolunteerStatus", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class VolunteerStatus extends AbstractLookup<VolunteerStatus, VolunteerStatusType> {

	private boolean volunteerActive;
	private boolean volunteerTerminated;

	private static final long serialVersionUID = 8503403148811184322L;

	public static enum VolunteerStatusType implements LookupType {
		ACTIVE(1, "Active", true, false), //
		INACTIVE(2, "Inactive", false, false), //
		TERMINATED(3, "Terminated", false, true), //
		TERMINATED_WITH_CAUSE(4, "Terminated with Cause", false, true);

		private long id;
		private String name;
		private boolean terminated;
		private boolean active;

		private VolunteerStatusType(long id, String name, boolean active, boolean terminated) {
			this.id = id;
			this.name = name;
			this.active = active;
			this.terminated = terminated;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isTerminated() {
			return terminated;
		}

		public boolean isActive() {
			return active;
		}

		public static VolunteerStatusType getById(int statusId) {
			for (VolunteerStatusType t : values())
				if (t.getId() == statusId)
					return t;
			return null;
		}
	}

	// -------------------------------------------- Business Methods

	@Transient
	public boolean isVolunteerInactiveOrTerminated() {
		return !isVolunteerActive();
	}

	// -------------------------------------------- Accessor Methods

	@Column(name = "ACTIVE", nullable = false)
	@JsonProperty
	public boolean isVolunteerActive() {
		return volunteerActive;
	}

	@SuppressWarnings("unused")
	private void setVolunteerActive(boolean volunteerActive) {
		this.volunteerActive = volunteerActive;
	}

	@Column(name = "TERMINATED", nullable = false)
	@JsonProperty
	public boolean isVolunteerTerminated() {
		return volunteerTerminated;
	}

	@SuppressWarnings("unused")
	private void setVolunteerTerminated(boolean volunteerTerminated) {
		this.volunteerTerminated = volunteerTerminated;
	}

}