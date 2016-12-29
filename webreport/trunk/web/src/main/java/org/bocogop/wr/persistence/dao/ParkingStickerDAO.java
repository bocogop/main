package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.volunteer.ParkingSticker;

public interface ParkingStickerDAO extends CustomizableSortedDAO<ParkingSticker> {

	List<ParkingSticker> findByCriteria(String stickerNumber, State state, String licensePlate);

}
