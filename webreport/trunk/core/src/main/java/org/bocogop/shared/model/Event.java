package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.util.DateUtil;
import org.hibernate.annotations.BatchSize;
import org.springframework.format.annotation.DateTimeFormat;

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

		public interface List extends Basic {
		}
	}

	// ---------------------------------------- Fields

	private String name;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate date;
	private String description;

	private List<Participation> participations;

	// ---------------------------------------- Business Methods

	@Transient
	public int getNumberOfParticipants() {
		return getParticipations().size();
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	@OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<Participation> getParticipations() {
		if (participations == null)
			participations = new ArrayList<>();
		return participations;
	}

	public void setParticipations(List<Participation> participations) {
		this.participations = participations;
	}

}
