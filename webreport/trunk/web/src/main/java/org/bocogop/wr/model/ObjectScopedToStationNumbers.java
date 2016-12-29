package org.bocogop.wr.model;

import java.util.Collection;

/**
 * Interface for objects which reference one or more VAFacilities (or
 * Facilities). This can be used to auto-switch the station if needed on the
 * front-end, or for other validation purposes. CPB
 */
public interface ObjectScopedToStationNumbers {
	Collection<String> getScopedToStationNumbers();
}