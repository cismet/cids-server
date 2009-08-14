/*
 * QueryStoreImpl.java
 *
 * Created on 19. November 2003, 18:38
 */

package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;
import Sirius.server.newuser.*;
import Sirius.server.*;
import Sirius.server.naming.NameServer;
import Sirius.server.middleware.interfaces.proxy.*;

/**
 *
 * @author  schlob
 */
public class QueryStoreImpl  {
    
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    private java.util.Hashtable activeLocalServers;
    
    private NameServer nameServer;
    
   
    
    /** Creates a new instance of QueryStoreImpl */
    public QueryStoreImpl(java.util.Hashtable activeLocalServers, 
    NameServer nameServer)throws RemoteException  
    {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
       
       
    }
    
    public boolean delete(int id, String domain) throws RemoteException {
        logger.debug("delete in QueryStore id:: "+id +" domain::"+domain);
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(domain)).delete(id);
        
    }    
    
    public Sirius.server.search.store.QueryData getQuery(int id, String domain) throws RemoteException {
         logger.debug("getQuery in QueryStore id:: "+id +" domain::"+domain); 
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(domain)).getQuery(id);
    }    
    
    public Sirius.server.search.store.Info[] getQueryInfos(UserGroup userGroup) throws RemoteException {
          logger.debug("getQueryInfos QueryStore userGroup:: "+userGroup);
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(userGroup.getDomain())).getQueryInfos(userGroup);
    }    
    
    public Sirius.server.search.store.Info[] getQueryInfos(User user) throws RemoteException {
        logger.debug("getQueryInfos QueryStore user:: "+user); 
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(user.getDomain())).getQueryInfos(user);
    }    
    
    public boolean storeQuery(User user, Sirius.server.search.store.QueryData data) throws RemoteException {
        logger.debug("storeQuery QueryStore :: "+data); 
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(data.getDomain())).storeQuery(user,data);
    }    
    
    
}
