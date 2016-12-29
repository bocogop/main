package org.bocogop.shared.persistence;

import org.junit.Test;

import org.bocogop.shared.AbstractCoreAppTest;
import org.bocogop.shared.model.ldap.LdapPerson;

public class TestLdapPersonDAO extends AbstractCoreAppTest {

	@Test
	public void testSearch() {
		LdapPerson person = ldapPersonDAO.findBySAMAccountName("vhaisdbarryc");
		System.out.println(person);
	}

}
