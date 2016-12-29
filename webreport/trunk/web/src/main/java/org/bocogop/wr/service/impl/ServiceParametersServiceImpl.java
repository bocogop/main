package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;
import org.bocogop.wr.service.ServiceParametersService;

@Service
public class ServiceParametersServiceImpl extends AbstractServiceImpl implements ServiceParametersService {
	private static final Logger log = LoggerFactory.getLogger(ServiceParametersServiceImpl.class);

	@Override
	public VoluntaryServiceParameters saveOrUpdate(VoluntaryServiceParameters serviceParameters)
			throws ServiceValidationException {
		return serviceParametersDAO.saveOrUpdate(serviceParameters);
	}

	@Override
	public void delete(long serviceParametersId) {
		serviceParametersDAO.delete(serviceParametersId);
	}

}
