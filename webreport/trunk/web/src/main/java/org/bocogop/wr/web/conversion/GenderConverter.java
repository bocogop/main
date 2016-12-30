package org.bocogop.wr.web.conversion;

import org.bocogop.wr.model.lookup.Gender;
import org.bocogop.wr.persistence.lookup.GenderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenderConverter extends AbstractStringToPersistentConverter<Gender> {

	@Autowired
	public GenderConverter(GenderDAO dao) {
		super(dao);
	}
}
