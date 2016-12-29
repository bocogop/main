package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.volunteer.ShirtSize;
import org.bocogop.wr.persistence.dao.ShirtSizeDAO;

@Component
public class ShirtSizeConverter extends AbstractStringToPersistentConverter<ShirtSize> {

	@Autowired
	public ShirtSizeConverter(ShirtSizeDAO dao) {
		super(dao);
	}
}
