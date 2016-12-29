package org.bocogop.shared.persistence.impl.lookup;

import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.lookup.InactiveReason;
import org.bocogop.shared.persistence.impl.AbstractAppLookupDAOImpl;
import org.bocogop.shared.persistence.lookup.InactiveReasonDAO;

@Repository
public class InactiveReasonDAOImpl extends AbstractAppLookupDAOImpl<InactiveReason> implements InactiveReasonDAO {

}
