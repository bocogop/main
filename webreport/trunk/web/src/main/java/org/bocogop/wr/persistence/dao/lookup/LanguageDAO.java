package org.bocogop.wr.persistence.dao.lookup;

import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.persistence.dao.CustomizableLookupDAO;

public interface LanguageDAO extends CustomizableLookupDAO<Language> {

	Language findByLanguageCode(String language);

}
