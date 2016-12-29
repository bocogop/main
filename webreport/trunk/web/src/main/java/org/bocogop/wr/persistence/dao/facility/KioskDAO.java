package org.bocogop.wr.persistence.dao.facility;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface KioskDAO extends CustomizableSortedDAO<Kiosk> {

	int bulkUpdateByCriteria(Collection<Long> kioskIds, ZonedDateTime lastPrinterStatusCheck);

	List<Kiosk> findByCriteria(Boolean registrationStatus);

}
