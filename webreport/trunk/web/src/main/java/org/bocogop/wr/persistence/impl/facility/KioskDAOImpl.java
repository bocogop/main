package org.bocogop.wr.persistence.impl.facility;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.persistence.dao.facility.KioskDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class KioskDAOImpl extends GenericHibernateSortedDAOImpl<Kiosk> implements KioskDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(KioskDAOImpl.class);

	@Override
	public int bulkUpdateByCriteria(Collection<Long> kioskIds, ZonedDateTime lastPrinterStatusCheck) {
		if (kioskIds == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (lastPrinterStatusCheck == null)
			throw new IllegalArgumentException("No updates specified");

		if (kioskIds.isEmpty())
			return 0;

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (lastPrinterStatusCheck != null) {
			updates.add("lastPrinterStatusCheck = :lastPrinterStatusCheck");
			params.put("lastPrinterStatusCheck", lastPrinterStatusCheck);
		}

		String jpql = "update " + Kiosk.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ " where id in (select pr.id from " + Kiosk.class.getName() + " pr where 1=1" //
				+ (kioskIds != null ? " and pr.id in (:kioskIds)" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (kioskIds != null)
			q.setParameter("kioskIds", kioskIds);

		return q.executeUpdate();
	}

	@Override
	public List<Kiosk> findByCriteria(Boolean registrationStatus) {
		StringBuilder sb = new StringBuilder("select v from ").append(Kiosk.class.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (registrationStatus != null) {
			whereClauseItems.add("v.registered = :registrationStatus");
			params.put("registrationStatus", registrationStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Kiosk> resultList = q.getResultList();
		return resultList;
	}

}
