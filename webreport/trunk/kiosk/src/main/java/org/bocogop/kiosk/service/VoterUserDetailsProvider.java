package org.bocogop.kiosk.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.service.AbstractAppServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VoterUserDetailsProvider extends AbstractAppServiceImpl {

	@Autowired
	private VoterDAO voterDAO;

	public Voter retrieveUser(String voterId, UsernamePasswordAuthenticationToken authentication) {
		List<Voter> vols = voterDAO.findByCriteria(voterId, null, null, null, false, false, null, null, null, null,
				null, null, null, null);
		if (vols.isEmpty())
			throw new UsernameNotFoundException("Sorry, that voter ID was not found.");
		Voter v = vols.get(0);

		Role voterRole = roleDAO.findByLookup(RoleType.VOTER);
		Set<Permission> permissions = voterRole.getPermissions();

		List<GrantedAuthority> allAuthorities = new ArrayList<>(permissions.size() + 1);
		allAuthorities.add(voterRole);
		allAuthorities.addAll(permissions);
		v.setAuthorities(Collections.unmodifiableList(allAuthorities));

		return v;
	}

}
