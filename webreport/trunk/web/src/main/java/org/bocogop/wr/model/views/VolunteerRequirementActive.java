package org.bocogop.wr.model.views;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import org.bocogop.wr.model.requirement.AbstractVolunteerRequirement;

@Entity
@Immutable
@Table(name = "VolunteerRequirementsActive", schema = "dbo")
public class VolunteerRequirementActive extends AbstractVolunteerRequirement {
	private static final long serialVersionUID = 3254887002195956807L;

}
