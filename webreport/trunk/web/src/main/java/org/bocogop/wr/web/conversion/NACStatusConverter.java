package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.organization.NACStatus;
import org.bocogop.wr.persistence.dao.lookup.NACStatusDAO;


@Component(value = "nacStatusConverter")
public class NACStatusConverter extends AbstractStringToPersistentConverter<NACStatus> {

	@Autowired
	protected NACStatusConverter(NACStatusDAO dao) {
		super(dao);
	}
}
