package org.bocogop.wr.persistence.dao.leie;

import org.bocogop.wr.model.leie.ExclusionType;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface ExclusionTypeDAO extends CustomizableAppDAO<ExclusionType> {

	ExclusionType findBySSA(String ssa);
	
}
