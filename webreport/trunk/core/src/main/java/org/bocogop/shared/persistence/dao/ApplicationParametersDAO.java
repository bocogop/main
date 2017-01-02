package org.bocogop.shared.persistence.dao;

import org.bocogop.shared.model.ApplicationParameter;

public interface ApplicationParametersDAO extends CustomizableAppDAO<ApplicationParameter> {
	ApplicationParameter findByName(String paramName);
}
