/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.MetaClassCache;
import Sirius.server.Server;
import Sirius.server.ServerType;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.interfaces.domainserver.InfoService;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserServer;
import Sirius.server.observ.RemoteObservable;
import Sirius.server.observ.RemoteObserver;
import Sirius.server.property.ServerProperties;

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
import java.util.Hashtable;
import java.util.Vector;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.search.CidsServerSearch;

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
    private final transient ActionServiceImpl actionService;

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
                if (localServer instanceof InfoService) {
                    final InfoService is = (InfoService)localServer;
                    MetaClassCache.getInstance().setAllClasses(is.getAllClassInformation(), localServers[i].getName());
                    System.out.println(localServers[i].getName()
                                + " added to the MetaClassCache [by already existing servers]");
                }
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
            actionService = new ActionServiceImpl(activeLocalServers, nameServer);
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

    @Deprecated
    @Override
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        return getChildren(node, usr, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   usr      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getChildren(final Node node, final User usr, final ConnectionContext context) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren for: " + node); // NOI18N
        }
        return catService.getChildren(node, usr, context);
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user, final String localServerName) throws RemoteException {
        return getRoots(user, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        return catService.getRoots(user, localServerName, context);
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user) throws RemoteException {
        return getRoots(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final ConnectionContext context) throws RemoteException {
        return catService.getRoots(user, context);
    }

    @Override
    @Deprecated
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return addNode(node, parent, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   parent   DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node addNode(final Node node, final Link parent, final User user, final ConnectionContext context)
            throws RemoteException {
        return catService.addNode(node, parent, user, context);
    }

    @Deprecated
    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return deleteNode(node, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user, final ConnectionContext context)
            throws RemoteException {
        return catService.deleteNode(node, user, context);
    }

    @Deprecated
    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return addLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        return catService.addLink(from, to, user, context);
    }

    @Deprecated
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return deleteLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        return catService.deleteLink(from, to, user, context);
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user, final String localServerName) throws RemoteException {
        return getClassTreeNodes(user, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        return metaService.getClassTreeNodes(user, localServerName, context);
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        return getClassTreeNodes(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final ConnectionContext context) throws RemoteException {
        return metaService.getClassTreeNodes(user, context);
    }

    @Override
    @Deprecated
    public MetaClass getClass(final User user, final int classID, final String localServerName) throws RemoteException {
        return getClass(user, classID, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   classID          DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClass(final User user,
            final int classID,
            final String localServerName,
            final ConnectionContext context) throws RemoteException {
        return metaService.getClass(user, classID, localServerName, context);
    }

    @Override
    @Deprecated
    public MetaClass getClassByTableName(final User user, final String tableName, final String localServerName)
            throws RemoteException {
        return getClassByTableName(user, tableName, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   tableName        DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClassByTableName(final User user,
            final String tableName,
            final String localServerName,
            final ConnectionContext context) throws RemoteException {
        return metaService.getClassByTableName(user, tableName, localServerName, context);
    }

    @Override
    @Deprecated
    public MetaClass[] getClasses(final User user, final String localServerName) throws RemoteException {
        return getClasses(user, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getClasses(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        return metaService.getClasses(user, localServerName, context);
    }

    @Override
    @Deprecated
    public String[] getDomains() throws RemoteException {
        return getDomains(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String[] getDomains(final ConnectionContext context) throws RemoteException {
        return metaService.getDomains(context);
    }

    @Override
    @Deprecated
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        return getMetaObjectNode(usr, nodeID, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   nodeID   DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User usr,
            final int nodeID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.getMetaObjectNode(usr, nodeID, domain, context);
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
    @Deprecated
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        return getMetaObject(usr, objectID, classID, domain, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject getMetaObject(final User usr,
            final int objectID,
            final int classID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.getMetaObject(usr, objectID, classID, domain, context);
    }

    @Override
    @Deprecated
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(usr, query, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   query    DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        return metaService.getMetaObjectNode(usr, query, context);
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
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return getMetaObject(usr, query, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        return metaService.getMetaObject(usr, query, context);
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query, final String domain) throws RemoteException {
        return getMetaObject(usr, query, domain, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User usr,
            final String query,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.getMetaObject(usr, query, domain, context);
    }

    @Override
    @Deprecated
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return deleteMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int deleteMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.deleteMetaObject(user, metaObject, domain, context);
    }

    @Override
    @Deprecated
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return insertMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject insertMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.insertMetaObject(user, metaObject, domain, context);
    }

    @Override
    @Deprecated
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return updateMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int updateMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        return metaService.updateMetaObject(user, metaObject, domain, context);
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user) throws RemoteException {
        return getMethods(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final ConnectionContext context) throws RemoteException {
        return metaService.getMethods(user, context);
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user, final String lsName) throws RemoteException {
        return getMethods(user, lsName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   lsName   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final String lsName, final ConnectionContext context)
            throws RemoteException {
        return metaService.getMethods(user, lsName, context);
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

    @Deprecated
    @Override
    public User getUser(
            final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        return getUser(
                userGroupLsName,
                userGroupName,
                userLsName,
                userName,
                password,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
     * @param   context          DOCUMENT ME!
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
            final String password,
            final ConnectionContext context) throws RemoteException, UserException {
        LOGINLOG.info("Login: " + userName + "@" + userGroupName + "@" + userGroupLsName);
        return userService.getUser(
                userGroupLsName,
                userGroupName,
                userLsName,
                userName,
                password,
                context);
    }

    @Override
    @Deprecated
    public Vector getUserGroupNames() throws RemoteException {
        return getUserGroupNames(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final ConnectionContext context) throws RemoteException {
        return userService.getUserGroupNames(context);
    }

    @Override
    @Deprecated
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        return getUserGroupNames(userName, lsHome, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     * @param   context   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome, final ConnectionContext context)
            throws RemoteException {
        return userService.getUserGroupNames(userName, lsHome, context);
    }

    @Deprecated
    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        return changePassword(user, oldPassword, newPassword, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     * @param   context      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException {
        return userService.changePassword(user, oldPassword, newPassword, context);
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

    @Override
    @Deprecated
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return getInstance(user, c, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   c        DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass c, final ConnectionContext context)
            throws RemoteException {
        return metaService.getInstance(user, c, context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     * @param   context                DOCUMENT ME!
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
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   context               DOCUMENT ME!
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
            final ConnectionContext context) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(classId, user, representationFields, context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     * @param   context                DOCUMENT ME!
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
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                context);
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   context               DOCUMENT ME!
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
            final ConnectionContext context) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(classId, user, query, representationFields, context);
    }

    @Deprecated
    @Override
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        return getConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public String getConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        return userService.getConfigAttr(user, key, context);
    }

    @Deprecated
    @Override
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return hasConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        return getConfigAttr(user, key, context) != null;
    }

    @Override
    @Deprecated
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        return customServerSearch(user, serverSearch, ConnectionContext.createDeprecated());
    }

    @Override
    public Collection customServerSearch(final User user,
            final CidsServerSearch serverSearch,
            final ConnectionContext context) throws RemoteException {
        return searchService.customServerSearch(user, serverSearch, context);
    }

    @Override
    @Deprecated
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        return getHistory(classId, objectId, domain, user, elements, ConnectionContext.createDeprecated());
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements,
            final ConnectionContext context) throws RemoteException {
        return metaService.getHistory(classId, objectId, domain, user, elements, context);
    }

    @Override
    @Deprecated
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return executeTask(user, taskname, taskdomain, ConnectionContext.createDeprecated(), body, params);
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final ConnectionContext context,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return actionService.executeTask(user, taskname, taskdomain, context, body, params);
    }
}
