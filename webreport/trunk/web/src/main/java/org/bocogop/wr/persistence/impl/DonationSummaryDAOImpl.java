package org.bocogop.wr.persistence.impl;

import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.ORGANIZATION;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.ORG_AND_INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.OTHER_AND_INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.OTHER_GROUPS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.persistence.dao.DonationSummaryDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonationSummaryDAOImpl extends GenericHibernateSortedDAOImpl<DonationSummary>
		implements DonationSummaryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonationSummaryDAOImpl.class);

	@Value("${donationSummaryList.maxResults}")
	private int maxResults;

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<DonationSummary> findByCriteria(Long facilityId, String donorName, Long donationId,
			LocalDate donationsOnOrAfter, LocalDate donationsOnOrBefore, Collection<DonorType> requiredDonorTypes,
			boolean includeAcknowledged, boolean includeUnacknowledged, QueryCustomization... customization) {
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select ds from ").append(DonationSummary.class.getName()).append(" ds") //
				.append(" left join fetch ds.donor d") //
				.append(" left join d.volunteer v") //
				.append(" left join fetch ds.organization o") //
				.append(" left join fetch d.organization o2") //
				.append(" left join fetch o2.organization o2_parent");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "ds");

		if (donationId != null) {
			whereClauseItems.add("ds.id = :donationId");
			params.put("donationId", donationId);
		} else {
			if (facilityId != null) {
				whereClauseItems.add("ds.facility.id = :facilityId");
				params.put("facilityId", facilityId);
			}

			if (StringUtils.isNotBlank(donorName)) {
				StringBuilder nameSb = new StringBuilder();

				int i = 0;
				String[] nameTerms = donorName.split("\\s+");

				// ---------- individual who's a volunteer
				nameSb.append("(d.donorType.id = :individualDonorTypeId and v is not null and (");
				params.put("individualDonorTypeId", INDIVIDUAL.getId());
				for (int t = 0; t < nameTerms.length; i++, t++) {
					if (t > 0)
						nameSb.append(" and ");
					nameSb.append("(v.firstName like :nameComp" + i + " or v.middleName like :nameComp" + i
							+ " or v.lastName like :nameComp" + i + ")");
					params.put("nameComp" + i, "%" + nameTerms[t].replaceAll("[^A-Za-z]", "") + "%");
				}
				nameSb.append("))");

				// ---------- individual who's not a volunteer
				nameSb.append(" or (d.donorType.id = :individualDonorTypeId and v is null and (");
				for (int t = 0; t < nameTerms.length; i++, t++) {
					if (t > 0)
						nameSb.append(" and ");
					nameSb.append("(d.firstName like :nameComp" + i + " or d.middleName like :nameComp" + i
							+ " or d.lastName like :nameComp" + i + ")");
					params.put("nameComp" + i, "%" + nameTerms[t].replaceAll("[^A-Za-z]", "") + "%");
				}
				nameSb.append("))");

				// ---------- organization
				nameSb.append(" or (d.donorType.id in (:organizationDonorTypeIds) and (");
				params.put("organizationDonorTypeIds", Arrays.asList(ORGANIZATION.getId(), ORG_AND_INDIVIDUAL.getId()));
				for (int t = 0; t < nameTerms.length; i++, t++) {
					if (t > 0)
						nameSb.append(" and ");
					nameSb.append("((TYPE(o2) = :orgClassType and o2.name like :nameComp" + i
							+ ") or (TYPE(o2) = :branchClassType and (o2.name like :nameComp" + i
							+ " or o2_parent.name like :nameComp" + i + ")))");
					params.put("orgClassType", Organization.class);
					params.put("branchClassType", OrganizationBranch.class);
					params.put("nameComp" + i, "%" + nameTerms[t] + "%");
				}
				nameSb.append("))");

				// ---------- combo columns - match against either
				nameSb.append(" or (d.donorType.id in (:otherGroupDonorTypeIds) and (");
				params.put("otherGroupDonorTypeIds",
						Arrays.asList(ORG_AND_INDIVIDUAL.getId(), OTHER_AND_INDIVIDUAL.getId(), OTHER_GROUPS.getId()));
				for (int t = 0; t < nameTerms.length; i++, t++) {
					if (t > 0)
						nameSb.append(" and ");
					nameSb.append("(d.otherGroup like :nameComp" + i + " or d.firstName like :nameComp" + i
							+ " or d.middleName like :nameComp" + i + " or d.lastName like :nameComp" + i + ")");
					params.put("nameComp" + i, "%" + nameTerms[t].replaceAll("[^A-Za-z]", "") + "%");
				}
				nameSb.append("))");

				whereClauseItems.add(nameSb.toString());
			}

			if (donationsOnOrAfter != null) {
				whereClauseItems.add("ds.donationDate >= :donationsOnOrAfter");
				params.put("donationsOnOrAfter", donationsOnOrAfter);
			}

			if (donationsOnOrBefore != null) {
				whereClauseItems.add("ds.donationDate <= :donationsOnOrBefore");
				params.put("donationsOnOrBefore", donationsOnOrBefore);
			}

			if (requiredDonorTypes != null) {
				whereClauseItems.add("d.donorType in (:donorTypes)");
				params.put("donorTypes", requiredDonorTypes);
			}

			if (!includeAcknowledged)
				whereClauseItems.add("ds.acknowledgementDate is null");
			if (!includeUnacknowledged) {
				whereClauseItems.add("ds.acknowledgementDate is not null");
			}
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("ds.donationDate desc, d.lastName, d.firstName, d.middleName");

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		return new TreeSet<DonationSummary>(q.setMaxResults(maxResults).getResultList());
	}

	@Override
	public DonationSummary findByEpayTrackingID(String epayTrackingID) {
		@SuppressWarnings("unchecked")
		List<DonationSummary> ds = em
				.createQuery("from " + DonationSummary.class.getName() + " where epayTrackingID = :epayTrackingID")
				.setParameter("epayTrackingID", epayTrackingID).getResultList();
		return ds.isEmpty() ? null : ds.get(0);
	}

}
