package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.persistence.dao.DonationReferenceDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonationReferenceDAOImpl extends GenericHibernateSortedDAOImpl<DonationReference>
		implements DonationReferenceDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonationReferenceDAOImpl.class);

	@Override
	public DonationReference findByInstitutionAndRef(String stationNum, String reference) {
		StringBuilder sb = new StringBuilder("select d from ").append(DonationReference.class.getName()).append(" d");
		sb.append(" left join fetch d.facility v");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(stationNum)) {
			whereClauseItems.add("v.stationNumber = :stationNum");
			params.put("stationNum", stationNum);
		}

		if (StringUtils.isNotBlank(reference)) {
			whereClauseItems.add("d.donationReference = :reference");
			params.put("reference", reference);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return (DonationReference) q.getResultList().get(0);
	}
	
	/*
	 * @Override public List<DonationReference> findByCriteria(Long id,
	 * QueryCustomization... customization) { return null; }
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<DonationReference> findDonReferenceByFacilityId(long facilityId) {
		StringBuilder sb = new StringBuilder("select d from ").append(DonationReference.class.getName()).append(" d");
		sb.append(" left join fetch d.facility f");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null)
			cust.setOrderBy("d.donationReference");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (facilityId > 0) {
			whereClauseItems.add("d.facility.id = :facilityId");
			params.put("facilityId",facilityId);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

}
