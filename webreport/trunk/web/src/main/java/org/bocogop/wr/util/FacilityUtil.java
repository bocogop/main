package org.bocogop.wr.util;

import org.bocogop.wr.model.facility.AbstractUpdateableLocation;

public final class FacilityUtil {

	public static String getLocationDisplayName(AbstractUpdateableLocation<?> f) {
		return f != null && "Location".equals(f.getScale()) ? f.getDisplayName() : "Main Facility";
	}

	public static long getLocationId(AbstractUpdateableLocation<?> f) {
		return f != null && "Location".equals(f.getScale()) ? f.getId() : -1;
	}

	public static String getFacilityDisplayName(AbstractUpdateableLocation<?> f) {
		if ("Facility".equals(f.getScale()))
			return f.getDisplayName();
		if ("Location".equals(f.getScale()))
			return f.getParent().getDisplayName();
		return "(unknown)";
	}

}
