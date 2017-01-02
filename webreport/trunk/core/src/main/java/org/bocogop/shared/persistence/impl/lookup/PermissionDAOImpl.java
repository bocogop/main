package org.bocogop.shared.persistence.impl.lookup;

import java.util.List;
import java.util.SortedSet;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.persistence.dao.PermissionDAO;
import org.bocogop.shared.persistence.impl.AbstractAppLookupDAOImpl;
import org.bocogop.shared.util.cache.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionDAOImpl extends AbstractAppLookupDAOImpl<Permission> implements PermissionDAO {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PermissionDAOImpl.class);

	@Override
	@Cacheable(value = CacheNames.QUERIES_PERMISSION_DAO)
	public List<Permission> findAll() {
		return super.findAll();
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_PERMISSION_DAO)
	public SortedSet<Permission> findAllSorted() {
		return super.findAllSorted();
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_PERMISSION_DAO)
	public SortedSet<Permission> findAllSorted(Boolean active) {
		return super.findAllSorted(active);
	}

}
