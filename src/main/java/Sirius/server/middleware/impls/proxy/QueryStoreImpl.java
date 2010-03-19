/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryStoreImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private java.util.Hashtable activeLocalServers;

    private NameServer nameServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryStoreImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public QueryStoreImpl(java.util.Hashtable activeLocalServers,
            NameServer nameServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean delete(int id, String domain) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete in QueryStore id:: " + id + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(domain)).delete(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Sirius.server.search.store.QueryData getQuery(int id, String domain) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQuery in QueryStore id:: " + id + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(domain)).getQuery(
                id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Sirius.server.search.store.Info[] getQueryInfos(UserGroup userGroup) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQueryInfos QueryStore userGroup:: " + userGroup);
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(userGroup.getDomain()))
                    .getQueryInfos(userGroup);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Sirius.server.search.store.Info[] getQueryInfos(User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQueryInfos QueryStore user:: " + user);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(user.getDomain()))
                    .getQueryInfos(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean storeQuery(User user, Sirius.server.search.store.QueryData data) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("storeQuery QueryStore :: " + data);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(data.getDomain()))
                    .storeQuery(user, data);
    }
}
