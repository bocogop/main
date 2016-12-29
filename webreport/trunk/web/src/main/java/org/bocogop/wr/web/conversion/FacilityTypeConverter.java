package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.facility.FacilityType;
import org.bocogop.wr.persistence.dao.lookup.FacilityTypeDAO;

@Component
public class FacilityTypeConverter extends AbstractStringToPersistentConverter<FacilityType> {

	@Autowired
	public FacilityTypeConverter(FacilityTypeDAO dao) {
		super(dao);
	}
}
