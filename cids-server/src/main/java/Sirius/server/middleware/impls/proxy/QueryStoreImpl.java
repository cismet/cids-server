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

import Sirius.server.*;
import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.*;

import java.rmi.*;
import java.rmi.server.*;

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
    public QueryStoreImpl(final java.util.Hashtable activeLocalServers, final NameServer nameServer)
            throws RemoteException {
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
    public boolean delete(final int id, final String domain) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete in QueryStore id:: " + id + " domain::" + domain); // NOI18N
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
    public Sirius.server.search.store.QueryData getQuery(final int id, final String domain) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQuery in QueryStore id:: " + id + " domain::" + domain); // NOI18N
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
    public Sirius.server.search.store.Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQueryInfos QueryStore userGroup:: " + userGroup); // NOI18N
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
    public Sirius.server.search.store.Info[] getQueryInfos(final User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getQueryInfos QueryStore user:: " + user); // NOI18N
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
    public boolean storeQuery(final User user, final Sirius.server.search.store.QueryData data) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("storeQuery QueryStore :: " + data); // NOI18N
        }
        return ((Sirius.server.middleware.interfaces.domainserver.QueryStore)activeLocalServers.get(data.getDomain()))
                    .storeQuery(user, data);
    }
}
