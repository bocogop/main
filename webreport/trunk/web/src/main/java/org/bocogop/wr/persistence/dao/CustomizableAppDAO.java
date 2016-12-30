package org.bocogop.wr.persistence.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bocogop.wr.persistence.AppDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface CustomizableAppDAO<T> extends AppDAO<T> {

	T findByPrimaryKey(Long id, QueryCustomization customizations);

	T findRequiredByPrimaryKey(Long id, QueryCustomization customization);

	Map<Long, T> findByPrimaryKeys(Collection<Long> primaryKeys, QueryCustomization customization);

	Map<Long, T> findRequiredByPrimaryKeys(Collection<Long> primaryKeys, QueryCustomization customization);

	List<T> findAll(QueryCustomization customization);

	<U extends T> List<U> findAllByType(Class<U> subtype, QueryCustomization customization);

}
