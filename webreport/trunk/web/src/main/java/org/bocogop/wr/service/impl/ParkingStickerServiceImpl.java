package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.model.volunteer.ParkingSticker;
import org.bocogop.wr.service.ParkingStickerService;

@Service
public class ParkingStickerServiceImpl extends AbstractServiceImpl implements ParkingStickerService {
	private static final Logger log = LoggerFactory.getLogger(ParkingStickerServiceImpl.class);

	@Override
	public ParkingSticker saveOrUpdate(ParkingSticker parkingSticker) {
		return parkingStickerDAO.saveOrUpdate(parkingSticker);
	}

	@Override
	public void delete(long parkingStickerId) {
		parkingStickerDAO.delete(parkingStickerId);
	}

}
