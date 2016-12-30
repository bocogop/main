package org.bocogop.wr.persistence.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.AppUserDAO;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.util.PersistenceUtil;
import org.bocogop.wr.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class AppUserDAOImpl extends AbstractAppSortedDAOImpl<AppUser> implements AppUserDAO {

	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private PrecinctDAO precinctDAO;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public AppUser saveOrUpdate(AppUser item) {
		flushIfDebug();
		boolean userWasPersistent = item.isPersistent();
		String password = item.getPassword();
		item = super.saveOrUpdate(item);
		if (!userWasPersistent && password != null) {
			boolean passwordUpdated = updatePassword(item.getId(), password);
			if (!passwordUpdated)
				throw new IllegalStateException(
						"Couldn't update the password for " + AppUser.class.getName() + " with ID " + item.getId());
			refresh(item);
		}
		flushIfDebug();
		return item;
	}

	private boolean updatePassword(long appUserId, String password) {
		boolean b = em
				.createQuery("update " + AppUser.class.getName() + " set password = :password where id = :appUserId")
				.setParameter("password", passwordEncoder.encode(password)).setParameter("appUserId", appUserId)
				.executeUpdate() > 0;
		flushIfDebug();
		return b;

	}

	@Override
	public int updateFieldsWithoutVersionCheck(long appUserID, boolean incrementVersion, Long lastVisitedPrecinctId,
			ZonedDateTime accountLockDate, Integer failedLoginCount, String hashedPassword) {
		/*
		 * Necessary in case we made changes prior to this that haven't been
		 * flushed yet - CPB
		 */
		em.flush();

		List<String> updateItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (lastVisitedPrecinctId != null) {
			Precinct lastVisitedPrecinct = precinctDAO.findRequiredByPrimaryKey(lastVisitedPrecinctId);
			updateItems.add("lastVisitedPrecinct = :lastVisitedPrecinct");
			params.put("lastVisitedPrecinct", lastVisitedPrecinct);
		}

		if (hashedPassword != null) {
			updateItems.add("password = :hashedPassword");
			params.put("hashedPassword", hashedPassword);
		}

		if (failedLoginCount != null) {
			updateItems.add("failedLoginCount = :failedLoginCount");
			params.put("failedLoginCount", failedLoginCount);
		}

		if (accountLockDate != null) {
			updateItems.add("accountLockDate = :accountLockDate");
			params.put("accountLockDate", accountLockDate);
		}

		if (updateItems.isEmpty())
			throw new IllegalArgumentException("At least one update item must be specified");

		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(AppUser.class.getName()).append(" set ");
		if (incrementVersion)
			sb.append("version = version + 1, ");
		sb.append(StringUtils.join(updateItems, ", "));

		sb.append(" where id = :appUserID");
		params.put("appUserID", appUserID);

		Query q = em.createQuery(sb.toString());
		for (Map.Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());

		int changes = q.executeUpdate();
		flushIfDebug();
		return changes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AppUser> findByCriteria(Collection<String> usernamesCaseInsensitive, String userDn,
			boolean preinitialize, String lastName, boolean wildcardLastName, String firstName,
			boolean wildcardFirstName) {
		StringBuilder sb = new StringBuilder("select t from ").append(AppUser.class.getName()).append(" t");
		if (preinitialize) {
			sb.append(" left join fetch t.globalRoles gr");
			sb.append(" left join fetch gr.role r");
			sb.append(" left join fetch r.internalPermissions ip");
			sb.append(" left join fetch ip.permission p");
		}
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (CollectionUtils.isNotEmpty(usernamesCaseInsensitive)) {
			whereClauseItems.add("t.username in (:usernames)");
			params.put("usernames", usernamesCaseInsensitive);
		}

		if (StringUtils.isNotBlank(userDn)) {
			whereClauseItems.add("t.userDn = :userDn");
			params.put("userDn", userDn);
		}

		if (StringUtils.isNotBlank(lastName)) {
			if (wildcardLastName) {
				whereClauseItems.add("t.lastName like :lastName");
				params.put("lastName", "%" + lastName + "%");
			} else {
				whereClauseItems.add("t.lastName = :lastName");
				params.put("lastName", lastName);
			}
		}

		if (StringUtils.isNotBlank(firstName)) {
			if (wildcardFirstName) {
				whereClauseItems.add("t.firstName like :firstName");
				params.put("firstName", "%" + firstName + "%");
			} else {
				whereClauseItems.add("t.firstName = :firstName");
				params.put("firstName", firstName);
			}
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, null);
		List<AppUser> u = q.getResultList();
		if (!preinitialize)
			return u;

		/*
		 * Duplicates must be manually removed due to prefetching - see
		 * Hibernate FAQ's - CPB
		 */
		return new TreeSet<>(u);
	}

	@Override
	public AppUser findByUsername(String username, boolean preinitialize) {
		Collection<AppUser> users = findByCriteria(Arrays.asList(username), null, preinitialize, null, false, null,
				false);
		if (users.isEmpty())
			return null;
		if (users.size() > 1)
			throw new IllegalStateException("More than one user was found with the username '" + username + "': IDs "
					+ StringUtils.join(PersistenceUtil.translateObjectsToIds(users), ","));
		AppUser appUser = users.iterator().next();
		return appUser;
	}

	@Override
	public AppUser findRequiredByUsername(String username, boolean preinitialize) {
		AppUser u = findByUsername(username, preinitialize);
		if (u == null)
			throw new IllegalArgumentException(
					"No " + AppUser.class.getSimpleName() + " with username " + username + " was found.");
		return u;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QuickSearchResult> findByNameOrUsername(String text, int maxResults) {
		if (StringUtils.isBlank(text))
			return new ArrayList<>();

		String[] tokens = text.split("\\W");

		StringBuilder sb = new StringBuilder();
		sb.append("select t.id, t.firstName, t.middleName, t.lastName, lower(t.username) from ");
		sb.append(AppUser.class.getName());
		sb.append(" t where 1=1");

		Map<String, String> params = new HashMap<>();

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (StringUtils.isBlank(token))
				continue;

			sb.append(" and (lower(t.firstName) like :text").append(i);
			sb.append(" or lower(t.lastName) like :text").append(i);
			sb.append(" or lower(t.username) like :text").append(i).append(")");
			params.put("text" + i, "%" + token.toLowerCase() + "%");
		}
		Query q = query(sb.toString());
		for (Entry<String, String> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());
		List<Object[]> results = q.setMaxResults(maxResults).getResultList();

		List<QuickSearchResult> returnResults = new ArrayList<>(results.size());
		for (Object[] result : results) {
			returnResults.add(new QuickSearchResult(((Number) result[0]).longValue(),
					StringUtil.getDisplayName(true, (String) result[1], (String) result[2], (String) result[3], null),
					(String) result[4]));
		}
		return returnResults;
	}

}
