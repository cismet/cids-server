/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.Server;
import Sirius.server.ServerType;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.UserServer;
import Sirius.server.observ.RemoteObservable;
import Sirius.server.observ.RemoteObserver;
import Sirius.server.property.ServerProperties;
import Sirius.server.search.CidsServerSearch;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;

import Sirius.util.image.Image;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import de.cismet.cids.server.CallServerService;

/**
 * Benoetigte Keys fuer configFile: registryIps<br>
 * serverName<br>
 * jdbcDriver *
 *
 * @version  $Revision$, $Date$
 */
public final class ProxyImpl extends UnicastRemoteObject implements CallServerService, RemoteObserver {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProxyImpl.class);

    private static final transient Logger LOGINLOG = Logger.getLogger("de.cismet.cids.System");

    //~ Instance fields --------------------------------------------------------

    private final transient ServerProperties properties;
    // contains DomainServers
    private final transient Hashtable activeLocalServers;
    private final transient NameServer nameServer;
    private final transient CatalogueServiceImpl catService;
    private final transient MetaServiceImpl metaService;
    private final transient RemoteObserverImpl remoteObserver;
    private final transient SystemServiceImpl systemService;
    private final transient UserServiceImpl userService;
    private final transient QueryStoreImpl queryStore;
    private final transient SearchServiceImpl searchService;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProxyImpl object.
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public ProxyImpl(final ServerProperties properties) throws RemoteException {
        super(properties.getServerPort());
        LOGINLOG.info("SERVERSTART----");
        try {
            this.properties = properties;

            final String[] registryIps = properties.getRegistryIps();
            if ((registryIps == null) || (registryIps.length == 0)) {
                throw new IllegalStateException("no registry IPs found");
            }

            final String rmiPort = properties.getRMIRegistryPort();

            nameServer = (NameServer)Naming.lookup("rmi://" + registryIps[0] + ":" + rmiPort + "/nameServer"); // NOI18N
            final UserServer userServer = (UserServer)nameServer;                                              // Naming.lookup("rmi://"+registryIps[0]+"/userServer");
            activeLocalServers = new java.util.Hashtable(5);

            final Server[] localServers = nameServer.getServers(ServerType.LOCALSERVER);
            if (LOG.isDebugEnabled()) {
                LOG.debug("<CS> " + localServers.length + " LocalServer received from SiriusRegistry"); // NOI18N
            }

            for (int i = 0; i < localServers.length; i++) {
                final String name = localServers[i].getName();
                final String lookupString = localServers[i].getRMIAddress();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<CS> lookup: " + lookupString); // NOI18N
                }

                final Remote localServer = Naming.lookup(lookupString);
                activeLocalServers.put(name, localServer);
            }

            register();
            registerAsObserver(registryIps[0] + ":" + rmiPort);

            catService = new CatalogueServiceImpl(activeLocalServers);
            metaService = new MetaServiceImpl(activeLocalServers, nameServer);
            remoteObserver = new RemoteObserverImpl(activeLocalServers, nameServer);
            systemService = new SystemServiceImpl(activeLocalServers, nameServer);
            userService = new UserServiceImpl(activeLocalServers, userServer);
            queryStore = new QueryStoreImpl(activeLocalServers, nameServer);
            searchService = new SearchServiceImpl(activeLocalServers, nameServer);
        } catch (final RemoteException e) {
            final String message = "error during proxy startup"; // NOI18N
            LOG.error(message, e);
            throw e;
        } catch (final Exception e) {
            // running in an exception at construction time leads to invalid server!
            final String message = "error during proxy startup"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    NameServer getNameServer() {
        return nameServer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   usr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren for: " + node); // NOI18N
        }
        return catService.getChildren(node, usr);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final String localServerName) throws RemoteException {
        return catService.getRoots(user, localServerName);
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
    @Override
    public Node[] getRoots(final User user) throws RemoteException {
        return catService.getRoots(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node    DOCUMENT ME!
     * @param   parent  DOCUMENT ME!
     * @param   user    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return catService.addNode(node, parent, user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return catService.deleteNode(node, user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return catService.addLink(from, to, user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return catService.deleteLink(from, to, user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String localServerName) throws RemoteException {
        return metaService.getClassTreeNodes(user, localServerName);
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
    @Override
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        return metaService.getClassTreeNodes(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   classID          DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClass(final User user, final int classID, final String localServerName) throws RemoteException {
        return metaService.getClass(user, classID, localServerName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   tableName        DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClassByTableName(final User user, final String tableName, final String localServerName)
            throws RemoteException {
        return metaService.getClassByTableName(user, tableName, localServerName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getClasses(final User user, final String localServerName) throws RemoteException {
        return metaService.getClasses(user, localServerName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String[] getDomains() throws RemoteException {
        return metaService.getDomains();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr     DOCUMENT ME!
     * @param   nodeID  DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        return metaService.getMetaObjectNode(usr, nodeID, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr       DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     * @param   classID   DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        return metaService.getMetaObject(usr, objectID, classID, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return metaService.getMetaObjectNode(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final Query query) throws RemoteException {
        return metaService.getMetaObjectNode(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return metaService.getMetaObject(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query) throws RemoteException {
        return metaService.getMetaObject(usr, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return metaService.deleteMetaObject(user, metaObject, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int insertMetaObject(final User user, final Query query, final String domain) throws RemoteException {
        return metaService.insertMetaObject(user, query, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return metaService.insertMetaObject(user, metaObject, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return metaService.updateMetaObject(user, metaObject, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int update(final User user, final String query, final String domain) throws RemoteException {
        return metaService.update(user, query, domain);
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
    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        return metaService.getMethods(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final String lsName) throws RemoteException {
        return metaService.getMethods(user, lsName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons(final String lsName) throws RemoteException {
        return systemService.getDefaultIcons(lsName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons() throws RemoteException {
        return systemService.getDefaultIcons();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public User getUser(
            final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        LOGINLOG.info("Login: " + userName + "@" + userGroupName + "@" + userGroupLsName);
        return userService.getUser(
                userLsName,
                userGroupName,
                userGroupLsName,
                userName,
                password);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames() throws RemoteException {
        return userService.getUserGroupNames();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        return userService.getUserGroupNames(userName, lsHome);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        return userService.changePassword(user, oldPassword, newPassword);
    }

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
    @Override
    public void update(final RemoteObservable obs, final java.lang.Object arg) throws RemoteException {
        remoteObserver.update(obs, arg);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  RemoteException       DOCUMENT ME!
     * @throws  UnknownHostException  DOCUMENT ME!
     */
    // TODO: at least the unknownhostexception should most certainly be handled by the method
    protected void register() throws RemoteException, UnknownHostException {
        nameServer.registerServer(
            ServerType.CALLSERVER,
            properties.getServerName(),
            InetAddress.getLocalHost().getHostAddress(),
            properties.getRMIRegistryPort());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  siriusRegistryHost  DOCUMENT ME!
     */
    void registerAsObserver(final String siriusRegistryHost) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Info <CS> registerAsObserver:: " + siriusRegistryHost); // NOI18N
        }
        try {
            final RemoteObservable server = (RemoteObservable)Naming.lookup(
                    "rmi://"                                                    // NOI18N
                            + siriusRegistryHost
                            + "/nameServer");                                   // NOI18N
            server.addObserver(this);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Info <CS> added as observer: " + this);              // NOI18N
            }
        } catch (final NotBoundException nbe) {
            // TODO: why is there a serr???
            System.err.println("<CS> No SiriusRegistry bound on RMIRegistry at " + siriusRegistryHost); // NOI18N
            LOG.error("<CS> No SiriusRegistry bound on RMIRegistry at " + siriusRegistryHost, nbe);     // NOI18N
        } catch (final RemoteException re) {
            // TODO: why is there a serr???
            System.err.println("<CS> RMIRegistry on " + siriusRegistryHost + " could not be contacted"); // NOI18N
            LOG.error("<CS> RMIRegistry on " + siriusRegistryHost + " could not be contacted", re);      // NOI18N
        } catch (final Exception e) {
            LOG.error(e, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  siriusRegistryHost  DOCUMENT ME!
     */
    void unregisterAsObserver(final String siriusRegistryHost) {
        try {
            final RemoteObservable server = (RemoteObservable)Naming.lookup(
                    "rmi://" // NOI18N
                            + siriusRegistryHost
                            + "/nameServer"); // NOI18N
            server.deleteObserver(this);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Info <CS> removed as observer: " + this); // NOI18N
            }
        } catch (Exception e) {
            LOG.error("could not unregister as observer: " + this, e); // NOI18N
        }
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
    @Override
    public boolean delete(final int id, final String domain) throws RemoteException {
        return queryStore.delete(id, domain);
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
    @Override
    public QueryData getQuery(final int id, final String domain) throws RemoteException {
        return queryStore.getQuery(id, domain);
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
    @Override
    public Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        return queryStore.getQueryInfos(userGroup);
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
    @Override
    public Info[] getQueryInfos(final User user) throws RemoteException {
        return queryStore.getQueryInfos(user);
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
    @Override
    public boolean storeQuery(final User user, final QueryData data) throws RemoteException {
        return queryStore.storeQuery(user, data);
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
    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        return searchService.getSearchOptions(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        return searchService.getSearchOptions(user, domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   classIds       DOCUMENT ME!
     * @param   searchOptions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public SearchResult search(final User user, final String[] classIds, final SearchOption[] searchOptions)
            throws RemoteException {
        return searchService.search(user, classIds, searchOptions);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     * @param   resultType   DOCUMENT ME!
     * @param   isUpdate     DOCUMENT ME!
     * @param   isBatch      DOCUMENT ME!
     * @param   isRoot       DOCUMENT ME!
     * @param   isUnion      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int addQuery(
            final User user,
            final String name,
            final String description,
            final String statement,
            final int resultType,
            final char isUpdate,
            final char isBatch,
            final char isRoot,
            final char isUnion) throws RemoteException {
        return searchService.addQuery(
                user,
                name,
                description,
                statement,
                resultType,
                isUpdate,
                isBatch,
                isRoot,
                isUnion);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int addQuery(final User user, final String name, final String description, final String statement)
            throws RemoteException {
        return searchService.addQuery(user, name, description, statement);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   queryId        DOCUMENT ME!
     * @param   typeId         DOCUMENT ME!
     * @param   paramkey       DOCUMENT ME!
     * @param   description    DOCUMENT ME!
     * @param   isQueryResult  DOCUMENT ME!
     * @param   queryPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addQueryParameter(
            final User user,
            final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        return searchService.addQueryParameter(
                user,
                queryId,
                typeId,
                paramkey,
                description,
                isQueryResult,
                queryPosition);
    }

    /**
     * position set in order of the addition.
     *
     * @param   user         DOCUMENT ME!
     * @param   queryId      DOCUMENT ME!
     * @param   paramkey     DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addQueryParameter(
            final User user,
            final int queryId,
            final String paramkey,
            final String description) throws RemoteException {
        return searchService.addQueryParameter(user, queryId, paramkey, description);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   c     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return metaService.getInstance(user, c);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(classId, user, representationFields);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
    }

    @Override
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        return userService.getConfigAttr(user, key);
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return getConfigAttr(user, key) != null;
    }

    @Override
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        return searchService.customServerSearch(user, serverSearch);
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        return metaService.getHistory(classId, objectId, domain, user, elements);
    }
}
