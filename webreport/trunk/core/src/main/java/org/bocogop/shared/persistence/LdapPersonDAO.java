package org.bocogop.shared.persistence;

import java.util.List;

import org.bocogop.shared.model.ldap.LdapPerson;

public interface LdapPersonDAO {

	LdapPerson findBySAMAccountName(String sAMAccountName);

	List<LdapPerson> findByName(String firstName, boolean wildcardFirstName, String lastName, boolean wildcardLastName);

	boolean authenticate(String userDn, String password);

}
