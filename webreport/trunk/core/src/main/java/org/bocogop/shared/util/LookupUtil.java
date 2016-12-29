package org.bocogop.shared.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.Lookup;
import org.bocogop.shared.model.lookup.LookupType;

public class LookupUtil {

	public static final Comparator<AbstractLookup<?, ?>> BY_NAME = new Comparator<AbstractLookup<?, ?>>() {
		public int compare(AbstractLookup<?, ?> o1, AbstractLookup<?, ?> o2) {
			if (o1.equals(o2))
				return 0;
			return o1.getName().compareTo(o2.getName()) > 0 ? 1 : -1;
		}
	};

	public static List<Long> translateToIDs(Collection<? extends AbstractLookup<?, ?>> u) {
		List<Long> results = new ArrayList<>();
		if (u == null)
			return results;

		for (AbstractLookup<?, ?> i : u)
			results.add(i.getId());

		return results;
	}

	public static List<Long> translateToIDs(AbstractLookup<?, ?>[] u) {
		List<Long> results = new ArrayList<>();
		if (u == null)
			return results;

		for (AbstractLookup<?, ?> i : u)
			results.add(i.getId());

		return results;
	}

	public static List<Long> translateTypesToIDs(Collection<? extends LookupType> u) {
		List<Long> results = new ArrayList<>();
		if (u == null)
			return results;

		for (LookupType i : u)
			results.add(i.getId());

		return results;
	}

	public static List<Long> translateTypesToIDs(LookupType[] u) {
		List<Long> results = new ArrayList<>();
		if (u == null)
			return results;

		for (LookupType i : u)
			results.add(i.getId());

		return results;
	}

	public static <T extends LookupType> T getById(Class<T> clazz, long id) {
		T[] vals = clazz.getEnumConstants();
		for (T val : vals)
			if (id == val.getId())
				return val;
		return null;
	}

	public static <T extends LookupType> Map<Long, T> translateIDsToTypes(Class<T> clazz, String... codes) {
		if (codes == null)
			return new HashMap<>();

		return translateIDsToTypes(clazz, Arrays.asList(codes));
	}

	public static <T extends LookupType> Map<Long, T> translateIDsToTypes(Class<T> clazz, Collection<String> codes) {
		Map<Long, T> results = new HashMap<>();
		if (codes == null)
			return results;

		T[] vals = clazz.getEnumConstants();
		for (T val : vals)
			if (codes.contains(val.getId()))
				results.put(val.getId(), val);
		return results;
	}

	public static <T extends LookupType> Set<T> translateLookupsToTypes(Collection<? extends Lookup<?, T>> lookups) {
		Set<T> results = new HashSet<>();
		for (Lookup<?, T> lookup : lookups)
			results.add(lookup.getLookupType());
		return results;
	}

	public static <T extends LookupType> boolean isTypeInLookups(T type, Collection<? extends Lookup<?, ?>> lookups) {
		for (Lookup<?, ?> l : lookups)
			if (l.getId() != null && l.getId() == type.getId())
				return true;
		return false;
	}
}