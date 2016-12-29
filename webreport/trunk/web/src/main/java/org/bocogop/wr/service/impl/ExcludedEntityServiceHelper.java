package org.bocogop.wr.service.impl;

import static org.bocogop.wr.model.ApplicationParameter.ApplicationParameterType.LEIE_JOB_LAST_EXECUTED_DATE;
import static org.bocogop.wr.model.ApplicationParameter.ApplicationParameterType.LEIE_SOURCE_DATA_CHANGED_DATE;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.wr.model.ApplicationParameter;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;

@Component
public class ExcludedEntityServiceHelper {

	@Autowired
	private ExcludedEntityDAO excludedEntityDAO;
	@Autowired
	private ApplicationParametersDAO applicationParametersDAO;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addValues(List<ExcludedEntity> eList) {
		for (ExcludedEntity e : eList)
			e = excludedEntityDAO.saveOrUpdate(e);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteValues(List<ExcludedEntity> eList) {
		for (ExcludedEntity e : eList)
			excludedEntityDAO.delete(e.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateExecutedDate() {
		ApplicationParameter param = applicationParametersDAO.findByName(LEIE_JOB_LAST_EXECUTED_DATE.getParamName());
		param.setParameterValue(ZonedDateTime.now().toString());
		applicationParametersDAO.saveOrUpdate(param);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateDataChangedDate() {
		ApplicationParameter param = applicationParametersDAO.findByName(LEIE_SOURCE_DATA_CHANGED_DATE.getParamName());
		param.setParameterValue(ZonedDateTime.now().toString());
		applicationParametersDAO.saveOrUpdate(param);
	}

}
