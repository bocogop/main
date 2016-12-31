package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author Connor
 *
 */
@Entity
@Table(name = "Voter")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Voter extends AbstractVoter<Voter> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class VoterView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}

		public interface Demographics extends Basic {
		}
	}

	// -------------------------------------- Fields

	// -------------------------------------- Constructors

	public Voter() {
	}

	public Voter(String lastName, String firstName, String middleName, String nameSuffix) {
		super(lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Accessor Methods

}
