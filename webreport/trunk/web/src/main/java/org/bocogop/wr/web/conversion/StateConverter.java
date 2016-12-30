package org.bocogop.wr.web.conversion;

import org.bocogop.wr.model.lookup.State;
import org.bocogop.wr.persistence.lookup.StateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateConverter extends AbstractStringToPersistentConverter<State> {

	@Autowired
	public StateConverter(StateDAO dao) {
		super(dao);
	}
}
