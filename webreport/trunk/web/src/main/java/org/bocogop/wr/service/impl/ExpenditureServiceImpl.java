package org.bocogop.wr.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.expenditure.Expenditure;
import org.bocogop.wr.service.ExpenditureService;

@Service
public class ExpenditureServiceImpl extends AbstractServiceImpl implements ExpenditureService {
	private static final Logger log = LoggerFactory.getLogger(ExpenditureServiceImpl.class);

	@Override
	public Expenditure saveOrUpdate(Expenditure expenditure) throws ServiceValidationException {
		if (expenditure.getRequestDate().isAfter(LocalDate.now(getFacilityTimeZone())))
			throw new ServiceValidationException("expenditure.error.requestDateInFuture");
		
		if (expenditure.getRequestDate().isBefore(dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone())))
			throw new ServiceValidationException("expenditure.error.requestDateBeforeCurrentFY");
		if (StringUtils.isBlank(expenditure.getTransactionId())
				&& StringUtils.isBlank(expenditure.getPurchaseOrderNumber()))
			throw new ServiceValidationException("expenditure.error.requiredTransactionOrPurchaseOrder");
		if (expenditure.getAmount().compareTo(BigDecimal.ZERO) != 1)
			throw new ServiceValidationException("expenditure.error.amountNonPositive");

		expenditure = expenditureDAO.saveOrUpdate(expenditure);
		return expenditure;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.EXPENDITURE_DELETE + "')")
	public void delete(long expenditureId) throws ServiceValidationException {
		Expenditure expenditure = expenditureDAO.findRequiredByPrimaryKey(expenditureId);
		if (expenditure.getRequestDate().isBefore(dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone())))
			throw new ServiceValidationException("expenditure.error.requestDateBeforeCurrentFY");
		
		expenditureDAO.delete(expenditureId);
	}

}
