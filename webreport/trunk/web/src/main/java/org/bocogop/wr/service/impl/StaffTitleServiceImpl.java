package org.bocogop.wr.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.service.StaffTitleService;

@Service
public class StaffTitleServiceImpl extends AbstractServiceImpl implements StaffTitleService {
	private static final Logger log = LoggerFactory.getLogger(StaffTitleServiceImpl.class);

	@Override
	public StaffTitle saveOrUpdate(StaffTitle staffTitle){
		return staffTitleDAO.saveOrUpdate(staffTitle);
	}
	
	@Override
	public void delete(long staffTitleId) throws ServiceValidationException {
		// An award code can be deleted if it has not been utilized.
		List<VoluntaryServiceStaff> linkedVolunteerServiceStaff = voluntaryServiceStaffDAO.findByStaffTitle(staffTitleId);
		if (!linkedVolunteerServiceStaff.isEmpty())
			throw new ServiceValidationException("staffTitle.error.staffTitleUsed");
		
		staffTitleDAO.delete(staffTitleId);
	}

}
