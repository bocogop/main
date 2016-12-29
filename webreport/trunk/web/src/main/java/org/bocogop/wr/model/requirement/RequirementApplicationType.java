package org.bocogop.wr.model.requirement;

/**
 * Defines whether the requirement applies to all volunteers, only those having
 * an assignment with a specific role type, or only those having assignments to
 * specific roles. This definition applies to whatever scope the requirement has
 * (national or facility level). CPB
 * 
 */
public enum RequirementApplicationType {
	ALL_VOLUNTEERS, ROLE_TYPE, SPECIFIC_ROLES;
}
