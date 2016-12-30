package org.bocogop.wr.service.impl;

import static org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.apache.commons.lang3.StringUtils.trim;

import java.time.LocalDate;

import org.bocogop.wr.model.CoreUserDetails;
import org.bocogop.wr.model.Permission;
import org.bocogop.wr.model.Permission.PermissionType;
import org.bocogop.wr.model.voter.Voter;
import org.bocogop.wr.service.validation.ServiceValidationException;
import org.bocogop.wr.service.voter.VoterService;
import org.bocogop.wr.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class VoterServiceImpl extends AbstractServiceImpl implements VoterService {
	private static final Logger log = LoggerFactory.getLogger(VoterServiceImpl.class);

	@Value("${maxIdleDaysBeforeVoterInactivation}")
	private int maxIdleDaysBeforeVoterInactivation;
	@Value("${voterInactivationGracePeriod}")
	private int voterInactivationGracePeriod;
	@Value("${notification.voterSelfService.expirationDaysOut}")
	private int expirationDaysOut;

	@Override
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public Voter saveOrUpdate(Voter vol, boolean createDataChangeNotifications, boolean autoTerminateIfLEIEMatch)
			throws ServiceValidationException {
		CoreUserDetails userContext = null;

		/*
		 * If the person has LOGIN_KIOSK permission, allow them to proceed only
		 * if they are editing the same voter as the logged in context.
		 * Otherwise let the person proceed only if they have VOTER_EDIT
		 * permission.
		 */
		userContext = getCurrentUser();
		if (!SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.VOTER_EDIT)) {
			log.warn(userContext.getClass().getSimpleName() + " with ID " + userContext.getId()
					+ " tried to update voter " + vol.getId() + " but does not have permission");
			throw new AccessDeniedException("You do not have proper permission to update or create this voter.");
		}

		boolean isNew = !vol.isPersistent();

		/*
		 * Detaching to ensure we get a fresh copy of the vol data from the DB
		 * and it doesn't use what's in the session cache (otherwise the
		 * existingVoter == voter)- CPB
		 */
		voterDAO.detach(vol);
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver2=" + vol.getVersion());

		Voter existingVol = isNew ? null : voterDAO.findRequiredByPrimaryKey(vol.getId());
		if (existingVol != null)
			voterDAO.detach(existingVol);
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver3=" + vol.getVersion());

		if (isNew) {
			vol.setEntryDate(LocalDate.now());
		}
		vol.setFirstName(trim(capitalizeFully(vol.getFirstName())));
		vol.setMiddleName(trim(capitalizeFully(vol.getMiddleName())));
		vol.setLastName(trim(capitalizeFully(vol.getLastName())));
		vol.setSuffix(trim(capitalizeFully(vol.getSuffix())));
		vol.setZip(trim(vol.getZip()));

		vol = voterDAO.saveOrUpdate(vol);

		return vol;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public void delete(long voterId) {
		voterDAO.delete(voterId);
	}

}
