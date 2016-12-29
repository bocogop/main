package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;

@Component(value = "facilityConverter")
public class FacilityConverter extends AbstractStringToPersistentConverter<Facility> {

	@Autowired
	protected FacilityConverter(FacilityDAO dao) {
		super(dao);
	}
}
