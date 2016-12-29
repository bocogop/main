package org.bocogop.wr.persistence.impl.requirement;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.requirement.BenefitingServiceRoleTemplateRequirementAssociation;
import org.bocogop.wr.persistence.dao.requirement.BenefitingServiceRoleTemplateRequirementAssociationDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;

@Repository
public class BenefitingServiceRoleTemplateRequirementAssociationDAOImpl
		extends GenericHibernateDAOImpl<BenefitingServiceRoleTemplateRequirementAssociation>
		implements BenefitingServiceRoleTemplateRequirementAssociationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(BenefitingServiceRoleTemplateRequirementAssociationDAOImpl.class);

	@Override
	public int bulkDeleteByCriteria(Long requirementId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceTemplateId) {
		if (benefitingServiceRoleTemplateId == null && requirementId == null && benefitingServiceTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + BenefitingServiceRoleTemplateRequirementAssociation.class.getName()
				+ " where id in (select vfa.id from "
				+ BenefitingServiceRoleTemplateRequirementAssociation.class.getName() + " vfa" //
				+ " where (1=2" //
				+ (requirementId != null ? " or vfa.requirement.id = :requirementId" : "") //
				+ (benefitingServiceRoleTemplateId != null
						? " or vfa.benefitingServiceRoleTemplate.id = :benefitingServiceRoleTemplateId" : "") //
				+ (benefitingServiceTemplateId != null
						? " or vfa.benefitingServiceRoleTemplate.benefitingServiceTemplate.id = :benefitingServiceTemplateId"
						: "") //
				+ "))");
		if (requirementId != null)
			q.setParameter("requirementId", requirementId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		return q.executeUpdate();
	}

}
