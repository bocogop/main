package org.bocogop.wr.persistence.impl.lookup;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.persistence.dao.lookup.LanguageDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class LanguageDAOImpl extends GenericHibernateLookupDAOImpl<Language> implements LanguageDAO {

	@Override
	public Language findByLanguageCode(String languageCode) {
		return (Language) query("select l from " + Language.class.getName() + " l where SUBSTRING(l.culture, 1, "
				+ languageCode.length() + ") = :language").setParameter("language", languageCode).getSingleResult();
	}

}
