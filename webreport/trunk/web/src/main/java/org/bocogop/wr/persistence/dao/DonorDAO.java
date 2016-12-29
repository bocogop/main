package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.persistence.impl.DonorDAOImpl.DonorSearchResult;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface DonorDAO extends CustomizableSortedDAO<Donor> {


	/**
	 * 
	 * @param donorType
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @param orgName TODO
	 * @param city
	 * @param state
	 * @param zip
	 * @param email
	 * @param phone
	 * @return
	 */
	public List<DonorSearchResult> findByCriteria(DonorType donorType, String firstName, String middleName, String lastName, 
			String orgName, String city, State state, String zip, String email, String phone, Long facilityId, QueryCustomization... customization);
	
	public Donor findByVolunteerFK(Long volunteerId);

	public Donor findByOrganizationFK(Long orgId);

}
