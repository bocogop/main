package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.voluntaryService.VoluntaryServiceType;
import org.bocogop.wr.persistence.dao.lookup.VoluntaryServiceTypeDAO;

@Component
public class VoluntaryServiceTypeConverter extends AbstractStringToPersistentConverter<VoluntaryServiceType> {

	@Autowired
	protected VoluntaryServiceTypeConverter(VoluntaryServiceTypeDAO dao) {
		super(dao);
	}
}
