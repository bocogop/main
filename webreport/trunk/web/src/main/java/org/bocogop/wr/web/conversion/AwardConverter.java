package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.persistence.dao.AwardDAO;

@Component
public class AwardConverter extends AbstractStringToPersistentConverter<Award> {

	@Autowired
	public AwardConverter(AwardDAO dao) {
		super(dao);
	}
}
