package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractVoter<T extends AbstractVoter<T>> extends AbstractSimpleVoter<T> {
	private static final long serialVersionUID = 3222064615857480112L;

	// -------------------------------------- Fields

	// -------------------------------------- Constructors

	protected AbstractVoter() {
	}

	protected AbstractVoter(String lastName, String firstName, String middleName, String nameSuffix) {
		super(lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	// -------------------------------------- Accessor Methods

}
