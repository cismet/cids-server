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
 *
 * @author  schlob
 */
public class TransactionServiceImpl
{
    TransactionExecuter executer;
    
    
    /** Creates a new instance of TransactionServiceImpl */
    public TransactionServiceImpl(Object executingService)
    {
        
        this.executer=new TransactionExecuter(executingService);
        
    }
    
    
    
     public int executeTransactionList( ArrayList transactions) throws RemoteException
     {
     
        return executer.execute(transactions);
     
     }
    
}
