package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.persistence.dao.lookup.RequirementStatusDAO;

@Component
public class RequirementStatusConverter extends AbstractStringToPersistentConverter<RequirementStatus> {

	@Autowired
	protected RequirementStatusConverter(RequirementStatusDAO dao) {
		super(dao);
	}
}