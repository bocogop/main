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
import org.bocogop.wr.model.organization.StdVAVSTitle.StdVAVSTitleValue;

@Entity
@Immutable
@Table(name = "WR_STD_VAVS_TITLE", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class StdVAVSTitle extends AbstractLookup<StdVAVSTitle, StdVAVSTitleValue> {

	private static final long serialVersionUID = 3126201039266411925L;

	public static enum StdVAVSTitleValue implements LookupType {
		NATIONAL_REPRESENTATIVE(1, "National Representative"), 
		DEPUTY_NATIONAL_REPRESENTATIVE(2, "Deputy National Representative"), 
		NATIONAL_CERTIFYING_OFFICIAL(3, "National Certifying Official"), 
		HONORARY_REPRESENTATIVE(4,"Honorary Representative"), 
		NATIONAL_CHAIRPERSON(5,"National Chairperson"),	
		NATIONAL_PRESIDENT(6,"National President");		
		
		private long id;
		private String name;

		private StdVAVSTitleValue(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}

}
