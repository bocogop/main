package org.bocogop.wr.persistence.dao;

import org.bocogop.wr.model.ApplicationParameter;

public interface ApplicationParametersDAO extends CustomizableAppDAO<ApplicationParameter> {
	ApplicationParameter findByName(String paramName);
}
