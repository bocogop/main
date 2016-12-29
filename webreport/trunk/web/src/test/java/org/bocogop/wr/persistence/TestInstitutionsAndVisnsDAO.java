package org.bocogop.wr.persistence;

import org.junit.Ignore;
import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebTest;

public class TestInstitutionsAndVisnsDAO extends AbstractTransactionalWebTest {

	@Test
	@Ignore // until we use FacilityAndVisn or another view - CPB
	public void testCustomFindAllSorted() {
		facilityAndVISNDAO.getForFacilityId(1000098);
	}

}
