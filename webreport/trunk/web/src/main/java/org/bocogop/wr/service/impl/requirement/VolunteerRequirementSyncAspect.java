package org.bocogop.wr.service.impl.requirement;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.service.requirement.VolunteerRequirementService;

@Aspect
@Component
/*
 * Order(1) to place it ahead of the Transaction interceptor, so we commit the
 * main service method transactions before running the code below - CPB
 */
@Order(1)
public class VolunteerRequirementSyncAspect {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementSyncAspect.class);

	@Autowired
	private VolunteerRequirementService service;
	@Autowired
	private VolunteerAssignmentDAO volunteerAssignmentDAO;

	@Around("execution(* org.bocogop.wr.service.impl.VolunteerServiceImpl.saveOrUpdate(..))")
	public Volunteer runForVolunteerCreate(ProceedingJoinPoint jp) throws Throwable {
		Volunteer v = (Volunteer) jp.getArgs()[0];
		boolean isEdit = v.isPersistent();
		v = (Volunteer) jp.proceed();
		if (!isEdit && v.getId() != null) {
			processVolunteer(v.getId());
		}
		return v;
	}

	@AfterReturning("execution(* org.bocogop.wr.service.impl.VolunteerServiceImpl.addOrReactivateAssignment(..))")
	public void runForReactivateAssignment(JoinPoint joinPoint) {
		Long volunteerAssignmentId = (Long) joinPoint.getArgs()[0];
		Long volunteerId = (Long) joinPoint.getArgs()[1];
		if (volunteerId == null && volunteerAssignmentId != null) {
			VolunteerAssignment va = volunteerAssignmentDAO.findRequiredByPrimaryKey(volunteerAssignmentId);
			volunteerId = va.getVolunteer().getId();
		}
		if (volunteerId != null) {
			processVolunteer(volunteerId);
		} else {
			log.warn("Either volunteerId or volunteerAssignmentId parameter was expected but not present");
		}
	}

	private void processVolunteer(Long volunteerId) {
		Future<Integer> result = service.bulkAddNecessaryRequirementsLater(volunteerId, null, null, null);
		try {
			/*
			 * wait up to 3 seconds before we request a refresh of the UI, since
			 * in most cases this is fast enough to be modeled as a synchronous
			 * operation - CPB
			 */
			result.get(3, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// ignore
		}
	}

	@AfterReturning("execution(* org.bocogop.wr.service.impl.BenefitingServiceRoleServiceImpl.merge(..))")
	public void runForBenefitingServiceRoleMerge(JoinPoint joinPoint) {
		Long toBenefitingServiceRoleId = (Long) joinPoint.getArgs()[1];
		if (toBenefitingServiceRoleId != null) {
			service.bulkAddNecessaryRequirementsLater(null, null, toBenefitingServiceRoleId, null);
		} else {
			log.warn("toBenefitingServiceRoleId parameter was expected but not present");
		}
	}

	@AfterReturning("execution(* org.bocogop.wr.service.impl.BenefitingServiceRoleTemplateServiceImpl.merge(..))")
	public void runForBenefitingServiceRoleTemplateMerge(JoinPoint joinPoint) {
		Long toBenefitingServiceRoleTemplateId = (Long) joinPoint.getArgs()[1];
		if (toBenefitingServiceRoleTemplateId != null) {
			service.bulkAddNecessaryRequirementsLater(null, null, null, toBenefitingServiceRoleTemplateId);
		} else {
			log.warn("toBenefitingServiceRoleTemplateId parameter was expected but not present");
		}
	}

	@Around("execution(* org.bocogop.wr.service.impl.BenefitingServiceRoleTemplateServiceImpl.saveOrUpdate(..))")
	public BenefitingServiceRoleTemplate runForBenefitingServiceRoleTemplateUpdate(ProceedingJoinPoint jp)
			throws Throwable {
		BenefitingServiceRoleTemplate v = (BenefitingServiceRoleTemplate) jp.getArgs()[0];
		boolean isEdit = v.isPersistent();

		BenefitingServiceRoleType typeBefore = v.getRoleType();
		boolean nullBefore = typeBefore == null;

		v = (BenefitingServiceRoleTemplate) jp.proceed();

		BenefitingServiceRoleType typeAfter = v.getRoleType();
		boolean nullAfter = typeAfter == null;

		if (isEdit && (nullBefore != nullAfter || (typeAfter != null && !typeAfter.equals(typeBefore)))) {
			service.bulkAddNecessaryRequirementsLater(null, null, null, v.getId());
		}

		return v;
	}

	@Around("execution(* org.bocogop.wr.service.impl.BenefitingServiceRoleServiceImpl.saveOrUpdate(..))")
	public BenefitingServiceRole runForBenefitingServiceRoleUpdate(ProceedingJoinPoint jp) throws Throwable {
		BenefitingServiceRole v = (BenefitingServiceRole) jp.getArgs()[0];
		boolean isEdit = v.isPersistent();

		BenefitingServiceRoleType typeBefore = v.getRoleType();
		boolean nullBefore = typeBefore == null;

		v = (BenefitingServiceRole) jp.proceed();

		BenefitingServiceRoleType typeAfter = v.getRoleType();
		boolean nullAfter = typeAfter == null;

		if (isEdit && (nullBefore != nullAfter || (typeAfter != null && !typeAfter.equals(typeBefore)))) {
			service.bulkAddNecessaryRequirementsLater(null, null, v.getId(), null);
		}

		return v;
	}

	@AfterReturning(pointcut = "execution(* org.bocogop.wr.service.impl.RequirementServiceImpl.saveOrUpdate(..))", returning = "retVal")
	public void runForRequirementSaveOrUpdate(AbstractRequirement retVal) {
		service.bulkAddNecessaryRequirementsLater(null, retVal.getId(), null, null);
	}

	@AfterReturning("execution(* org.bocogop.wr.service.impl.RequirementServiceImpl.reactivateRequirement(..))")
	public void runForRequirementReactivate(JoinPoint joinPoint) {
		Long requirementId = (Long) joinPoint.getArgs()[0];
		service.bulkAddNecessaryRequirementsLater(null, requirementId, null, null);
	}
	
	@AfterReturning("execution(* org.bocogop.wr.service.impl.RequirementServiceImpl.changeType(..))")
	public void runForRequirementChangeType(JoinPoint joinPoint) {
		Long requirementId = (Long) joinPoint.getArgs()[0];
		service.bulkAddNecessaryRequirementsLater(null, requirementId, null, null);
	}

}
