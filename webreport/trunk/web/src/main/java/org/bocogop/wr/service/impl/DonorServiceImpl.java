package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.service.DonorService;

@Service
public class DonorServiceImpl extends AbstractServiceImpl implements DonorService {
	private static final Logger log = LoggerFactory.getLogger(DonorServiceImpl.class);

	@Override
	public Donor saveOrUpdate(Donor donor) throws ServiceValidationException {
		/* Business-level validations */

		donor = donorDAO.saveOrUpdate(donor);
		return donor;
	}

	@Override
	public Donor linkVolunteer(Long donorId, Long volunteerId) throws ServiceValidationException {

		Donor donor = null;

		if (volunteerId == null) {
			throw new IllegalArgumentException("Volunteer Id cannot be null");
		}

		if (donorDAO.findByVolunteerFK(volunteerId) != null) {
			throw new ServiceValidationException("donor.error.volunteerAlreadyLinked");
		}
		;

		if (donorId == null) {
			// new donor that should be created
			donor = new Donor();
			donor.setDonorType(donorTypeDAO.findByLookup(DonorType.DonorTypeValue.INDIVIDUAL));
		} else {
			donor = donorDAO.findRequiredByPrimaryKey(donorId);
			if (donor == null) {
				throw new IllegalArgumentException("Donor not found for donor id = " + donorId);
			}
		}

		// donor record should no longer retain name/address info
		clearDonorData(donor);

		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		donor.setVolunteer(v);

		donor = saveOrUpdate(donor);

		return donor;
	}

	@Override
	public Donor linkOrganization(Long donorId, Long orgId) throws ServiceValidationException {
		Donor donor = null;

		if (orgId == null) {
			throw new IllegalArgumentException("Organization Id cannot be null");
		}

		if (donorDAO.findByOrganizationFK(orgId) != null) {
			throw new ServiceValidationException("donor.error.orgAlreadyLinked");
		}
		;

		if (donorId == null) {
			// new donor that should be created
			donor = new Donor();
			donor.setDonorType(donorTypeDAO.findByLookup(DonorType.DonorTypeValue.ORGANIZATION));
		} else {
			donor = donorDAO.findRequiredByPrimaryKey(donorId);

			if (donor == null) {
				throw new IllegalArgumentException("Donor not found for donor id = " + donorId);
			}
		}

		// donor record should no longer retain name/address info
		clearDonorData(donor);

		AbstractBasicOrganization o = organizationDAO.findRequiredByPrimaryKey(orgId);
		donor.setOrganization(o);

		donor = saveOrUpdate(donor);

		return donor;

	}

	@Override
	public void delete(Long donorId) throws ServiceValidationException {
		donorDAO.delete(donorId);
	}

	private Donor clearDonorData(Donor donor) {
		donor.setAddressLine1(null);
		donor.setAddressLine2(null);
		donor.setCity(null);
		donor.setOtherGroup(null);
		donor.setEmail(null);
		donor.setFirstName(null);
		donor.setLastName(null);
		donor.setMiddleName(null);
		donor.setPhone(null);
		donor.setPrefix(null);
		donor.setState(null);
		donor.setSuffix(null);
		donor.setZip(null);
		return donor;
	}

}
