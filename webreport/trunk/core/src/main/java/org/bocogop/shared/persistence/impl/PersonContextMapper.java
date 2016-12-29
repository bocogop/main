package org.bocogop.shared.persistence.impl;

import java.util.Arrays;
import java.util.TreeSet;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.stereotype.Component;

import org.bocogop.shared.model.ldap.LdapConstants;
import org.bocogop.shared.model.ldap.LdapPerson;

/**
 * Maps attributes from DirContextOperations to properties of LdapPerson.
 */
@Component
public class PersonContextMapper implements ContextMapper<LdapPerson>, LdapConstants {

	public LdapPerson mapFromContext(Object ctx) {
		DirContextOperations dirContext = (DirContextOperations) ctx;
		LdapPerson person = new LdapPerson();

		// NamingEnumeration<? extends Attribute> all =
		// dirContext.getAttributes().getAll();
		// try {
		// while (all.hasMore()) {
		// Attribute a = all.next();
		// for (int i = 0; i < a.size(); i++)
		// System.out.println(a.getID() + " = " + a.get(i));
		// }
		// } catch (NamingException e) {
		// e.printStackTrace();
		// }

		person.setDn(dirContext.getStringAttribute(DISTINGUISHED_NAME));
		person.setShortDnString(dirContext.getDn().toString());
		person.setFullName(dirContext.getStringAttribute(CN));
		person.setDisplayName(dirContext.getStringAttribute(DISPLAY_NAME));
		person.setLastName(dirContext.getStringAttribute(SN));
		person.setFirstName(dirContext.getStringAttribute(GIVEN_NAME));
		person.setMiddleName(dirContext.getStringAttribute(MIDDLE_NAME));
		person.setSamAccountName(dirContext.getStringAttribute(SAM_ACCOUNT_NAME));
		person.setUserPrincipalName(dirContext.getStringAttribute(USER_PRINCIPAL));
		person.setBadPasswordCount(dirContext.getStringAttribute(BAD_PWD_COUNT));
		person.setTitle(dirContext.getStringAttribute(TITLE));
		person.setDescription(dirContext.getStringAttribute(DESCRIPTION));
		person.setDepartment(dirContext.getStringAttribute(DEPARTMENT));
		person.setOffice(dirContext.getStringAttribute(OFFICE));
		person.setTelephoneNumber(dirContext.getStringAttribute(TELEPHONE_NUMBER));
		person.setEmail(dirContext.getStringAttribute(EMAIL));

		String[] groupsArray = dirContext.getStringAttributes(MEMBER_OF);
		if (groupsArray != null)
			person.setGroups(new TreeSet<>(Arrays.asList(groupsArray)));

		return person;
	}
}
