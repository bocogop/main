package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This class is a convenience superclass for all non-versioned persistent
 * business objects. For versioned persistent business objects, see
 * AbstractPersistentWithVersion. To maintain independence from the persistence
 * layer, this class contains only a list of properties and no "helper"
 * functions that might throw vendor-specific exceptions (e.g.
 * HibernateExceptions).
 * 
 * @author Connor Barry
 */
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public abstract class AbstractBusinessKeyOwner<T> {

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null
				/*
				 * prevents polymorphic comparisons; that's ok thus far - CPB
				 */
				|| !Hibernate.getClass(o).equals(Hibernate.getClass(this)))
			return false;
		T oo = (T) o;
		return requiredEquals(oo);
	}

	protected abstract boolean requiredEquals(T oo);

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return requiredHashCode();
	}

	protected abstract int requiredHashCode();

	public void initialize(Object o) {
		if (!Hibernate.isInitialized(o))
			Hibernate.initialize(o);
	}

}
