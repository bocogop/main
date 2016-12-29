package org.bocogop.wr.service;

import org.bocogop.wr.model.BinaryObject;

public interface BinaryObjectService {

	BinaryObject saveOrUpdate(BinaryObject bo);

	void delete(long binaryObjectId);

}
