package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.persistence.dao.DonationDetailDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonationDetailDAOImpl extends GenericHibernateSortedDAOImpl<DonationDetail> implements DonationDetailDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonationDetailDAOImpl.class);

	/*
	 * @Override public List<DonationDetail> findByCriteria(Long id,
	 * QueryCustomization... customization) { return null; }
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<DonationDetail> findByDonationSummaryId(long donationSummaryId, boolean includeNullOfDonGenPostFund) {
		StringBuilder sb = new StringBuilder("select d from ").append(DonationDetail.class.getName()).append(" d");
		sb.append(" left join fetch d.donationSummary s");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null && !includeNullOfDonGenPostFund)
			cust.setOrderBy("d.donGenPostFund.generalPostFund");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (donationSummaryId > 0) {
			whereClauseItems.add("d.donationSummary.id = :donationSummaryId");
			params.put("donationSummaryId", donationSummaryId);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

}
