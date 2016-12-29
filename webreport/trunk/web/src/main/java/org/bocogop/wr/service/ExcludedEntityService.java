package org.bocogop.wr.service;

import java.io.IOException;

public interface ExcludedEntityService {

	int refreshDataAndUpdateVolunteers() throws IOException;

	void updateVolunteers();

}
