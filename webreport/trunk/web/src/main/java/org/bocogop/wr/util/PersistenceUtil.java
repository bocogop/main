package org.bocogop.wr.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bocogop.wr.model.core.IdentifiedPersistent;
import org.bocogop.wr.persistence.usertype.CodedEnum;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class PersistenceUtil {

	public static <T extends IdentifiedPersistent> Set<Long> translateObjectsToIds(Set<T> o) {
		Set<Long> ids = new HashSet<>(o.size());
		for (T item : o)
			ids.add(item.getId());
		return ids;
	}

	public static <T extends IdentifiedPersistent> List<Long> translateObjectsToIds(Collection<T> o) {
		List<Long> ids = new ArrayList<>(o.size());
		for (T item : o)
			ids.add(item.getId());
		return ids;
	}

	public static <T extends IdentifiedPersistent> Map<Long, T> translateObjectsToIdMap(Collection<T> o) {
		Map<Long, T> idMap = new LinkedHashMap<>(o.size());
		for (T item : o)
			idMap.put(item.getId(), item);
		return idMap;
	}

	public static <T extends CodedEnum> Set<String> translateCodedEnumToCodes(Set<T> o) {
		Set<String> codes = new HashSet<>(o.size());
		for (T item : o) {
			// piece of crap Fortify.
			CodedEnum x = (CodedEnum) item;
			codes.add(x.getCode());
		}
		return codes;
	}

	public static <T extends CodedEnum> List<String> translateCodedEnumToCodes(Collection<T> o) {
		List<String> ids = new ArrayList<>(o.size());
		for (T item : o) {
			// piece of crap Fortify.
			CodedEnum x = (CodedEnum) item;
			ids.add(x.getCode());
		}
		return ids;
	}

	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

}
