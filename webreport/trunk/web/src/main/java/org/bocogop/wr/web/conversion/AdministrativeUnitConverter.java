package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.persistence.dao.lookup.AdministrativeUnitDAO;

@Component
public class AdministrativeUnitConverter extends AbstractStringToPersistentConverter<AdministrativeUnit> {

	@Autowired
	public AdministrativeUnitConverter(AdministrativeUnitDAO dao) {
		super(dao);
	}
}
