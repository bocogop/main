package org.bocogop.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.bocogop.shared.model.AuditedPersistent;

public class CollectionUtil {

	public static final Comparator<AuditedPersistent> BY_INVERSE_CREATION_DATE = new Comparator<AuditedPersistent>() {
		public int compare(AuditedPersistent o1, AuditedPersistent o2) {
			return -(o1.getCreatedDate().compareTo(o2.getCreatedDate()));
		}
	};

	public static interface SynchronizeCollectionsOps<T, U> {

		@SuppressWarnings("unchecked")
		default T convert(U u) {
			return (T) u;
		}

		default void add(Collection<T> coll, T item) {
			coll.add(item);
		}

		default void remove(Iterator<T> it, T itemToRemove) {
			it.remove();
		}

	}

	@SafeVarargs
	public static <T, U> void synchronizeCollections(Collection<T> existingItems, Collection<U> newItems,
			SynchronizeCollectionsOps<T, U>... operations) {
		if (newItems == null) newItems = new ArrayList<>();
		
		SynchronizeCollectionsOps<T, U> ops = ArrayUtils.isNotEmpty(operations) ? operations[0]
				: new SynchronizeCollectionsOps<T, U>() {
				};

		List<U> itemsToAdd = new ArrayList<>(newItems);
		List<T> itemsToRemove = new ArrayList<>(existingItems);

		for (Iterator<U> it = itemsToAdd.iterator(); it.hasNext();) {
			U u = it.next();
			T uConverted = ops.convert(u);
			if (existingItems.contains(uConverted)) {
				itemsToRemove.remove(uConverted);
				it.remove();
			}
		}

		/* process items to remove */
		for (Iterator<T> it = existingItems.iterator(); it.hasNext();) {
			T next = it.next();
			if (itemsToRemove.contains(next)) {
				ops.remove(it, next);
			}
		}

		/* process items to add */
		for (U u : itemsToAdd) {
			T itemToAdd = ops.convert(u);
			ops.add(existingItems, itemToAdd);
		}
	}

	public static interface SynchronizeMapsOps<T, U, V, W> {
		@SuppressWarnings("unchecked")
		default T convertKey(V v) {
			return (T) v;
		}

		@SuppressWarnings("unchecked")
		default U convertValue(W w) {
			return (U) w;
		}
	}

	@SafeVarargs
	public static <T, U, V, W> void synchronizeMaps(Map<T, U> existingItems, Map<V, W> newItems,
			SynchronizeMapsOps<T, U, V, W>... operations) {
		SynchronizeMapsOps<T, U, V, W> ops = ArrayUtils.isNotEmpty(operations) ? operations[0]
				: new SynchronizeMapsOps<T, U, V, W>() {
				};

		List<V> keysToAdd = new ArrayList<>(newItems.keySet());
		List<T> keysToRemove = new ArrayList<>(existingItems.keySet());

		for (Iterator<V> it = keysToAdd.iterator(); it.hasNext();) {
			V v = it.next();
			T t = ops.convertKey(v);
			if (existingItems.containsKey(t)) {
				keysToRemove.remove(t);
				it.remove();
			}
		}

		/* process items to remove */
		for (Iterator<T> it = existingItems.keySet().iterator(); it.hasNext();)
			if (keysToRemove.contains(it.next()))
				it.remove();

		/* process items to add */
		for (V v : keysToAdd) {
			T t = ops.convertKey(v);
			W w = newItems.get(v);
			U wConverted = ops.convertValue(w);
			existingItems.put(t, wConverted);
		}
	}

	/**
	 * This method returns the value associated with the specified key in the
	 * specified map. If the value does not exist, the map is populated with the
	 * specified defaultValue and this value is returned.
	 * 
	 * @param <K>
	 *            The type of the key in the map
	 * @param <T>
	 *            The type of the value in the map
	 * @param key
	 *            The key to use in the map
	 * @param map
	 *            The map to search
	 * @param defaultValue
	 *            The default value to insert in the map if the map doesn't yet
	 *            contain the key
	 * @return
	 */
	public static <K, T> T getOrInsert(K key, Map<K, T> map, T defaultValue) {
		T currentValue = map.get(key);
		if (currentValue == null) {
			currentValue = defaultValue;
			map.put(key, currentValue);
		}
		return currentValue;
	}

	/**
	 * Analyzes the specified timeline for groups of sequential items that are
	 * considered duplicates. A list of the separate groups is returned.
	 * "Sequential" here is consistent with the order in which the specified
	 * collection is Iterable. The returned groups will be iterable in the same
	 * order as the items in the specified collection.
	 * <p>
	 * The specified comparator is used to determine if two items are
	 * duplicates. If zero is returned, the items are considered duplicates. A
	 * nonzero value means the items are different.
	 * 
	 * @param collection
	 * @param duplicateComparator
	 */
	public static <T> List<Collection<T>> getSequentialDuplicates(Collection<T> collection,
			Comparator<? super T> duplicateComparator) {
		List<Collection<T>> itemsToRemove = new ArrayList<>();
		List<T> currentGroup = null;

		T previousItem = null;
		for (Iterator<T> it = collection.iterator(); it.hasNext();) {
			T item = it.next();

			if (previousItem == null) {
				previousItem = item;
				continue;
			}

			if (duplicateComparator.compare(item, previousItem) == 0) {
				if (currentGroup == null) {
					currentGroup = new ArrayList<>();
					itemsToRemove.add(currentGroup);
					currentGroup.add(previousItem);
				}
				currentGroup.add(item);
			} else {
				if (currentGroup != null) {
					currentGroup = null;
				}
			}

			previousItem = item;
		}

		return itemsToRemove;
	}
}