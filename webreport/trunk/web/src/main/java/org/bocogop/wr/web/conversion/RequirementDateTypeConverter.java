package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.requirement.RequirementDateType;
import org.bocogop.wr.persistence.dao.lookup.RequirementDateTypeDAO;


@Component
public class RequirementDateTypeConverter extends AbstractStringToPersistentConverter<RequirementDateType> {

	@Autowired
	protected RequirementDateTypeConverter(RequirementDateTypeDAO dao) {
		super(dao);
	}
}