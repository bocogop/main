package org.bocogop.wr.persistence.dao.lookup;

import org.bocogop.wr.model.organization.NACStatus;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface NACStatusDAO extends CustomizableAppDAO<NACStatus> {

	NACStatus findByMembershipStatus(String membershipStatus);

}
