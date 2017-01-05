package org.bocogop.kiosk.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.voter.MultiVoterTempUserDetails;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.service.AbstractAppServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VoterUserDetailsProvider extends AbstractAppServiceImpl {

	@Autowired
	private VoterDAO voterDAO;

	public UserDetails retrieveUser(String combinedUsername, UsernamePasswordAuthenticationToken authentication) {
		String[] usernameTokens = combinedUsername.split("\\|", -1);
		if (usernameTokens.length != 3)
			throw new UsernameNotFoundException("Invalid username specified");

		String voterId = usernameTokens[0];
		String firstName = usernameTokens[1];
		String lastName = usernameTokens[2];

		Integer birthYear = null;

		Object credentials = authentication.getCredentials();
		if (credentials instanceof String) {
			try {
				birthYear = Integer.parseInt(((String) credentials).trim());
			} catch (NumberFormatException e) {
				throw new BadCredentialsException("Invalid birth year specified");
			}
		}

		Voter v = null;

		if (StringUtils.isNotBlank(voterId)) {
			List<Voter> vols = voterDAO.findByCriteria(voterId, null, null, null, false, false, birthYear, null, null, null,
					null, null, null, null);
			if (vols.size() > 1) {
				return new MultiVoterTempUserDetails(vols);
			} else if (vols.size() == 1) {
				v = vols.get(0);
			}
		} else {
			List<Voter> vols = voterDAO.findByCriteria(null, firstName, null, lastName, true, true, birthYear, null,
					null, null, null, null, null, null);
			if (vols.size() > 1) {
				return new MultiVoterTempUserDetails(vols);
			} else if (vols.size() == 1) {
				v = vols.get(0);
			}
		}

		if (v == null)
			throw new BadCredentialsException("Sorry, no voter was found.");

		Role voterRole = roleDAO.findByLookup(RoleType.VOTER);
		Set<Permission> permissions = voterRole.getPermissions();

		List<GrantedAuthority> allAuthorities = new ArrayList<>(permissions.size() + 1);
		allAuthorities.add(voterRole);
		allAuthorities.addAll(permissions);
		v.setAuthorities(Collections.unmodifiableList(allAuthorities));

		return v;
	}

}
