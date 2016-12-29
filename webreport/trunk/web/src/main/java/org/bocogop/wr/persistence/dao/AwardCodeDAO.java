package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface AwardCodeDAO extends CustomizableSortedDAO<Award> {
	public List<Award> findByCriteria(String name, String code, QueryCustomization... customization);
}
