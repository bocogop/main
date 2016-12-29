package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.volunteer.TransportationMethod;
import org.bocogop.wr.persistence.dao.lookup.TransportationMethodDAO;

@Component
public class MethodOfTransportationConverter extends AbstractStringToPersistentConverter<TransportationMethod> {

	@Autowired
	public MethodOfTransportationConverter(TransportationMethodDAO dao) {
		super(dao);
	}
}
