package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.organization.StdVAVSTitle;
import org.bocogop.wr.persistence.dao.lookup.StdVAVSTitleDAO;


@Component
public class StdVAVSTitleConverter extends AbstractStringToPersistentConverter<StdVAVSTitle> {

	@Autowired
	protected StdVAVSTitleConverter(StdVAVSTitleDAO dao) {
		super(dao);
	}
}
