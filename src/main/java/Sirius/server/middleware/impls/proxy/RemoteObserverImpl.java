/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class RemoteObserverImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private java.util.Hashtable activeLocalServers;
    private NameServer nameServer;
//    private Sirius.Server.Server[] localServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RemoteObserverImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public RemoteObserverImpl(
            java.util.Hashtable activeLocalServers,
            NameServer nameServer
            /*,
             *Sirius.Server.Server[] localServers*/) throws RemoteException {
        this.nameServer = nameServer;
        this.activeLocalServers = activeLocalServers;
//        this.localServers = localServers;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Diese Funktion wird immer dann aufgerufen, wenn sich ein neuer LocalServer beim CentralServer registriert. Der
     * CentralServer informiert die CallServer (Observer), dass ein neuer LocalServer hinzugekommen ist. Der/Die
     * CallServer aktualisieren ihre Liste mit den LocalServern.
     *
     * @param   obs  DOCUMENT ME!
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public void update(RemoteObservable obs, java.lang.Object arg) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("Info <CS> Observer::update\n");   // NOI18N
        }

        try {
            Server[] localServers;
            localServers = nameServer.getServers(ServerType.LOCALSERVER);
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> Available LocalServer:");   // NOI18N
            }

            activeLocalServers.clear();

            for (int i = 0; i < localServers.length; i++) {
                Remote localServer = (Remote)Naming.lookup(localServers[i].getRMIAddress());
                activeLocalServers.put(localServers[i].getName(), localServer);
                if (logger.isDebugEnabled()) {
                    logger.debug("\t" + localServers[i].getName());   // NOI18N
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> Observer::update beendet\n");   // NOI18N
            }
        } catch (java.rmi.NotBoundException nbe) {
            logger.error(nbe);
            throw new RemoteException(nbe.getMessage() + "Error in Update\n\n");   // NOI18N
        } catch (Exception e) {
            logger.error(e);
            throw new RemoteException(e.getMessage() + "Error in update");   // NOI18N
        }
    }
}
