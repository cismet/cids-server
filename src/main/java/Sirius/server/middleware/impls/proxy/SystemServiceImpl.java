/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SystemServiceImpl.java
 *
 * Created on 25. September 2003, 12:42
 */
package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;

import Sirius.server.*;
import Sirius.server.naming.NameServer;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.middleware.interfaces.proxy.*;

import Sirius.util.image.*;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class SystemServiceImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private NameServer nameServer;
    private java.util.Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SystemServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SystemServiceImpl(java.util.Hashtable activeLocalServers, NameServer nameServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Image[] getDefaultIcons(String lsName) throws RemoteException {
        if(logger.isInfoEnabled())
            logger.info("Info <CS> getDefIcons from " + lsName);   // NOI18N
        Image[] i = new Image[0];
        Sirius.server.middleware.interfaces.domainserver.SystemService s = null;

        try {
            s = (Sirius.server.middleware.interfaces.domainserver.SystemService)activeLocalServers.get(lsName.trim());
            i = s.getDefaultIcons();
            if (logger.isDebugEnabled()) {
                logger.debug("image[] " + i);   // NOI18N
            }
        } catch (Exception e) {
            logger.error("Info <CS> getDefIcons from " + lsName + " failed", e);   // NOI18N

            throw new RemoteException("getDefIcons(lsName) failed", e);   // NOI18N
        }

        return i;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Image[] getDefaultIcons() throws RemoteException {
        Image[] i = new Image[0];
        Sirius.server.middleware.interfaces.domainserver.SystemService s = null;

        try {
            if (activeLocalServers.size() > 0) {
                s = (Sirius.server.middleware.interfaces.domainserver.SystemService)activeLocalServers.values()
                            .iterator().next();
                if (logger.isDebugEnabled()) {
                    logger.debug("<CS> getDefIcons");   // NOI18N
                }
                i = s.getDefaultIcons();
            } else {
                throw new Exception("no LocalServer registered!");   // NOI18N
            }
        } catch (Exception e) {
            logger.error("Info <CS> getDefIcons failed", e);   // NOI18N

            throw new RemoteException("getDefIcons(void) failed", e);   // NOI18N
        }

        return i;
    }

//    public Sirius.Server.Server getTranslationServer() throws RemoteException {
//        Sirius.Server.Server[] ts = nameServer.getServers(ServerType.TRANSLATIONSERVER);
//
//        return ts[ts.length-1]; // der zuletzt angemeldete
//    }

}
