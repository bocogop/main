package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission;
import org.bocogop.wr.model.letterTemplate.LetterTemplate;
import org.bocogop.wr.service.LetterTemplateService;

@Service
public class LetterTemplateServiceImpl extends AbstractServiceImpl implements LetterTemplateService {
	private static final Logger log = LoggerFactory.getLogger(LetterTemplateServiceImpl.class);

	@Override
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public LetterTemplate saveOrUpdate(LetterTemplate letterTemplate) {
		/* Business-level validations */

		letterTemplate = letterTemplateDAO.saveOrUpdate(letterTemplate);
		return letterTemplate;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public void delete(long letterTemplateId) {
		letterTemplateDAO.delete(letterTemplateId);
	}

}
