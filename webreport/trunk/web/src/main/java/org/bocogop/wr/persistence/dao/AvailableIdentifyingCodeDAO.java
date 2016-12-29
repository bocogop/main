package org.bocogop.wr.persistence.dao;

import org.bocogop.wr.model.volunteer.AvailableIdentifyingCode;

public interface AvailableIdentifyingCodeDAO extends CustomizableAppDAO<AvailableIdentifyingCode> {

	AvailableIdentifyingCode getFirstUnused();

}
