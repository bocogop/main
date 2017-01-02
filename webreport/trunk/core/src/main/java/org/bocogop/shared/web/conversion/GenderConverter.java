package org.bocogop.shared.web.conversion;

import org.bocogop.shared.model.lookup.Gender;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenderConverter extends AbstractStringToPersistentConverter<Gender> {

	@Autowired
	public GenderConverter(GenderDAO dao) {
		super(dao);
	}
}
