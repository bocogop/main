package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.organization.NationalOfficial;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class NationalOfficialDAOImpl extends GenericHibernateSortedDAOImpl<NationalOfficial> implements NationalOfficialDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NationalOfficialDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<NationalOfficial> findByCriteria(Long organizationId, LocalDate activeAsOfDate) {
		StringBuilder sb = new StringBuilder("select o from ").append(NationalOfficial.class.getName())
				.append(" o");
		sb.append(" left join fetch o.organization org");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.lastName, o.firstName");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (organizationId == null)
			throw new IllegalArgumentException(
					"Parameter 'organizationId' is required");

		whereClauseItems.add("org.id = :orgId");
		params.put("orgId", organizationId);
		
		
		if (activeAsOfDate != null) {
			whereClauseItems.add("COALESCE(o.vavsStartDate, '1900-01-01') <= :activeAsOfDate");
			whereClauseItems.add("COALESCE(o.vavsEndDate, '2199-01-01') > :activeAsOfDate");
			params.put("activeAsOfDate", activeAsOfDate);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}
	
	/*public NationalOfficial findByVAVSTitle(Long organizationId, String vavsTitleName) {
		try {
			StringBuilder sb = new StringBuilder("select o from ").append(NationalOfficial.class.getName())
					.append(" o");
			sb.append(" left join fetch o.stdVAVSTitle title");
			sb.append(" left join fetch o.organization org");
			
			List<String> whereClauseItems = new ArrayList<>();
			Map<String, Object> params = new HashMap<>();

			
			if (organizationId == null)
				throw new IllegalArgumentException(
						"Parameter 'organizationId' is required");
			whereClauseItems.add("org.id = :orgId");
			params.put("orgId", organizationId);

			if (vavsTitleName == null)
				throw new IllegalArgumentException(
						"Parameter 'vavsTitleName' is required");
			whereClauseItems.add("title.name = :vavsTitle");
			params.put("vavsTitle", vavsTitleName);

			Query q = constructQuery(em, sb, whereClauseItems, params, null);

			return (NationalOfficial) q.getSingleResult();
			
		} catch (NoResultException e) {
			return null;
		}
	}*/
}
