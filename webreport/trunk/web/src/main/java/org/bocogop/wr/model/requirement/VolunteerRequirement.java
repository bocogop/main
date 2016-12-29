package org.bocogop.wr.model.requirement;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.bocogop.wr.model.facility.AbstractLocation.BasicLocationView;

@Entity
@Table(name = "VolunteerRequirement", schema = "wr")
public class VolunteerRequirement extends AbstractVolunteerRequirement {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class VolunteerRequirementView {
		public interface Basic extends BasicLocationView.Basic {
		}

		public interface Search extends Basic {
		}
	}

}
