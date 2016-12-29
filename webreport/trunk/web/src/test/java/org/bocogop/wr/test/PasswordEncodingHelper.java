package org.bocogop.wr.test;

import org.junit.Test;

import org.bocogop.wr.util.HashUtils;

public class PasswordEncodingHelper {

	/*
	 * This is a utility / helper class to generate the SHA-encoded passwords
	 * that CISS stores in the APP_USER table for when LDAP is offline. CPB
	 */
	@Test
	public void encodePassword() throws Exception {
		/* User names MUST be in uppercase! */
		System.out.println("P442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "P442", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("P442660 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "P442660", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("M442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "M442", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("M442660 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "M442660", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("S442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "S442", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("S442660 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "S442660", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("PS442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "PS442", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("PM442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "PM442", HashUtils.DEFAULT_ALGORITHM)));

		System.out.println("PSM442 " + " - "
		/* User names MUST be in uppercase! */
				+ String.valueOf(HashUtils.hash("Password1", "PSM442", HashUtils.DEFAULT_ALGORITHM)));

	}

}
