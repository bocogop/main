package org.bocogop.shared.service.impl;

import static org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.apache.commons.lang3.StringUtils.trim;

import org.apache.commons.lang.StringUtils;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.service.voter.VoterService;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class VoterServiceImpl extends AbstractServiceImpl implements VoterService {
	private static final Logger log = LoggerFactory.getLogger(VoterServiceImpl.class);

	@Override
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public Voter saveOrUpdate(Voter vol) throws ServiceValidationException {
		CoreUserDetails<?> userContext = getCurrentUser();
		if (!SecurityUtil.hasAllPermissions(PermissionType.VOTER_EDIT)) {
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

		Voter existingVol = isNew ? null : voterDAO.findRequiredByPrimaryKey(vol.getId());
		if (existingVol != null)
			voterDAO.detach(existingVol);

		vol.setFirstName(trim(capitalizeFully(vol.getFirstName())));
		vol.setMiddleName(trim(capitalizeFully(vol.getMiddleName())));
		vol.setLastName(trim(capitalizeFully(vol.getLastName())));
		vol.setSuffix(trim(capitalizeFully(vol.getSuffix())));
		vol.setZip(trim(vol.getZip()));

		if (StringUtils.trim(vol.getPhone()).equals(StringUtils.trim(vol.getUserProvidedPhone()))) {
			vol.setUserProvidedPhone(null);
		}

		if (StringUtils.trim(vol.getEmail()).equalsIgnoreCase(StringUtils.trim(vol.getUserProvidedEmail()))) {
			vol.setUserProvidedEmail(null);
		}

		if (StringUtils.trim(vol.getFirstName()).equalsIgnoreCase(StringUtils.trim(vol.getNickname()))) {
			vol.setNickname(null);
		}

		vol = voterDAO.saveOrUpdate(vol);

		return vol;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public void delete(long voterId) {
		voterDAO.delete(voterId);
	}

}
