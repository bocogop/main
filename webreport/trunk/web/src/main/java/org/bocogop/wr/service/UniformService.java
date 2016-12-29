package org.bocogop.wr.service;

import org.bocogop.wr.model.volunteer.Uniform;

public interface UniformService {

	/**
	 * @param uniform
	 *            The uniform to save or update
	 * @return The updated uniform after it's been merged
	 */
	Uniform saveOrUpdate(Uniform uniform);

	/**
	 * Deletes the Uniform with the specified parkingStickerId
	 * 
	 * @param uniformId
	 *            The ID of the uniform to delete
	 */
	void delete(long uniformId);

}
