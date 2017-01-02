package org.bocogop.shared.persistence.impl.lookup;

import java.util.List;
import java.util.SortedSet;

import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.impl.AbstractAppLookupDAOImpl;
import org.bocogop.shared.util.cache.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDAOImpl extends AbstractAppLookupDAOImpl<Role> implements RoleDAO {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RoleDAOImpl.class);

	@Override
	@Cacheable(value = CacheNames.QUERIES_ROLE_DAO)
	public List<Role> findAll() {
		return super.findAll();
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_ROLE_DAO)
	public SortedSet<Role> findAllSorted() {
		return super.findAllSorted();
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_ROLE_DAO)
	public SortedSet<Role> findAllSorted(Boolean active) {
		return super.findAllSorted(active);
	}

}
