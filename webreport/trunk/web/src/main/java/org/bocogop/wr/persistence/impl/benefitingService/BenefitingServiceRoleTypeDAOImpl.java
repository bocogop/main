package org.bocogop.wr.persistence.impl.benefitingService;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class BenefitingServiceRoleTypeDAOImpl extends GenericHibernateLookupDAOImpl<BenefitingServiceRoleType>
		implements BenefitingServiceRoleTypeDAO {

}
