package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.organization.NationalOfficial;
import org.bocogop.wr.service.NationalOfficialService;

@Service
public class NationalOfficialServiceImpl extends AbstractServiceImpl implements NationalOfficialService {
	private static final Logger log = LoggerFactory.getLogger(NationalOfficialServiceImpl.class);

	
	@Override
	public NationalOfficial saveOrUpdate(NationalOfficial nationalOfficial) throws ServiceValidationException {
		
		/*if (nationalOfficial.getStdVAVSTitle().getLookupType() ==  StdVAVSTitleValue.NATIONAL_REPRESENTATIVE) {
			if (nationalOfficialDAO.findByVAVSTitle(nationalOfficial.getOrganization().getId(), StdVAVSTitleValue.NATIONAL_REPRESENTATIVE.getName()) != null) {
				throw new ServiceValidationException("nationalOfficial.saveOrUpdate.error.singleNationalRep"); 
			}
		}*/
		
		return nationalOfficialDAO.saveOrUpdate(nationalOfficial);
	}

	@Override
	public void delete(long nationalOfficialId) {
		nationalOfficialDAO.delete(nationalOfficialId);
	}
}
