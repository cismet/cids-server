/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * TransactionService.java
 *
 * Created on 10. August 2004, 11:19
 */
package Sirius.server.middleware.interfaces.proxy;
import Sirius.server.newuser.*;

import java.rmi.*;

import java.util.*;

/**
 * enables calling of name referenced remote methods NOT USED.
 *
 * @author      schlob
 * @version     $Revision$, $Date$
 * @deprecated  NOT USED
 */
public interface TransactionService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * executes a list of transactions.
     *
     * @param       transactions  list of transactions
     *
     * @return      result of a list of transactions
     *
     * @throws      RemoteException  server error (eg no such method)
     *
     * @deprecated  NOT USED in cids
     */
    int executeTransactionList(ArrayList transactions) throws RemoteException;
}
