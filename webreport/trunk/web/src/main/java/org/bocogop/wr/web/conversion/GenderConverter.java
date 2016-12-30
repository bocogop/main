package org.bocogop.wr.web.conversion;

import org.bocogop.shared.model.lookup.sds.Gender;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenderConverter extends AbstractStringToPersistentConverter<Gender> {

	@Autowired
	public GenderConverter(GenderDAO dao) {
		super(dao);
	}
}
