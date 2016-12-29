package org.bocogop.wr.model.voluntaryService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceType.VoluntaryServiceTypeValue;

@Entity
@Immutable
@Table(name = "VoluntaryServiceTypes", schema = "wr")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@AttributeOverride(name = "name", column = @Column(name = "type") )
public class VoluntaryServiceType extends AbstractLookup<VoluntaryServiceType, VoluntaryServiceTypeValue> {

	private static final long serialVersionUID = -8678395783438462990L;

	public static enum VoluntaryServiceTypeValue implements LookupType {
		SERVICE(1), //
		PROGRAM(2), //
		SECTION(3);

		private long id;

		private VoluntaryServiceTypeValue(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}

	}

}
