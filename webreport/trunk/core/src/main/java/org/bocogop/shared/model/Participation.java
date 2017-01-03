package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.voter.Voter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "Participation")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Participation extends AbstractAuditedVersionedPersistent<Participation> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class ParticipationView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// ---------------------------------------- Fields

	private Voter voter;
	private Event event;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Participation oo) {
		return new EqualsBuilder().append(nullSafeGetId(getVoter()), nullSafeGetId(oo.getVoter()))
				.append(nullSafeGetId(getEvent()), nullSafeGetId(oo.getEvent())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVoter())).append(nullSafeGetId(getEvent())).toHashCode();
	}

	// ---------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VoterFK", nullable = false)
	@NotNull
	public Voter getVoter() {
		return voter;
	}

	public void setVoter(Voter voter) {
		this.voter = voter;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EventFK", nullable = false)
	@NotNull
	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

}
