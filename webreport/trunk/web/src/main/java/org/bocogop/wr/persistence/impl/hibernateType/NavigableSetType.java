package org.bocogop.wr.persistence.impl.hibernateType;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

/* Currently unused, waiting on annotation-based support for this in post-4.1.2 Hibernate. I'm sure there's a JIRA out there somewhere. See package-info.java. CPB */
public class NavigableSetType implements UserCollectionType {

	public NavigableSetType() {
	}

	// could be common for all collection implementations.
	public boolean contains(Object collection, Object obj) {
		Set<?> set = (Set<?>) collection;
		return set.contains(obj);
	}

	// could be common for all collection implementations.
	public Iterator<?> getElementsIterator(Object collection) {
		return ((Set<?>) collection).iterator();
	}

	// common for list-like collections.
	public Object indexOf(Object collection, Object obj) {
		return null;
	}

	// factory method for certain collection type.
	public Object instantiate() {
		return new TreeSet<>();
	}

	// standard wrapper for collection type.
	public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister persister)
			throws HibernateException {
		// Use hibernate's built in persistent set implementation
		// wrapper
		return new PersistentSet(session);
	}

	// could be common implementation for all collection implementations
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object replaceElements(Object collectionA, Object collectionB, CollectionPersister persister, Object owner,
			Map copyCache, SharedSessionContractImplementor implementor) throws HibernateException {
		Set setA = (Set) collectionA;
		Set setB = (Set) collectionB;
		setB.clear();
		setB.addAll(setA);
		return setB;
	}

	// standard wrapper for collection type.
	public PersistentCollection wrap(SharedSessionContractImplementor session, Object colllection) {
		// Use hibernate's built in persistent set implementation
		// wrapper.
		return new PersistentSet(session, (Set<?>) colllection);
	}

	@Override
	public Object instantiate(int anticipatedSize) {
		return new TreeSet<>();
	}
}