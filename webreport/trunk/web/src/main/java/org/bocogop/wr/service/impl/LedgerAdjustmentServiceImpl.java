package org.bocogop.wr.service.impl;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.expenditure.LedgerAdjustment;
import org.bocogop.wr.service.LedgerAdjustmentService;

@Service
public class LedgerAdjustmentServiceImpl extends AbstractServiceImpl implements LedgerAdjustmentService {
	private static final Logger log = LoggerFactory.getLogger(LedgerAdjustmentServiceImpl.class);

	@Override
	public LedgerAdjustment saveOrUpdate(LedgerAdjustment adjustment) throws ServiceValidationException {
		if (adjustment.getRequestDate().isAfter(LocalDate.now(getFacilityTimeZone())))
			throw new ServiceValidationException("ledgerAdjustment.error.requestDateInFuture");
		if (adjustment.getRequestDate().isBefore(dateUtil.getCurrentFiscalYearStartDate(getFacilityTimeZone())))
			throw new ServiceValidationException("ledgerAdjustment.error.requestDateBeforeCurrentFY");
		if (!adjustment.isPersistent()) {
			AppUser appUser = getCurrentUserAsOrNull(AppUser.class);
			if (appUser == null)
				throw new IllegalStateException("Missing required current user");
			adjustment.setOriginator(appUser);
		}
		adjustment = ledgerAdjustmentDAO.saveOrUpdate(adjustment);
		return adjustment;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.EXPENDITURE_DELETE + "')")
	public void delete(long ledgerAdjustmentId) throws ServiceValidationException {
		ledgerAdjustmentDAO.delete(ledgerAdjustmentId);
	}

}
