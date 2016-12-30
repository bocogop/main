package org.bocogop.wr.test.util;

import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestObjectFactory {

	@Autowired
	protected GenderDAO genderDAO;

}
