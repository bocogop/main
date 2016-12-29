package org.bocogop.wr.service.impl;

import org.springframework.stereotype.Service;

import org.bocogop.wr.model.BinaryObject;
import org.bocogop.wr.service.BinaryObjectService;

@Service
public class BinaryObjectServiceImpl extends AbstractServiceImpl implements BinaryObjectService {

	@Override
	public BinaryObject saveOrUpdate(BinaryObject bo) {
		BinaryObject persistentBO = binaryObjectDAO.saveOrUpdate(bo);
		return persistentBO;
	}

	@Override
	public void delete(long binaryObjectId) {
		binaryObjectDAO.delete(binaryObjectId);
	}

}
