package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;

@Component(value = "vaFacilityConverter")
public class VAFacilityConverter extends AbstractStringToPersistentConverter<VAFacility> {

	@Autowired
	protected VAFacilityConverter(VAFacilityDAO dao) {
		super(dao);
	}
}
