package org.bocogop.wr.model.organization;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.ObjectScopedToStationNumbers;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;

public interface BasicOrganization
		extends AuditedVersionedPersistent, Comparable<BasicOrganization>, ObjectScopedToStationNumbers {

	Organization getRootOrganization();

	boolean isActive();

	String getDisplayName();

	String getAddressMultilineDisplay();

	String getScale();

	ScopeType getScope();

	Facility getFacility();

	String getAbbreviation();

	String getName();

	String getFullName();

	boolean isInactive();

	List<VolunteerOrganization> getVolunteerOrganizations();

	String getAddressLine1();

	String getAddressLine2();

	String getCity();

	State getState();

	String getZip();

	String getEmail();

	String getPhone();

	List<WorkEntry> getWorkEntries();

	String getContactName();

	String getContactTitle();

}