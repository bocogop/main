package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.persistence.dao.AwardCodeDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class AwardCodeDAOImpl extends GenericHibernateSortedDAOImpl<Award> implements AwardCodeDAO {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Award> findByCriteria(String name, String code, QueryCustomization... customization) {
		
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select aw from ").append(Award.class.getName())
				.append(" aw");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "aw");

		if (name != null) {
			whereClauseItems.add("TRIM(LOWER(aw.name)) = :name");
			params.put("name", name.toLowerCase().trim());
		}
	
		if (code != null) {
			whereClauseItems.add("TRIM(LOWER(aw.code)) = :code");
			params.put("code", code.toLowerCase().trim());
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("aw.name");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		return q.getResultList();
	}
}
