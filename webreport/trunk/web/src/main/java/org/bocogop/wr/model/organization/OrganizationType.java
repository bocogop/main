package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.model.organization.OrganizationType.OrganizationTypeValue;

@Entity
@Immutable
@Table(name = "WR_STD_VolunteerOrganizationTypes", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class OrganizationType extends AbstractLookup<OrganizationType, OrganizationTypeValue> {
	private static final long serialVersionUID = -6332598924348883474L;

	public static enum OrganizationTypeValue implements LookupType {
		// FIXWR add when we have organization types, do we need to fill all?
		ACADEMIC(1),
		CIVIC(2),
		CORPORATE(3),
		FAITH_BASED(4),
		OTHER(5),
		VETRANS_SERVICE(6),
		YOUTH(7);
	
		private long id;

		private OrganizationTypeValue(long id) {
		this.id = id;
		}

		public long getId() {
			return id;
		}

		public static OrganizationTypeValue getById(long id) {
			for (OrganizationTypeValue v : values())
				if (v.getId() == id) return v;
			return null;
		}
		
	}

}
