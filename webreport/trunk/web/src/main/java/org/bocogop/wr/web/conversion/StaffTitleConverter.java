package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.persistence.dao.lookup.StaffTitleDAO;

@Component
public class StaffTitleConverter extends AbstractStringToPersistentConverter<StaffTitle> {

	@Autowired
	protected StaffTitleConverter(StaffTitleDAO dao) {
		super(dao);
	}
}
