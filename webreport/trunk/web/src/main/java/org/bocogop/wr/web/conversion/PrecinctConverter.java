package org.bocogop.wr.web.conversion;

import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "precinctConverter")
public class PrecinctConverter extends AbstractStringToPersistentConverter<Precinct> {

	@Autowired
	protected PrecinctConverter(PrecinctDAO dao) {
		super(dao);
	}
}
