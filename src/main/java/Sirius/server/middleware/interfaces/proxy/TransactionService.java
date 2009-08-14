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
 * enables calling of name referenced remote methods
 * NOT USED
 * @author schlob
 * @deprecated NOT USED
 */
public interface TransactionService extends Remote
{
    
    /**
     * executes a list of transactions
     * @param transactions list of transactions
     * @throws java.rmi.RemoteException server error (eg no such method)
     * @return result of a list of transactions
     * @deprecated NOT USED in cids
     */
    public int executeTransactionList( ArrayList transactions) throws RemoteException;
    
   
    
}
