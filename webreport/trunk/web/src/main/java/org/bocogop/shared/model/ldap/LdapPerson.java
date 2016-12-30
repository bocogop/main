package org.bocogop.shared.model.ldap;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class LdapPerson implements Serializable {
	private static final long serialVersionUID = -8953862790009843528L;

	public static String DISABLED_USERS = "OU=Disabled Users";
	public static String DISABLED_ACCOUNTS = "OU=Disabled Accounts";

	private String dn;
	private String shortDn; // dn minus base
	private String samAccountName;
	private String userPrincipalName;
	private String fullName;
	private String displayName;
	private String lastName;
	private String firstName;
	private String middleName;
	private String badPasswordCount;
	private String title;
	private String department;
	private String description;
	private String office;
	private String telephoneNumber;
	private String email;
	private Set<String> groups;

	public boolean isDisabled() {
		if (groups != null && (groups.contains(DISABLED_USERS) || groups.contains(DISABLED_ACCOUNTS))) {
			return true;
		}
		return false;
	}

	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * @return Returns the Distinguished Name of the Ldap Person not including
	 *         the base portion of the DN, specifically:
	 *         DC=vha,DC=med,DC=va,DC=gov.
	 */
	public String getShortDn() {
		return shortDn;
	}

	/**
	 * @param shortDn
	 *            The Distinguished Name to set not including the base portion
	 *            specifically DC=vha,DC=med,DC=va,DC=gov.
	 */
	public void setShortDnString(String shortDn) {
		this.shortDn = shortDn;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getBadPasswordCount() {
		return badPasswordCount;
	}

	public void setBadPasswordCount(String badPasswordCount) {
		this.badPasswordCount = badPasswordCount;
	}

	public String getSamAccountName() {
		return samAccountName;
	}

	public void setSamAccountName(String samAccountName) {
		this.samAccountName = samAccountName;
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return Returns a set of Group DNs or which the Ldap Person is a member.
	 */
	public Set<String> getGroups() {
		if (groups == null)
			groups = new TreeSet<String>();
		return groups;
	}

	/**
	 * @param groups
	 *            The set of Group DNs to set.
	 */
	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}

}
