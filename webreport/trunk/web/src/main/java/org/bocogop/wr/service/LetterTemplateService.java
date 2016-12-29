package org.bocogop.wr.service;

import org.bocogop.wr.model.letterTemplate.LetterTemplate;

public interface LetterTemplateService {

	LetterTemplate saveOrUpdate(LetterTemplate letterTemplate);

	void delete(long letterTemplateId);

}
