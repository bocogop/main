package org.bocogop.wr.persistence.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonationType;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.persistence.dao.DonorDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.lookup.DonationTypeDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonorDAOImpl extends GenericHibernateSortedDAOImpl<Donor> implements DonorDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonorDAOImpl.class);

	@Value("${donorSearch.maxResults}")
	private int maxResults;

	@Autowired
	private FacilityDAO facilityDAO;

	@Autowired
	private DonationTypeDAO donationTypeDAO;

	@Override
	public List<DonorSearchResult> findByCriteria(DonorType donorType, String firstName, String middleName,
			String lastName, String orgName, String city, State state, String zip, String email, String phone,
			Long facilityId, QueryCustomization... customization) {

		List<DonorSearchResult> resultList = new ArrayList<DonorSearchResult>();

		if (donorType == null) {
			throw new IllegalArgumentException("donorType cannot be null");
		}

		DonorSearchResult donorSearchResult = null;

		Map<Long, DonorSearchResult> donorSearchResultMap = new LinkedHashMap<Long, DonorSearchResult>();

		if (donorType.getLookupType() == DonorTypeValue.INDIVIDUAL) {
			List<Donor> mergedDonors = new ArrayList<>();

			// search unlinked individual donors
			List<Donor> donorResultList = findUnlinkedIndividualDonorsByCriteria(firstName, middleName, lastName, city,
					state, zip, email, phone, facilityId, customization);
			for (Donor donor : donorResultList) {
				mergedDonors.add(donor);
			}

			// search donor linked to volunteer
			List<Donor> volunteerResultList = findLinkedVolunteerDataByCriteria(donorType, firstName, middleName,
					lastName, city, state, zip, email, phone, facilityId, customization);
			for (Donor volunteerDonor : volunteerResultList) {
				mergedDonors.add(volunteerDonor);
			}

			Collections.sort(mergedDonors);
			if (mergedDonors.size() > maxResults)
				mergedDonors = mergedDonors.subList(0, maxResults);

			for (Donor r : mergedDonors) {
				donorSearchResultMap.put(r.getId(), new DonorSearchResult(r));
			}
		} else if (donorType.getLookupType() == DonorTypeValue.ORGANIZATION) {
			if (facilityId == null)
				throw new IllegalArgumentException("FacilityID is required if the donor type is 'Organization'");

			// search donor linked to organization
			List<Donor> organizationResultList = findLinkedOrganizationDataByCriteria(donorType, orgName, facilityId,
					customization);

			if (!organizationResultList.isEmpty()) {
				for (Donor organizationDonor : organizationResultList) {
					donorSearchResult = new DonorSearchResult(organizationDonor);
					donorSearchResult.setOrgFacility(organizationDonor.getOrganization().getFacility());
					donorSearchResult.setOrgContactName(organizationDonor.getOrganization().getContactName());
					donorSearchResultMap.put(organizationDonor.getId(), donorSearchResult);
				}
			}
		}

		if (!donorSearchResultMap.isEmpty()) {
			List<DonorSearchIdResult> donorIdResults = findDonorIds(donorSearchResultMap.keySet());

			for (DonorSearchIdResult r : donorIdResults) {
				DonorSearchResult donorSearchResultInfo = donorSearchResultMap.get(r.donorId);
				if (donorSearchResultInfo != null) {
					donorSearchResultInfo.setFacility(facilityDAO.findByPrimaryKey(r.facilityId));
					donorSearchResultInfo.setDonationType(donationTypeDAO.findByPrimaryKey(r.donationTypeId));
					donorSearchResultInfo.setDonationDate(r.donationDate);
					donorSearchResultInfo.setDonationValue(r.donationValue);
				}
			}

			resultList = new ArrayList<>(donorSearchResultMap.values());
		}

		return resultList;
	}

	private List<DonorSearchIdResult> findDonorIds(Collection<Long> donorIds) {

		String sb = "with max_ids as (" //
				+ "		select id," //
				+ "			DonorFK," //
				+ "			DonationTypeFK," //
				+ "			FacilityFK," //
				+ "			DonationDate," //
				+ "			row_number() over (partition by DonorFK order by DonationDate desc, id desc) as row_num" //
				+ "		from wr.DonationSummary" //
				+ "		where DonorFK in (:donorIds)" //
				+ "	)" //
				+ "	select" //
				+ "		d.DonorFK," //
				+ "		d.DonationTypeFK," //
				+ "		d.DonationDate," //
				+ "		d.FacilityFK," //
				+ "		ISNULL(sum(i.DonationValue), 0)" //
				+ "	from max_ids d" //
				+ "		left join wr.DonationDetail i on d.id = i.DonationSummaryFK" //
				+ "	where d.row_num = 1" //
				+ "	group by" //
				+ "		d.DonorFK," //
				+ "		d.DonationTypeFK," //
				+ "		d.DonationDate," //
				+ "		d.FacilityFK";

		@SuppressWarnings("unchecked")
		List<Object[]> donorIdList = (List<Object[]>) em.createNativeQuery(sb).setParameter("donorIds", donorIds)
				.getResultList();
		List<DonorSearchIdResult> results = new ArrayList<>();
		for (Object[] rr : donorIdList) {
			Long donorId = ((Number) rr[0]).longValue();
			Long donationTypeId = ((Number) rr[1]).longValue();
			LocalDate donationDate = ((Timestamp) rr[2]).toLocalDateTime().toLocalDate();
			Long facilityId = ((Number) rr[3]).longValue();
			BigDecimal n = (BigDecimal) rr[4];
			results.add(new DonorSearchIdResult(donorId, donationTypeId, donationDate, facilityId, n));
		}

		return results;
	}

	private List<Donor> findUnlinkedIndividualDonorsByCriteria(String firstName, String middleName, String lastName,
			String city, State state, String zip, String email, String phone, Long facilityId,
			QueryCustomization... customization) {
		// Don't bother with this yet - CPB
		QueryCustomization cust = new QueryCustomization();

		StringBuilder sb = new StringBuilder("select d from ").append(Donor.class.getName()).append(" d");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		// if facility is part of query param:
		if (facilityId != null) {
			whereClauseItems.add("exists (select ds1 from " + DonationSummary.class.getName()
					+ " ds1 where ds1.donor.id = d.id and ds1.facility.id = :facilityId)");
			params.put("facilityId", facilityId);
		}

		whereClauseItems.add("d.donorType.id = :donorTypeId");
		params.put("donorTypeId", DonorTypeValue.INDIVIDUAL.getId());

		whereClauseItems.add("d.volunteer is null");

		if (StringUtils.isNotBlank(firstName)) {
			whereClauseItems.add("LOWER(d.firstName) like :firstName");
			params.put("firstName", "%" + firstName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(middleName)) {
			whereClauseItems.add("LOWER(d.middleName) like :middleName");
			params.put("middleName", "%" + middleName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(lastName)) {
			whereClauseItems.add("LOWER(d.lastName) like :lastName");
			params.put("lastName", "%" + lastName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			whereClauseItems.add("LOWER(d.email) like :email");
			params.put("email", "%" + email.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(city)) {
			whereClauseItems.add("LOWER(d.city) like :city");
			params.put("city", "%" + city.toLowerCase() + "%");
		}

		if (state != null) {
			whereClauseItems.add("d.state.id = :stateId");
			params.put("stateId", state.getId());
		}

		if (StringUtils.isNotBlank(zip)) {
			whereClauseItems.add("LOWER(d.zip) like :zip");
			params.put("zip", "%" + zip.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(phone)) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("CONCAT(SUBSTRING(d.phone,1,3), ").append("SUBSTRING(d.phone, 5,3), ")
					.append("SUBSTRING(d.phone, 9,4)) like :phone");
			whereClauseItems.add(sb2.toString());
			params.put("phone", "%" + phone.replaceAll("\\D", "") + "%");
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("d.lastName, d.firstName");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		// fill in the donation value previously retrieved
		@SuppressWarnings("unchecked")
		List<Donor> donorResultList = q.setMaxResults(maxResults).getResultList();

		return donorResultList;
	}

	static class DonorSearchIdResult {
		Long donorId;
		Long donationTypeId;
		LocalDate donationDate;
		Long facilityId;
		BigDecimal donationValue;

		public DonorSearchIdResult(Long donorId, Long donationTypeId, LocalDate donationDate, Long facilityId,
				BigDecimal donationValue) {
			this.donorId = donorId;
			this.donationTypeId = donationTypeId;
			this.donationDate = donationDate;
			this.facilityId = facilityId;
			this.donationValue = donationValue;
		}
	}

	public static class DonorSearchResult implements Comparable<DonorSearchResult> {
		Donor donor;
		DonationType donationType;
		LocalDate donationDate;
		Facility facility;
		BigDecimal donationValue;
		Facility orgFacility;
		String orgContactName;

		public DonorSearchResult(Donor donor) {
			this.donor = donor;
		}

		@Override
		public int compareTo(DonorSearchResult o) {
			if (equals(o))
				return 0;

			return new CompareToBuilder().append(getDonor(), (o.getDonor())).toComparison() > 0 ? 1 : -1;
		}

		public Donor getDonor() {
			return donor;
		}

		public void setDonor(Donor donor) {
			this.donor = donor;
		}

		public DonationType getDonationType() {
			return donationType;
		}

		public void setDonationType(DonationType donationType) {
			this.donationType = donationType;
		}

		public LocalDate getDonationDate() {
			return donationDate;
		}

		public void setDonationDate(LocalDate donationDate) {
			this.donationDate = donationDate;
		}

		public Facility getFacility() {
			return facility;
		}

		public void setFacility(Facility facility) {
			this.facility = facility;
		}

		public BigDecimal getDonationValue() {
			return donationValue;
		}

		public void setDonationValue(BigDecimal donationValue) {
			this.donationValue = donationValue;
		}

		public Facility getOrgFacility() {
			return orgFacility;
		}

		public void setOrgFacility(Facility orgFacility) {
			this.orgFacility = orgFacility;
		}

		public String getOrgContactName() {
			return orgContactName;
		}

		public void setOrgContactName(String orgContactName) {
			this.orgContactName = orgContactName;
		}
	}

	// ======================= Donor linked to volunteer
	// =============================
	private List<Donor> findLinkedVolunteerDataByCriteria(DonorType donorType, String firstName, String middleName,
			String lastName, String city, State state, String zip, String email, String phone, Long facilityId,
			QueryCustomization... customization) {

		// Don't bother with this yet - CPB
		QueryCustomization cust = new QueryCustomization();

		StringBuilder sb = new StringBuilder("select d from ").append(Donor.class.getName())
				.append(" d join fetch d.volunteer v");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		// if facility is part of query param:
		if (facilityId != null) {
			whereClauseItems.add("exists (select ds1 from " + DonationSummary.class.getName()
					+ " ds1 where ds1.donor.id = d.id and ds1.facility.id = :facilityId)");
			params.put("facilityId", facilityId);
		}

		whereClauseItems.add("d.donorType.id = :donorTypeId");
		params.put("donorTypeId", donorType.getId());

		if (StringUtils.isNotBlank(firstName)) {
			whereClauseItems.add("LOWER(v.firstName) like :firstName");
			params.put("firstName", "%" + firstName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(middleName)) {
			whereClauseItems.add("LOWER(v.middleName) like :middleName");
			params.put("middleName", "%" + middleName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(lastName)) {
			whereClauseItems.add("LOWER(v.lastName) like :lastName");
			params.put("lastName", "%" + lastName.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			whereClauseItems.add("LOWER(v.email) like :email");
			params.put("email", "%" + email.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(city)) {
			whereClauseItems.add("LOWER(v.city) like :city");
			params.put("city", "%" + city.toLowerCase() + "%");
		}

		if (state != null) {
			whereClauseItems.add("v.state.id = :stateId");
			params.put("stateId", state.getId());
		}

		if (StringUtils.isNotBlank(zip)) {
			whereClauseItems.add("LOWER(v.zip) like :zip");
			params.put("zip", "%" + zip.toLowerCase() + "%");
		}

		if (StringUtils.isNotBlank(phone)) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("CONCAT(SUBSTRING(d.phone,1,3), ").append("SUBSTRING(d.phone, 5,3), ")
					.append("SUBSTRING(d.phone, 9,4)) like :phone");
			whereClauseItems.add(sb2.toString());
			params.put("phone", "%" + phone.replaceAll("\\D", "") + "%");
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("v.lastName, v.firstName");

		// -----------------

		Query q = constructQuery(em, sb, whereClauseItems, params, null, customization);

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		@SuppressWarnings("unchecked")
		List<Donor> volunteerResultObjList = q.setMaxResults(maxResults).getResultList();
		return volunteerResultObjList;
	}

	// ======================= Donor linked to organization
	// =============================
	private List<Donor> findLinkedOrganizationDataByCriteria(DonorType donorType, String name, long facilityId,
			QueryCustomization... customization) {

		// Don't bother with this yet - CPB
		QueryCustomization cust = new QueryCustomization();

		StringBuilder sb = new StringBuilder("select d from ").append(Donor.class.getName())
				.append(" d join fetch d.organization o ").append(" left join fetch o.organization parent")
				.append(" left join o.facility facility");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		// if facility is part of query param:
		whereClauseItems.add("o.scope = :nationalScope or facility.id = :facilityId");
		params.put("facilityId", facilityId);
		params.put("nationalScope", ScopeType.NATIONAL);

		whereClauseItems.add("o.inactive != :inactiveStatus");
		params.put("inactiveStatus", Boolean.TRUE);

		whereClauseItems.add("d.donorType.id = :donorTypeId");
		params.put("donorTypeId", donorType.getId());

		String[] nameTerms = new String[0];
		if (StringUtils.isNotBlank(name))
			nameTerms = name.split("\\s+");
		for (int i = 0; i < nameTerms.length; i++) {
			params.put("nameComp" + i, "%" + nameTerms[i].toLowerCase() + "%");
		}

		StringBuilder orgFrag = new StringBuilder("TYPE(o) = :organizationClass");
		params.put("organizationClass", Organization.class);

		for (int i = 0; i < nameTerms.length; i++)
			orgFrag.append(" and LOWER(o.name) like :nameComp" + i);

		StringBuilder branchFrag = new StringBuilder("TYPE(o) = :branchClass");
		params.put("branchClass", OrganizationBranch.class);
		for (int i = 0; i < nameTerms.length; i++)
			branchFrag.append(" and CONCAT(parent.name, '-', o.name) like :nameComp" + i);

		cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		whereClauseItems.add("(" + orgFrag + ") or (" + branchFrag + ")");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("CONCAT(parent.name, '-', o.name)");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		@SuppressWarnings("unchecked")
		List<Donor> volunteerResultObjList = q.setMaxResults(maxResults).getResultList();
		return volunteerResultObjList;
	}

	public Donor findByVolunteerFK(Long volunteerId) {
		@SuppressWarnings("unchecked")
		List<Donor> l = query("from " + Donor.class.getName() + " where volunteer.id = :volunteerId")
				.setParameter("volunteerId", volunteerId).getResultList();

		return l.isEmpty() ? null : l.get(0);
	}

	public Donor findByOrganizationFK(Long orgId) {
		@SuppressWarnings("unchecked")
		List<Donor> l = query("from " + Donor.class.getName() + " where organization.id = :orgId")
				.setParameter("orgId", orgId).getResultList();
		return l.isEmpty() ? null : l.get(0);
	}
}
