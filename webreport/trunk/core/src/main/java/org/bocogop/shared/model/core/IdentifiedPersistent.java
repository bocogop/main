package org.bocogop.shared.model.core;

import java.io.Serializable;

public interface IdentifiedPersistent extends Persistent, Serializable {

	/**
	 * @return The ID of the object, or null if the object is not persistent
	 */
	Long getId();

	/**
	 * @param id
	 *            The ID to set (be careful!)
	 */
	void setId(Long id);

}