package org.bocogop.wr.web;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.model.lookup.sds.VAFacility.VAFacilityValue;
import org.bocogop.wr.AbstractTransactionalWebTest;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.WebConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;

// FIXWR
@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class, WebConfig.class })
@WebAppConfiguration
public abstract class AbstractTransactionalControllerTest extends AbstractTransactionalWebTest {

	@Before
	protected void setupSessionContextSite() {
		VAFacility vaFacility = new VAFacility();
		vaFacility.setId(VAFacilityValue.CHEYENNE.getId());
		vaFacility.setName(VAFacilityValue.CHEYENNE.getName());
		// FIXWR push this into session I believe
	}

}
