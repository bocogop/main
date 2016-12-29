package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.volunteer.ParkingSticker;
import org.bocogop.wr.persistence.dao.ParkingStickerDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class ParkingStickerDAOImpl extends GenericHibernateSortedDAOImpl<ParkingSticker> implements ParkingStickerDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ParkingStickerDAOImpl.class);

	@Value("${donorSearch.maxResults}")
	private int maxResults;

	@Override
	public List<ParkingSticker> findByCriteria(String stickerNumber, State state, String licensePlate) {
		StringBuilder sb = new StringBuilder("select v from ").append(ParkingSticker.class.getName()).append(" v");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(stickerNumber)) {
			whereClauseItems.add("LOWER(v.stickerNumber) = :stickerNumber");
			params.put("stickerNumber", stickerNumber.toLowerCase());
		}

		if (state != null) {
			whereClauseItems.add("v.state.id = :stateId");
			params.put("stateId", state.getId());
		}

		if (StringUtils.isNotBlank(licensePlate)) {
			whereClauseItems.add("LOWER(v.licensePlate) = :licensePlate");
			params.put("licensePlate", licensePlate.toLowerCase());
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		@SuppressWarnings("unchecked")
		List<ParkingSticker> resultList = q.setMaxResults(maxResults).getResultList();
		return resultList;
	}

}
