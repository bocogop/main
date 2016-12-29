package org.bocogop.shared.model.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This class is a convenience superclass for all persistent objects that are
 * derived from other objects (i.e. objects mapped to views in the database). We
 * let the child classes define what type of ID column they have.
 * 
 * @author Connor Barry
 */
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public abstract class AbstractDerivedPersistent<T> extends AbstractBusinessKeyOwner<T> implements Persistent {
	private static final long serialVersionUID = -2441482181122221551L;

	/**
	 * Convenience method to return the ID of an object, or null if the
	 * specified object is null. This is helpful in equals/hashCode/compareTo
	 * methods where we want to retrieve only the ID of a proxied object without
	 * asking JPA to initialize that object. CPB
	 */
	public static Long nullSafeGetId(IdentifiedPersistent o) {
		return o == null ? null : o.getId();
	}

	/**
	 * Convenience method to return the double equivalent of a BigDecimal, if
	 * that narrowing is acceptable in equals/hashCode methods. This allows for
	 * BigDecimal comparisons by value, ignoring the scale. We could compare
	 * using BigDecimal.compareTo() but the hashCode() implementations would
	 * still be inconsistent across two values whose decimal value was the same
	 * but whose scale was different. CPB
	 */
	public static Double nullSafeGetDoubleValue(BigDecimal o) {
		return o == null ? null : o.doubleValue();
	}

	/**
	 * Convenience method to return the lowercase version of the specified
	 * String, or null if the specified String is null.
	 */
	public static String nullSafeLowercase(String s) {
		return s == null ? null : s.toLowerCase();
	}
	
	@Transient
	@Override
	public boolean isPersistent() {
		return true;
	}
}
