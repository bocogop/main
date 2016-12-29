package org.bocogop.shared.util;

import java.util.HashSet;
import java.util.Set;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.sds.VAFacility;

public final class StationsUtil {

	/*
	 * This may seem like overkill but it gives us a way to reference where we
	 * are doing this and also we can do add'l logic in the future if SDS
	 * structure changes - CPB
	 */
	public static String getThreeDigitStationNumber(String s) {
		return s.substring(0, 3);
	}

	public static boolean isThreeDigitStation(String s) {
		String threeDigitStationNumber = getThreeDigitStationNumber(s);
		return threeDigitStationNumber.equals(s);
	}

	public static VAFacility getVisnForFacilityOrAnyAncestor(VAFacility f) {
		VAFacility x = f;
		Set<Long> previouslyVisitedFacilityIds = new HashSet<>();

		for (int i = 0; i < 20; i++) {
			if (x.getFacilityType().getId() == ValidFacilityTypesEnum.VISN.getId())
				return x;

			VAFacility visn = x.getVisn();

			if (visn != null)
				return visn;
			previouslyVisitedFacilityIds.add(x.getId());

			VAFacility parent = x.getParent();
			if (parent == null || previouslyVisitedFacilityIds.contains(parent.getId()))
				break;

			x = parent;
		}

		return null;
	}

	// --------------------------------- Inner Classes

	public static enum ValidFacilityTypesEnum implements LookupType {
		CBOC(1009148, "CBOC", "COMMUNITY BASED OUTPATIENT CLINIC"), //
		DENT(1009160, "Dent", "DENTAL CLINIC"), //
		DOM(1009158, "Dom", "DOMICILIARY"), //
		MROC(1009174, "M&ROC", "MEDICAL AND REGIONAL OFFFICE CENTER"), //
		NHC(1009185, "NHC", "NURSING HOME CARE"), //
		OC(1009189, "OC", "OUTPATIENT CLINIC (INDEPENDENT)"), //
		OPC(1009197, "OPC", "OUT PATIENT CLINIC"), //
		ORC(1009198, "ORC", "OUTREACH CLINIC"), //
		VAMC(1009231, "VAMC", "VA MEDICAL CENTER"), //
		ROOC(1009209, "RO-OC", "REGIONAL OFFICE - OUTPATIENT CLINIC"), //
		VISN(1009241, "VISN", "VETERANS INTEGRATED SERVICE NETWORK");

		private long id;
		private String code;
		private String name;

		private ValidFacilityTypesEnum(long id, String code, String name) {
			this.id = id;
			this.code = code;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
	}

}
