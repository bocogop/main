package org.bocogop.shared.util.context;

import org.bocogop.shared.model.lookup.sds.VAFacility;

/**
 * Represents the contract for any class responsible for providing
 * session-bound values.
 * 
 */
public interface ContextManager {
	VAFacility getSiteContext();

	void setSiteContext(VAFacility f);
}