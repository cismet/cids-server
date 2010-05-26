/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * TransactionServiceImpl.java
 *
 * Created on 10. August 2004, 14:45
 */
package Sirius.server.middleware.impls.proxy;

import Sirius.server.transaction.*;

import java.rmi.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TransactionServiceImpl {

    //~ Instance fields --------------------------------------------------------

    TransactionExecuter executer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TransactionServiceImpl.
     *
     * @param  executingService  DOCUMENT ME!
     */
    public TransactionServiceImpl(final Object executingService) {
        this.executer = new TransactionExecuter(executingService);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   transactions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int executeTransactionList(final ArrayList transactions) throws RemoteException {
        return executer.execute(transactions);
    }
}
