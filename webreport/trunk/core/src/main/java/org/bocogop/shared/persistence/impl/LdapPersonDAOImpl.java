package org.bocogop.shared.persistence.impl;

import static org.bocogop.shared.model.ldap.LdapConstants.GIVEN_NAME;
import static org.bocogop.shared.model.ldap.LdapConstants.OBJECT_CLASS;
import static org.bocogop.shared.model.ldap.LdapConstants.SAM_ACCOUNT_NAME;
import static org.bocogop.shared.model.ldap.LdapConstants.SN;
import static org.bocogop.shared.model.ldap.LdapConstants.USER;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.ldap.LdapPerson;
import org.bocogop.shared.persistence.LdapPersonDAO;

@Repository
public class LdapPersonDAOImpl implements LdapPersonDAO {

	@Autowired
	private LdapTemplate ldapTemplate;
	@Autowired
	private PersonContextMapper personContextMapper;
	@Value("${ldapBase}")
	private String ldapBase;
	@Value("${ldapReadServerUrl1}")
	private String ldapReadServerUrl1;
	@Value("${ldapReadServerUrl2}")
	private String ldapReadServerUrl2;
	@Value("${ldapMaxSearchResults}")
	private int maxLdapSearchResults;

	/**
	 * Searches ActiveDirectory by the given sAMAccountName
	 * 
	 * @param sAMAccountName
	 *            the sAmAccountaName to search by
	 * @return the found LdapPerson
	 */
	public LdapPerson findBySAMAccountName(String sAMAccountName) {
		LdapQuery q = query() //
				.where(OBJECT_CLASS).is(USER) //
				.and(SAM_ACCOUNT_NAME).is(sAMAccountName);
		List<LdapPerson> results = ldapTemplate.search(q, personContextMapper);

		return results.size() > 0 ? results.get(0) : null;
	}

	/**
	 * Searches ActiveDirectory by the given sAMAccountName
	 * 
	 * @param sAMAccountName
	 *            the sAmAccountaName to search by
	 * 
	 * @return the found LdapPerson
	 */
	public List<LdapPerson> findByName(String firstName, boolean wildcardFirstName, String requiredLastName,
			boolean wildcardLastName) {
		ContainerCriteria q = query().countLimit(maxLdapSearchResults) //
				.where(OBJECT_CLASS).is(USER); //
		if (wildcardLastName) {
			q = q.and(SN).whitespaceWildcardsLike(requiredLastName);
		} else {
			q = q.and(SN).is(requiredLastName);
		}

		if (StringUtils.isNotBlank(firstName)) {
			if (wildcardFirstName) {
				q = q.and(GIVEN_NAME).whitespaceWildcardsLike(firstName);
			} else {
				q = q.and(GIVEN_NAME).is(firstName);
			}
		}

		List<LdapPerson> results = ldapTemplate.search(q, personContextMapper);
		return results;
	}

	/**
	 * Authenticate by logging into the ldap server
	 * 
	 * @param userDn
	 * @param password
	 * @return
	 */
	public boolean authenticate(String userDn, String password) throws CommunicationException {
		DirContext ctx = null;
		try {
			LdapContextSource context = new LdapContextSource();
			context.setUrls(new String[] { ldapReadServerUrl1, ldapReadServerUrl2 });
			context.setBase(ldapBase);
			context.setUserDn(userDn);
			context.setPassword(password);
			context.setPooled(false);
			context.afterPropertiesSet();
			ctx = context.getReadOnlyContext();
			return true;
		} catch (Exception e) {
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (e instanceof CommunicationException && (rootCause instanceof UnknownHostException
					|| rootCause instanceof NoRouteToHostException || rootCause instanceof ConnectException)) {
				throw (CommunicationException) e;
			}

			return false;
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					// ignore
				}
			}
		}
	}
}
