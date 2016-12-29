package org.bocogop.wr.service.impl;

import static org.bocogop.wr.model.requirement.RequirementApplicationType.ALL_VOLUNTEERS;
import static org.bocogop.wr.model.requirement.RequirementApplicationType.ROLE_TYPE;
import static org.bocogop.wr.model.requirement.RequirementApplicationType.SPECIFIC_ROLES;
import static org.bocogop.wr.model.requirement.RequirementDateType.RequirementDateTypeValue.NOT_APPLICABLE;
import static org.bocogop.wr.model.requirement.RequirementScopeType.FACILITY;
import static org.bocogop.wr.model.requirement.RequirementScopeType.GLOBAL;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.AbstractVolunteerRequirement;
import org.bocogop.wr.model.requirement.AbstractVolunteerRequirement.CompareByRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleTypeRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleTypeRequirement;
import org.bocogop.wr.model.requirement.RequirementApplicationType;
import org.bocogop.wr.model.requirement.RequirementAvailableStatus;
import org.bocogop.wr.model.requirement.RequirementDateType;
import org.bocogop.wr.model.requirement.RequirementScopeType;
import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.service.RequirementService;

@Service
public class RequirementServiceImpl extends AbstractServiceImpl implements RequirementService {
	private static final Logger log = LoggerFactory.getLogger(RequirementServiceImpl.class);

	public static Map<String, Object> getRequirementsByScope(
			List<? extends AbstractVolunteerRequirement> requirements, List<VolunteerAssignment> assignments) {
		Map<String, Object> requirementsByScopeMap = new HashMap<>();

		Comparator<AbstractVolunteerRequirement> reqComparator = new CompareByRequirement();

		SortedSet<AbstractVolunteerRequirement> globalAll = new TreeSet<>(reqComparator);
		requirementsByScopeMap.put("globalAll", globalAll);

		SortedSet<AbstractVolunteerRequirement> facilityAll = new TreeSet<>(reqComparator);
		requirementsByScopeMap.put("facilityAll", facilityAll);

		Map<Long, SortedSet<AbstractVolunteerRequirement>> reqsByVolAssignmentIdMap = new HashMap<>();
		requirementsByScopeMap.put("byAssignment", reqsByVolAssignmentIdMap);

		for (AbstractVolunteerRequirement volReq : requirements) {
			AbstractRequirement req = PersistenceUtil.initializeAndUnproxy(volReq.getRequirement());
			RequirementApplicationType applicationType = req.getApplicationType();
			RequirementScopeType scopeType = req.getScope();

			if (applicationType == ALL_VOLUNTEERS) {
				if (scopeType == GLOBAL) {
					globalAll.add(volReq);
				} else if (scopeType == FACILITY) {
					facilityAll.add(volReq);
				}
			} else {
				for (VolunteerAssignment va : assignments) {
					long assignmentRootFacilityId = va.getFacility() == null ? -1
							: va.getFacility().getRootFacilityId();

					BenefitingServiceRole bsr = va.getBenefitingServiceRole();
					BenefitingServiceRoleTemplate bsrt = bsr.getTemplate();

					boolean add = false;
					if (applicationType == RequirementApplicationType.ROLE_TYPE) {
						if (scopeType == GLOBAL) {
							GlobalRoleTypeRequirement grtr = (GlobalRoleTypeRequirement) req;
							BenefitingServiceRoleType reqRoleType = grtr.getRoleType();
							add = (bsr.getRoleType().equals(reqRoleType)
									|| (bsr.isNational() && bsr.getTemplate().getRoleType().equals(reqRoleType)));
						} else if (scopeType == FACILITY) {
							FacilityRoleTypeRequirement frtr = (FacilityRoleTypeRequirement) req;
							long requirementFacilityId = frtr.getFacility() == null ? -2 : frtr.getFacility().getId();
							if (requirementFacilityId != assignmentRootFacilityId)
								continue;

							BenefitingServiceRoleType reqRoleType = frtr.getRoleType();
							add = bsr.getRoleType().equals(reqRoleType)
									|| (bsr.isNational() && bsr.getTemplate().getRoleType().equals(reqRoleType));
						}
					} else if (applicationType == SPECIFIC_ROLES) {
						if (scopeType == GLOBAL) {
							GlobalRoleRequirement grr = (GlobalRoleRequirement) req;
							add = bsrt != null && grr.getBenefitingServiceRoleTemplates().contains(bsrt);
						} else if (scopeType == FACILITY) {
							FacilityRoleRequirement frr = (FacilityRoleRequirement) req;
							long requirementFacilityId = frr.getFacility() == null ? -2 : frr.getFacility().getId();
							if (requirementFacilityId != assignmentRootFacilityId)
								continue;

							add = frr.getBenefitingServiceRoles().contains(bsr);
						}
					}

					if (add)
						reqsByVolAssignmentIdMap.computeIfAbsent(va.getId(), k -> new TreeSet<>(reqComparator))
								.add(volReq);
				}
			}
		}

		return requirementsByScopeMap;
	}

	@Override
	public AbstractRequirement saveOrUpdate(final AbstractRequirement req) throws ServiceValidationException {
		requirementDAO.detach(req);

		boolean isNew = !req.isPersistent();
		Long requirementId = req.getId();

		if (isNew) {
			RequirementStatus metStatus = requirementStatusDAO.findByLookup(RequirementStatusValue.MET);
			req.getAvailableStatuses().add(new RequirementAvailableStatus(req, metStatus));
		}

		boolean isDateTypeChangedToNotApplicable = false;
		boolean statusWasRemoved = false;
		RequirementDateType newDateType = req.getDateType();
		if (!isNew) {
			AbstractRequirement unmodifiedRequirement = requirementDAO.findRequiredByPrimaryKey(requirementId);

			RequirementDateType oldDateType = unmodifiedRequirement.getDateType();
			isDateTypeChangedToNotApplicable = (oldDateType == null || oldDateType.getId() != NOT_APPLICABLE.getId())
					&& (newDateType != null && newDateType.getId() == NOT_APPLICABLE.getId());

			statusWasRemoved = !req.getAvailableStatuses().containsAll(unmodifiedRequirement.getAvailableStatuses());
		}

		if (newDateType.getId() == NOT_APPLICABLE.getId()) {
			req.setDaysNotification(null);
		}

		// ---- duplicate check

		// first get any others that match the name and scope
		Stream<AbstractRequirement> dupStream = requirementDAO
				.findByCriteria(req.getFacilityScope() == null ? null : req.getFacilityScope().getId(), req.getName())
				.stream();
		if (!isNew) {
			// if we're editing one, filter ourself out of the matches
			dupStream = dupStream.filter(p -> !p.getId().equals(requirementId));
		}
		// in order to be a duplicate, the type must match
		dupStream = dupStream.filter(p -> p.getApplicationType() == req.getApplicationType());
		// and the role type, or some specific role, must match depending on the
		// type
		if (req.getApplicationType() == ROLE_TYPE && req.getScope() == RequirementScopeType.GLOBAL) {
			GlobalRoleTypeRequirement grtr = (GlobalRoleTypeRequirement) req;
			dupStream = dupStream.filter(p -> p instanceof GlobalRoleTypeRequirement
					&& ((GlobalRoleTypeRequirement) p).getRoleType().equals(grtr.getRoleType()));
		} else if (req.getApplicationType() == SPECIFIC_ROLES && req.getScope() == RequirementScopeType.GLOBAL) {
			GlobalRoleRequirement grr = (GlobalRoleRequirement) req;
			dupStream = dupStream.filter(p -> p instanceof GlobalRoleRequirement
					&& ((GlobalRoleRequirement) p).getBenefitingServiceRoleTemplates().stream()
							.anyMatch(grr.getBenefitingServiceRoleTemplates()::contains));
		} else if (req.getApplicationType() == ROLE_TYPE && req.getScope() == RequirementScopeType.FACILITY) {
			FacilityRoleTypeRequirement frtr = (FacilityRoleTypeRequirement) req;
			dupStream = dupStream.filter(p -> p instanceof FacilityRoleTypeRequirement
					&& ((FacilityRoleTypeRequirement) p).getRoleType().equals(frtr.getRoleType()));
		} else if (req.getApplicationType() == SPECIFIC_ROLES && req.getScope() == RequirementScopeType.FACILITY) {
			FacilityRoleRequirement frr = (FacilityRoleRequirement) req;
			dupStream = dupStream.filter(p -> p instanceof FacilityRoleRequirement && ((FacilityRoleRequirement) p)
					.getBenefitingServiceRoles().stream().anyMatch(frr.getBenefitingServiceRoles()::contains));
		}

		Optional<AbstractRequirement> findAny = dupStream.findAny();
		if (findAny.isPresent()) {
			AbstractRequirement offender = findAny.get();
			throw new ServiceValidationException("requirement.error.duplicateNameFacility",
					new Serializable[] { offender.getName() });
		}

		AbstractRequirement updatedReq = requirementDAO.saveOrUpdate(req);

		if (!isNew) {
			// bulk update if date type is changed to Not Applicable
			if (isDateTypeChangedToNotApplicable) {
				// When changing a date type on an existing requirement from any
				// other value to ”Not Applicable”
				// all VolunteerRequirement dates associated to this requirement
				// are deleted.
				volunteerRequirementDAO.bulkUpdateDateToNull(updatedReq.getId());
			}

			if (statusWasRemoved) {
				volunteerRequirementDAO.bulkUpdateInvalidStatusesToNew(updatedReq.getId());
			}
		}

		return updatedReq;
	}

	@Override
	public void delete(long requirementId) {
		volunteerRequirementDAO.deleteByCriteria(requirementId);
		benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(requirementId, null, null, null, null);
		benefitingServiceRoleTemplateRequirementAssociationDAO.bulkDeleteByCriteria(requirementId, null, null);
		requirementDAO.delete(requirementId);
	}

	@Override
	public void changeType(long requirementId, RequirementApplicationType requirementChangeNewType,
			BenefitingServiceRoleType requirementChangeNewRoleType) {
		AbstractRequirement r = requirementDAO.findRequiredByPrimaryKey(requirementId);
		String newTypeCode = AbstractRequirement.getTypeCode(r.getScope(), requirementChangeNewType);
		requirementDAO.changeType(requirementId, newTypeCode);

		requirementDAO.updateFieldsWithoutVersionIncrement(requirementId, true,
				requirementChangeNewType != ROLE_TYPE ? null : requirementChangeNewRoleType.getId());

		if (requirementChangeNewType != SPECIFIC_ROLES) {
			benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(requirementId, null, null, null, null);
		}
	}

	@Override
	public void inactivateRequirement(long id) {
		AbstractRequirement requirement = requirementDAO.findRequiredByPrimaryKey(id);
		requirement.setInactive(true);
		requirement = requirementDAO.saveOrUpdate(requirement);
	}

	@Override
	public void reactivateRequirement(long id) {
		AbstractRequirement requirement = requirementDAO.findRequiredByPrimaryKey(id);
		requirement.setInactive(false);
		requirement = requirementDAO.saveOrUpdate(requirement);
	}

}
