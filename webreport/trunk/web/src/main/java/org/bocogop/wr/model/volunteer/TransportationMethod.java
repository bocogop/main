package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.volunteer.TransportationMethod.TransportationMethodType;

@Entity
@Immutable
@Table(name = "WR_STD_TRANSPORTATION_METHOD", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class TransportationMethod extends AbstractLookup<TransportationMethod, TransportationMethodType> {

	private static final long serialVersionUID = 3126201039266411925L;

	public static enum TransportationMethodType implements LookupType {
		PRIVATELY_OWNED_VEHICLE(1, "Privately Owned Vehicle"), //
		PUBLIC_TRANSPORTATION(2, "Public Transportation"), //
		WALK_OR_BICYCLE(3, "Walk/Bicycle"), //
		OTHER(4, "Other") //
		;

		private long id;
		private String name;

		private TransportationMethodType(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public long getId() {
			return id;
		}

	}

}
