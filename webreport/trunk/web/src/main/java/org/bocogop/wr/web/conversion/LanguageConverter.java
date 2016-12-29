package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.persistence.dao.lookup.LanguageDAO;

@Component
public class LanguageConverter extends AbstractStringToPersistentConverter<Language> {

	@Autowired
	protected LanguageConverter(LanguageDAO dao) {
		super(dao);
	}
}
