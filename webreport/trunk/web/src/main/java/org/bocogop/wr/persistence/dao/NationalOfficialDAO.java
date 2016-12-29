package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.organization.NationalOfficial;

public interface NationalOfficialDAO extends CustomizableSortedDAO<NationalOfficial> {

	/**
	 *
	 * @param organizationId
	 * @param activeAsOfDate
	 * @return
	 */
	List<NationalOfficial> findByCriteria(Long organizationId, LocalDate activeAsOfDate);

	/**
	 * 
	 * @param name
	 * @return
	 */
	
//	public NationalOfficial findByVAVSTitle(Long organizationId, String name);
}
