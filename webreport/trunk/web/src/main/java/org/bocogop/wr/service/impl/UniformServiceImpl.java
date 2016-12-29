package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.model.volunteer.Uniform;
import org.bocogop.wr.service.UniformService;

@Service
public class UniformServiceImpl extends AbstractServiceImpl implements UniformService {
	private static final Logger log = LoggerFactory.getLogger(UniformServiceImpl.class);

	@Override
	public Uniform saveOrUpdate(Uniform uniform) {
		return uniformDAO.saveOrUpdate(uniform);
	}

	@Override
	public void delete(long uniformId) {
		uniformDAO.delete(uniformId);
	}

}
