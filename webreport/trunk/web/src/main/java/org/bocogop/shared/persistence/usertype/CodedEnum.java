package org.bocogop.shared.persistence.usertype;

/**
 * Flags an enum as having a code which we want to use when storing or
 * retrieving from the database
 */
public interface CodedEnum {

	String getCode();

}
