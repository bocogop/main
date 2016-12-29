package org.bocogop.wr.persistence.dao.leie;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.volunteer.Volunteer;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ExcludedEntityMatch {
	
	private Volunteer volunteer;
	private ExcludedEntity excludedEntity;

	public ExcludedEntityMatch(Volunteer volunteer, ExcludedEntity excludedEntity) {
		this.volunteer = volunteer;
		this.excludedEntity = excludedEntity;
	}

	public Volunteer getVolunteer() {
		return volunteer;
	}

	public ExcludedEntity getExcludedEntity() {
		return excludedEntity;
	}

}