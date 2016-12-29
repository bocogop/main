package org.bocogop.wr.persistence.impl.requirement;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.requirement.BenefitingServiceRoleRequirementAssociation;
import org.bocogop.wr.persistence.dao.requirement.BenefitingServiceRoleRequirementAssociationDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;

@Repository
public class BenefitingServiceRoleRequirementAssociationDAOImpl
		extends GenericHibernateDAOImpl<BenefitingServiceRoleRequirementAssociation>
		implements BenefitingServiceRoleRequirementAssociationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceRoleRequirementAssociationDAOImpl.class);

	@Override
	public int bulkDeleteByCriteria(Long requirementId, Long benefitingServiceRoleId, Long benefitingServiceId,
			Long benefitingServiceRoleTemplateId, Long benefitingServiceTemplateId) {
		if (requirementId == null && benefitingServiceRoleId == null && benefitingServiceId == null
				&& benefitingServiceRoleTemplateId == null && benefitingServiceTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + BenefitingServiceRoleRequirementAssociation.class.getName()
				+ " where id in (select vfa.id from " + BenefitingServiceRoleRequirementAssociation.class.getName()
				+ " vfa" //
				+ " where (1=2" //
				+ (requirementId != null ? " or vfa.requirement.id = :requirementId" : "") //
				+ (benefitingServiceRoleId != null ? " or vfa.benefitingServiceRole.id = :benefitingServiceRoleId" : "") //
				+ (benefitingServiceId != null
						? " or vfa.benefitingServiceRole.benefitingService.id = :benefitingServiceId" : "") //
				+ (benefitingServiceRoleTemplateId != null ? " or vfa.benefitingServiceRole.benefitingServiceTemplate.id = :benefitingServiceRoleTemplateId" : "") //
				+ (benefitingServiceTemplateId != null
						? " or vfa.benefitingServiceRole.benefitingService.template.id = :benefitingServiceTemplateId" : "") //
				+ "))");
		if (requirementId != null)
			q.setParameter("requirementId", requirementId);
		if (benefitingServiceRoleId != null)
			q.setParameter("benefitingServiceRoleId", benefitingServiceRoleId);
		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		return q.executeUpdate();
	}

}
