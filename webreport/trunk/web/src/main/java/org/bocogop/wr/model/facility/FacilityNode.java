package org.bocogop.wr.model.facility;

import java.util.SortedSet;

public interface FacilityNode<T> extends Comparable<T> {
	
	SortedSet<? extends FacilityNode<?>> getFacilityChildren();

	Long getId();

	String getDisplayName();

	boolean isActive();
}