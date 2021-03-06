package org.bocogop.shared.model;

public interface IdentifiedPersistent extends Persistent {

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