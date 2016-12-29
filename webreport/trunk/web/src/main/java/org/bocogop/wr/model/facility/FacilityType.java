package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractIdentifiedPersistent;
import org.bocogop.shared.model.lookup.LookupType;

@Entity
@Table(name = "FacilityType", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class FacilityType extends AbstractIdentifiedPersistent<FacilityType> {
	private static final long serialVersionUID = -8678395783438462990L;

	private String description;

	@Override
	protected boolean requiredEquals(FacilityType oo) {
		return new EqualsBuilder().append(getDescription(), oo.getDescription()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDescription()).toHashCode();
	}

	@Column(length = 20)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static enum FacilityTypeValue implements LookupType {
		OBSOLETE_GAME(0), //
		OBSOLETE_TIMEKEEPING(1), //
		TIMEKEEPING(2),
		GAMES(3); // need to verify this val is in DB and it has ID 3
		
		private long id;

		private FacilityTypeValue(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}

		public static FacilityTypeValue getById(long id) {
			for (FacilityTypeValue v : values())
				if (v.getId() == id) return v;
			return null;
		}
		
	}

}
