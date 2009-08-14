/*
 * RemoteObserverImpl.java
 *
 * Created on 25. September 2003, 12:29
 */

package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;
import Sirius.server.observ.*;
import Sirius.server.*;
import Sirius.server.naming.NameServer;
import Sirius.server.middleware.interfaces.domainserver.*;
/**
 *
 * @author  awindholz
 */
public class RemoteObserverImpl
 {
      private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    private java.util.Hashtable activeLocalServers;
    private NameServer nameServer;
//    private Sirius.Server.Server[] localServers;
    
    /** Creates a new instance of RemoteObserverImpl */
    public RemoteObserverImpl(
    java.util.Hashtable activeLocalServers, 
    NameServer nameServer
    /*,
    Sirius.Server.Server[] localServers*/) throws RemoteException {
        this.nameServer = nameServer;
        this.activeLocalServers = activeLocalServers;
//        this.localServers = localServers;
    }
    
    /** Diese Funktion wird immer dann aufgerufen, wenn sich ein neuer LocalServer beim
     * CentralServer registriert. Der CentralServer informiert die CallServer (Observer),
     * dass ein neuer LocalServer hinzugekommen ist. Der/Die CallServer aktualisieren
     * ihre Liste mit den LocalServern.
     **/
    public void update(RemoteObservable obs, java.lang.Object arg) throws RemoteException {
        logger.debug("Info <CS> Observer::update\n");
        
        try {
           Server[] localServers;
            localServers = nameServer.getServers(ServerType.LOCALSERVER);
            
           logger.debug("<CS> Verfuegbare LocalServer:");
            
            activeLocalServers.clear();
            
            for(int i = 0; i<localServers.length ;i++) {
                Remote localServer =(Remote) Naming.lookup(localServers[i].getRMIAddress());
                activeLocalServers.put(localServers[i].getName(),localServer );
                logger.debug("\t"+localServers[i].getName());
            }
            logger.debug("<CS> Observer::update beendet\n");
            
        } catch(java.rmi.NotBoundException nbe) {
           logger.error(nbe);
            throw new RemoteException(nbe.getMessage()+"Fehler in Update\n\n");
            
        } catch(Exception e) {
           logger.error(e);
            throw new RemoteException(e.getMessage()+"Fehler in update");
        }
    }
}