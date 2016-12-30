package org.bocogop.shared.model.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is a convenience superclass for all business objects that are
 * identified by a single Long id. When creating self-contained business objects
 * that are mapped to their own table via Hibernate, consider using
 * AbstractPersistent, which enforces equals() and hashCode(). For versioned
 * Hibernate business objects, see AbstractPersistentWithVersion.
 * 
 * @author Connor Barry
 */
@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public abstract class AbstractIdentifiedPersistent<T> extends AbstractBusinessKeyOwner<T>
		implements IdentifiedPersistent {
	private static final long serialVersionUID = 8486933439888151406L;

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

	// ------------------------ Fields

	private Long id;

	// ------------------------ Business Methods

	@Transient
	@JsonIgnore
	public boolean isPersistent() {
		return getId() != null;
	}

	// ------------------------ Accessor Methods

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty
	@XmlTransient
	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

}
