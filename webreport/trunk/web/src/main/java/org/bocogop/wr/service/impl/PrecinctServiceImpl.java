package org.bocogop.wr.service.impl;

import org.bocogop.wr.model.Permission.PermissionType;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.service.PrecinctService;
import org.bocogop.wr.service.validation.ServiceValidationException;
import org.bocogop.wr.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrecinctServiceImpl extends AbstractServiceImpl implements PrecinctService {
	private static final Logger log = LoggerFactory.getLogger(PrecinctServiceImpl.class);

	@Override
	public Precinct saveOrUpdate(Precinct precinct) throws ServiceValidationException {
		/* Business-level validations */
		if (SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.PRECINCT_EDIT)) {
			precinct = precinctDAO.saveOrUpdate(precinct);
		}

		return precinct;
	}

	@Override
	public void delete(long precinctId) {
		precinctDAO.delete(precinctId);
	}

}
