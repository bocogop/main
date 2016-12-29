package org.bocogop.wr.service.volunteer;

import java.time.ZoneId;
import java.util.Map;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityMatch;

public interface VolunteerService {

	/**
	 * @param volunteer
	 *            The volunteer to save or update
	 * @param createDataChangeNotifications
	 *            TODO
	 * @param autoTerminateIfLEIEMatch
	 *            TODO
	 * @param userContext
	 *            TODO
	 * @return The updated volunteer after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	Volunteer saveOrUpdate(Volunteer volunteer, boolean createDataChangeNotifications, boolean autoTerminateIfLEIEMatch)
			throws ServiceValidationException;

	/**
	 * Deletes the Volunteer with the specified volunteerId
	 * 
	 * @param volunteerId
	 *            The ID of the volunteer to delete
	 */
	void delete(long volunteerId);

	void setPrimaryOrganization(long volunteerId, long organizationId);

	void setPrimaryFacility(long volunteerId, long facilityId);

	boolean addOrReactivateOrganization(long volunteerId, long organizationId) throws ServiceValidationException;

	Map<String, Object> deleteOrganization(long volunteerOrganizationId);

	Map<String, Object> inactivateOrganization(long volunteerOrganizationId);

	boolean deleteAssignment(long volunteerAssignmentId);

	boolean addOrReactivateAssignment(Long volunteerAssignmentId, Long volunteerId, long facilityId,
			Long benefitingServiceRoleId) throws ServiceValidationException;

	int inactivateStaleVolunteers(ZoneId switchToTimeZone);

	void updatePreferredLanguage(long volunteerId, String language);

	boolean inactivateAssignment(long volunteerAssignmentId);

	Volunteer terminateVolunteer(long volunteerId, boolean withCause, String remarksToAppend);

	public static class LEIETerminationParams {
		public String facAdminNotificationName;
		public String facAdminNotificationDescription;
		public String natAdminNotificationName;
		public String natAdminNotificationDescription;

		public LEIETerminationParams(String facAdminNotificationName, String facAdminNotificationDescription,
				String natAdminNotificationName, String natAdminNotificationDescription) {
			this.facAdminNotificationName = facAdminNotificationName;
			this.facAdminNotificationDescription = facAdminNotificationDescription;
			this.natAdminNotificationName = natAdminNotificationName;
			this.natAdminNotificationDescription = natAdminNotificationDescription;
		}
	}

	Volunteer terminateVolunteerForLEIEMatch(ExcludedEntityMatch match, LEIETerminationParams termParams)
			throws ServiceValidationException;

}
