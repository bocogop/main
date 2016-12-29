package org.bocogop.shared.model.lookup;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import org.bocogop.shared.model.lookup.InactiveReason.InactiveReasonType;

@Entity
@Immutable
@Table(name = "STD_INACTIVE_REASON", schema = "CORE")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class InactiveReason extends AbstractLookup<InactiveReason, InactiveReasonType> {
	private static final long serialVersionUID = -3428288935788213367L;

	public static enum InactiveReasonType implements LookupType {
		NO_LONGER_AN_EMPLOYEE(1), //
		NO_LONGER_ASSIGNED_TO_APP(2), //
		LACK_OF_ACTIVITY(3);

		private long id;

		private InactiveReasonType(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

	}

}
