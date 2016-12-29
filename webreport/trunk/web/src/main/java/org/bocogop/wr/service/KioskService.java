package org.bocogop.wr.service;

import java.util.Collection;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.Kiosk;

public interface KioskService {

	Kiosk saveOrUpdate(Kiosk k) throws ServiceValidationException;

	void delete(long kioskId);

	void kioskCheckin(Collection<Long> kioskIds);

	void registerKiosk(long kioskId);

}
