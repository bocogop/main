package org.bocogop.wr.persistence.impl.leie;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.leie.ExclusionType;
import org.bocogop.wr.persistence.dao.leie.ExclusionTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;
import org.bocogop.wr.util.cache.CacheNames;

@Repository
public class ExclusionTypeDAOImpl extends GenericHibernateDAOImpl<ExclusionType> implements ExclusionTypeDAO {

	@Override
	@Cacheable(value = CacheNames.QUERIES_EXCLUSION_TYPE_DAO)
	public ExclusionType findBySSA(String ssa) {
		@SuppressWarnings("unchecked")
		List<ExclusionType> results = query("from " + ExclusionType.class.getName() + " where ssa = :ssa")
				.setParameter("ssa", ssa).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
