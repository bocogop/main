package org.bocogop.wr.test.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.shared.persistence.lookup.sds.GenderDAO;

@Component
public class TestObjectFactory {

	@Autowired
	protected GenderDAO genderDAO;

}
