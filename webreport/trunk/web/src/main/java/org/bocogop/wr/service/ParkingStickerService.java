package org.bocogop.wr.service;

import org.bocogop.wr.model.volunteer.ParkingSticker;

public interface ParkingStickerService {

	/**
	 * @param parkingSticker
	 *            The parking sticker to save or update
	 * @return The updated parking sticker after it's been merged
	 */
	ParkingSticker saveOrUpdate(ParkingSticker parkingSticker);

	/**
	 * Deletes the Parking Sticker with the specified parkingStickerId
	 * 
	 * @param parkingStickerId
	 *            The ID of the parking sticker to delete
	 */
	void delete(long parkingStickerId);

}
