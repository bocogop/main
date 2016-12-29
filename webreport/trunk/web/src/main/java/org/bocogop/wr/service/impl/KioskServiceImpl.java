package org.bocogop.wr.service.impl;

import java.time.ZonedDateTime;
import java.util.Collection;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.service.KioskService;

@Service
public class KioskServiceImpl extends AbstractServiceImpl implements KioskService {
	private static final Logger log = LoggerFactory.getLogger(KioskServiceImpl.class);

	@Override
	public Kiosk saveOrUpdate(Kiosk k) throws ServiceValidationException {
		try {
			k = kioskDAO.saveOrUpdate(k);
			kioskDAO.flush();
			return k;
		} catch (PersistenceException e) {
			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException)
				throw new ServiceValidationException("facility.kiosk.duplicate");
			throw e;
		}
	}

	@Override
	public void delete(long kioskId) {
		kioskDAO.delete(kioskId);
	}

	@Override
	public void kioskCheckin(Collection<Long> kioskIds) {
		kioskDAO.bulkUpdateByCriteria(kioskIds, ZonedDateTime.now());
	}

	@Override
	public void registerKiosk(long kioskId) {
		Kiosk kiosk = kioskDAO.findRequiredByPrimaryKey(kioskId);
		kiosk.setRegistered(true);
		kiosk = kioskDAO.saveOrUpdate(kiosk);
	}

}
