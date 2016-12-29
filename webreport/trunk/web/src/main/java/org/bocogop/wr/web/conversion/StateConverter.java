package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;

@Component
public class StateConverter extends AbstractStringToPersistentConverter<State> {

	@Autowired
	public StateConverter(StateDAO dao) {
		super(dao);
	}
}
