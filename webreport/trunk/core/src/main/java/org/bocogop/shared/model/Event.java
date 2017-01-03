package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Event")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Event extends AbstractAuditedVersionedPersistent<Event> implements Comparable<Event> {
	private static final long serialVersionUID = -8678395783438462990L;

	public static class EventView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	public static class EventAssignmentsAndOrgsView {
		public interface Combined {
		}
	}

	// ---------------------------------------- Fields

	private String name;
	private LocalDate date;

	private List<Participation> participations;
	
	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Event oo) {
		return new EqualsBuilder().append(getDate(), oo.getDate()).append(getName(), oo.getName()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDate()).append(getName()).toHashCode();
	}

	@Override
	public int compareTo(Event o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(getDate(), o.getDate()).append(getName(), o.getName()).toComparison() > 0
				? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Column(nullable = false, length = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	@OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<Participation> getParticipations() {
		return participations;
	}

	public void setParticipations(List<Participation> participations) {
		this.participations = participations;
	}
	
}
