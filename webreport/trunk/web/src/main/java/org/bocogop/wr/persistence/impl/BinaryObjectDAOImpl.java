package org.bocogop.wr.persistence.impl;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.BinaryObject;
import org.bocogop.wr.persistence.dao.BinaryObjectDAO;

@Repository
public class BinaryObjectDAOImpl extends GenericHibernateDAOImpl<BinaryObject>implements BinaryObjectDAO {

}
