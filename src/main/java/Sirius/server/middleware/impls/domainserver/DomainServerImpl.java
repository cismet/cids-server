/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.MetaClassCache;
import Sirius.server.Server;
import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.ServerType;
import Sirius.server.Shutdown;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.history.HistoryException;
import Sirius.server.localserver.history.HistoryServer;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.object.CustomDeletionProvider;
import Sirius.server.localserver.query.querystore.Store;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.user.UserStore;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.interfaces.domainserver.*;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserServer;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.PreparableStatement;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.openide.util.Lookup;

import java.io.File;

import java.net.InetAddress;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.Properties;

import de.cismet.cids.objectextension.ObjectExtensionFactory;

import de.cismet.cids.server.DefaultServerExceptionHandler;
import de.cismet.cids.server.ServerSecurityManager;
import de.cismet.cids.server.actions.ScheduledServerAction;
import de.cismet.cids.server.actions.ScheduledServerActionManager;
import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.connectioncontext.ConnectionContextBackend;
import de.cismet.cids.server.connectioncontext.ConnectionContextLog;
import de.cismet.cids.server.search.QueryPostProcessor;
import de.cismet.cids.server.ws.rest.RESTfulService;

import de.cismet.cids.utils.ClassloadingHelper;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.cidsx.server.actions.RestApiCidsServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DomainServerImpl extends UnicastRemoteObject implements CatalogueService,
    MetaService,
    SystemService,
    UserService,
    ActionService,
    InfoService { // ActionListener

    //~ Static fields/initializers ---------------------------------------------

    private static transient DomainServerImpl instance;
    public static final String SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX = "csa://";
    // this servers configuration
    protected static ServerProperties properties;

    //~ Instance fields --------------------------------------------------------

    // dbaccess of the mis (catalogue, classes and objects
    protected DBServer dbServer;
    // userservice of a localserver
    protected UserStore userstore;
    // history server of a localserver
    protected HistoryServer historyServer;
    // executing the searchservice
    // for storing and loading prdefinded queries
    protected Store queryStore;
    // references to the Registry
    protected NameServer nameServer;
    protected UserServer userServer;
    // this severs info object
    protected Server serverInfo;
    private HashMap<String, ServerAction> serverActionMap = new HashMap<>();
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private final ScheduledServerActionManager scheduledManager;

    //~ Constructors -----------------------------------------------------------

    /**
     * protected ServerStatus status;
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  RemoteException  DOCUMENT ME!
     */
    public DomainServerImpl(final ServerProperties properties) throws Throwable {
        // export object
        super(properties.getServerPort());

        try {
            this.properties = properties;
            final String fileName;
            if (((fileName = properties.getLog4jPropertyFile()) != null) && !fileName.equals("")) { // NOI18N
                PropertyConfigurator.configureAndWatch(fileName, 10000);
            }

            serverInfo = new Server(
                    ServerType.LOCALSERVER,
                    properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(),
                    properties.getRMIRegistryPort(),
                    String.valueOf(properties.getServerPort()));

            dbServer = new DBServer(properties);

            userstore = dbServer.getUserStore();

            queryStore = new Store(dbServer.getActiveDBConnection().getConnection(), properties);

            System.out.println("\n<LS> DBConnection: " + dbServer.getActiveDBConnection().getURL() + "\n"); // NOI18N

            System.out.println(serverInfo.getRMIAddress());
            logger.info(serverInfo.getRMIAddress());
            System.out.println("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString()); // NOI18N
            logger.info("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());        // NOI18N
            Naming.bind(serverInfo.getBindString(), this);

            // status = new ServerStatus();
            register();

            if (logger.isDebugEnabled()) {
                logger.debug("Server Referenz " + this); // NOI18N
            }

            historyServer = dbServer.getHistoryServer();

            try {
                ServerResourcesLoader.getInstance().setResourcesBasePath(properties.getServerResourcesBasePath());
            } catch (final Exception ex) {
                logger.warn(
                    "ServerResourcePath could not be determined. CachedServerResourcesLoader will not work as expected !",
                    ex);
            }

            if (ConnectionContextBackend.getInstance().isEnabled()) {
                try {
                    ConnectionContextBackend.getInstance().loadConfig(properties.getConnectionContextConfig());
                } catch (final Exception ex) {
                    logger.warn(
                        "RuleSets for ConnectionContext loggers couldn't be loaded. ConnectionContextBackend will not work as expected !",
                        ex);
                }
            }

            final Collection<? extends RestApiCidsServerAction> restServerActions = Lookup.getDefault()
                        .lookupAll(RestApiCidsServerAction.class);
            final Collection<? extends ServerAction> serverActions = Lookup.getDefault().lookupAll(ServerAction.class);

            for (final ServerAction restServerAction : restServerActions) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding REST API Server Action '" + restServerAction.getTaskName() + "'");
                }
                serverActionMap.put(restServerAction.getTaskName(), restServerAction);
            }

            for (final ServerAction serverAction : serverActions) {
                if (!serverActionMap.containsKey(serverAction.getTaskName())) {
                    logger.warn("Adding LEGACY Server Action '" + serverAction.getTaskName()
                                + "' -> Change to RestApiCidsServerAction");
                    serverActionMap.put(serverAction.getTaskName(), serverAction);
                }
            }

            MetaClassCache.getInstance().setAllClasses(dbServer.getClasses(), properties.getServerName());

            if (ScheduledServerActionManager.isScheduledServerActionFeatureSupported(
                            dbServer.getActiveDBConnection())) {
                scheduledManager = new ScheduledServerActionManager(
                        this,
                        dbServer.getActiveDBConnection(),
                        userServer,
                        serverInfo.getName());
                scheduledManager.resumeAll();
            } else {
                logger.info("scheduled server action feature is not supported by this server instance"); // NOI18N
                scheduledManager = null;
            }

            final Collection<? extends DomainServerStartupHook> startupHooks = Lookup.getDefault()
                        .lookupAll(DomainServerStartupHook.class);
            for (final DomainServerStartupHook hook : startupHooks) {
                if (hook.getDomain() != null) {
                    if (hook.getDomain().equalsIgnoreCase(properties.getServerName())
                                || hook.getDomain().equalsIgnoreCase(
                                    DomainServerStartupHook.START_ON_DOMAIN.ANY.toString())) {
                        if (hook instanceof ConnectionContextStore) {
                            final ConnectionContext connectionContext = ConnectionContext.create(
                                    ConnectionContext.Category.STARTUP,
                                    hook.getClass().getSimpleName());
                            ((ConnectionContextStore)hook).initWithConnectionContext(connectionContext);
                        }
                        hook.domainServerStarted();
                    }
                }
            }
            // initFrame();
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerProperties getServerProperties() {
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getAllClassInformation() throws RemoteException {
        try {
            return dbServer.getClasses();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error("Error in getAllClassInformation()", e); // NOI18N
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public NodeReferenceList getChildren(final Node node, final User user) throws RemoteException {
        return getChildren(node, user, ConnectionContext.createDeprecated());
    }

    @Override
    public NodeReferenceList getChildren(final Node node, final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "getChildren",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("node", node);
                                    }
                                })));
        }
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getChildren(node, user);
            }

            return new NodeReferenceList();                // no permission
        } catch (Throwable e) {
            if (logger != null) {
                logger.error("Error in getChildren()", e); // NOI18N
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------------------

    @Override
    @Deprecated
    public NodeReferenceList getRoots(final User user) throws RemoteException {
        return getRoots(user, ConnectionContext.createDeprecated());
    }

    @Override
    public NodeReferenceList getRoots(final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(connectionContext, user, "getRoots"));
        }
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getTops(user);
            }

            return new NodeReferenceList(); // no permission => empty list
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            return new NodeReferenceList();
                // throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   taskname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerAction getServerActionByTaskname(final String taskname) {
        return serverActionMap.get(taskname);
    }

    @Override
    @Deprecated
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return addNode(node, parent, user, ConnectionContext.createDeprecated());
    }

    @Override
    public Node addNode(final Node node, final Link parent, final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "addNode",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("node", node);
                                        put("parent", parent);
                                    }
                                })));
        }
        try {
            return dbServer.getTree().addNode(node, parent, user);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return deleteNode(node, user, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean deleteNode(final Node node, final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "deleteNode",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("node", node);
                                    }
                                })));
        }
        try {
            return dbServer.getTree().deleteNode(node, user);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return addLink(from, to, user, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean addLink(final Node from, final Node to, final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "addLink",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("from", from);
                                        put("to", to);
                                    }
                                })));
        }
        try {
            return dbServer.getTree().addLink(from, to, user);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return deleteLink(from, to, user, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean deleteLink(final Node from,
            final Node to,
            final User user,
            final ConnectionContext connectionContext) throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "deleteLink",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("from", from);
                                        put("to", to);
                                    }
                                })));
        }
        try {
            return dbServer.getTree().deleteLink(from, to, user);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public Node[] getNodes(final User user, final int[] ids) throws RemoteException {
        return getNodes(user, ids, ConnectionContext.createDeprecated());
    }

    @Override
    public Node[] getNodes(final User user, final int[] ids, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "getNodes",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("ids", ids);
                                    }
                                })));
        }
        try {
            return dbServer.getNodes(ids, user);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public NodeReferenceList getClassTreeNodes(final User user) throws RemoteException {
        return getClassTreeNodes(user, ConnectionContext.createDeprecated());
    }

    @Override
    public NodeReferenceList getClassTreeNodes(final User user, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(connectionContext, user, "getClassTreeNodes"));
        }
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getClassTreeNodes(user);
            }

            return new NodeReferenceList(); // no permission empty list
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public MetaClass[] getClasses(final User user) throws RemoteException {
        return getClasses(user, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaClass[] getClasses(final User user, final ConnectionContext connectionContext) throws RemoteException {
        try { // if(userstore.validateUser(user))
            final MetaClass[] metaClasses = dbServer.getClasses(user);
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaClasses(
                                metaClasses,
                                connectionContext,
                                user,
                                "getClasses",
                                null));
            }
            return metaClasses;

            // return new MetaClass[0];
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public MetaClass getClass(final User user, final int classID) throws RemoteException {
        return getClass(user, classID, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaClass getClass(final User user, final int classID, final ConnectionContext connectionContext)
            throws RemoteException {
        try { // if(userstore.validateUser(user))
            final MetaClass metaClass = dbServer.getClass(user, classID);
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaClass(
                                metaClass,
                                connectionContext,
                                user,
                                "getClass",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classID", classID);
                                        }
                                    })));
            }
            return metaClass;

            // return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public MetaClass getClassByTableName(final User user, final String tableName) throws RemoteException {
        return getClassByTableName(user, tableName, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaClass getClassByTableName(final User user,
            final String tableName,
            final ConnectionContext connectionContext) throws RemoteException {
        try { // if(userstore.validateUser(user))
            final MetaClass metaClass = dbServer.getClassByTableName(user, tableName);
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaClass(
                                metaClass,
                                connectionContext,
                                user,
                                "getClassByTableName",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("tableName", tableName);
                                        }
                                    })));
            }
            return metaClass;
                // return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   objectIDs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    public MetaObject[] getObjects(final User user, final String[] objectIDs) throws RemoteException {
        return getObjects(user, objectIDs, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user               DOCUMENT ME!
     * @param   objectIDs          DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject[] getObjects(final User user, final String[] objectIDs, final ConnectionContext connectionContext)
            throws RemoteException {
        try {
            final MetaObject[] mos = dbServer.getObjects(objectIDs, user);
            for (final MetaObject mo : mos) {
                if (mo instanceof ConnectionContextStore) {
                    ((ConnectionContextStore)mo).initWithConnectionContext(connectionContext);
                }
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObjects(
                                mos,
                                connectionContext,
                                user,
                                "getObjects",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("objectIDs", objectIDs);
                                        }
                                    })));
            }
            return mos;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    public MetaObject getObject(final User user, final String objectID) throws RemoteException {
        return getObject(user, objectID, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user               DOCUMENT ME!
     * @param   objectID           DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject getObject(final User user, final String objectID, final ConnectionContext connectionContext)
            throws RemoteException {
        try {
            final MetaObject mo = dbServer.getObject(objectID, user);
            if (mo != null) {
                if (mo instanceof ConnectionContextStore) {
                    ((ConnectionContextStore)mo).initWithConnectionContext(connectionContext);
                }
                final MetaClass[] classes = dbServer.getClasses(user);

                mo.setAllClasses(getClassHashTable(classes, serverInfo.getName()));

                // Check if Object can be extended
                if (mo.getMetaClass().hasExtensionAttributes()) {
                    // TODO:Check if there is a ExtensionFactory
                    final Class<?> extensionFactoryClass = ClassloadingHelper.getDynamicClass(mo.getMetaClass(),
                            ClassloadingHelper.CLASS_TYPE.EXTENSION_FACTORY);

                    if (extensionFactoryClass != null) {
                        final ObjectExtensionFactory ef = (ObjectExtensionFactory)extensionFactoryClass.newInstance();
                        ef.setDomainServer(this);
                        ef.setUser(user);
                        try {
                            ef.extend(mo.getBean());
                        } catch (Exception e) {
                            logger.error("Error during ObjectExtension", e); // NOI18N
                        }
                    }
                }
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObject(
                                mo,
                                connectionContext,
                                user,
                                "getObject",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("objectID", objectID);
                                        }
                                    })));
            }
            return mo;
        } catch (final Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes     DOCUMENT ME!
     * @param   serverName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static HashMap getClassHashTable(final MetaClass[] classes, final String serverName) {
        final HashMap classHash = new HashMap();
        for (int i = 0; i < classes.length; i++) {
            final String key = serverName + classes[i].getID();
            if (!classHash.containsKey(key)) {
                classHash.put(key, classes[i]);
            }
        }

        return classHash;
    }

    @Override
    @Deprecated
    public Node getMetaObjectNode(final User usr, final int nodeID) throws RemoteException {
        return getMetaObjectNode(usr, nodeID, ConnectionContext.createDeprecated());
    }

    // retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
    @Override
    public Node getMetaObjectNode(final User usr, final int nodeID, final ConnectionContext connectionContext)
            throws RemoteException {
        final int[] tmp = { nodeID };

        // single value directly referenced
        return getNodes(usr, tmp, connectionContext)[0];
    }

    @Override
    @Deprecated
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID) throws RemoteException {
        return getMetaObject(usr, objectID, classID, ConnectionContext.createDeprecated());
    }

    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    // MetaObject ersetzt DefaultObject
    @Override
    public MetaObject getMetaObject(final User usr,
            final int objectID,
            final int classID,
            final ConnectionContext connectionContext) throws RemoteException {
        return getObject(usr, objectID + "@" + classID, connectionContext); // NOI18N
    }
// Query not yet defined but will be MetaSQL

    @Override
    @Deprecated
    public MetaObjectNode[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(usr, query, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObjectNode[] getMetaObjectNode(final User usr,
            final String query,
            final ConnectionContext connectionContext) throws RemoteException {
        final String domain = usr.getDomain();
        final ArrayList<ArrayList> result = this.performCustomSearch(query, connectionContext);
        final MetaObjectNode[] ret = new MetaObjectNode[result.size()];
        int i = 0;
        for (final ArrayList row : result) {
            // #177 getMetaObjectNode Integer Cast
            ret[i] = new MetaObjectNode(domain,
                    ((Number)row.get(1)).intValue(),
                    ((Number)row.get(0)).intValue());
            i++;
        }
        return ret;
//        return getMetaObjectNode(
//                usr,
//                new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, query), usr.getDomain())); // NOI18N
            // throw new UnsupportedOperationException("not implemented ");
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return getMetaObject(usr, query, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User user, final String query, final ConnectionContext connectionContext)
            throws RemoteException {
        final MetaObjectNode[] nodes = (MetaObjectNode[])(getMetaObjectNode(user, query, connectionContext));
        final MetaObject[] mos = new MetaObject[nodes.length];
        int i = 0;
        for (final MetaObjectNode n : nodes) {
            mos[i] = getMetaObject(user, n.getObjectId(), n.getClassId(), connectionContext);
            i++;
        }
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForMetaObjects(
                            mos,
                            connectionContext,
                            user,
                            "getMetaObject",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("query", query);
                                    }
                                })));
        }
        return mos;
//        final MetaObject[] o = getMetaObject(
//                usr,
//                new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain())); // NOI18N
//
//        return o;

        // throw new UnsupportedOperationException("not implemented ");
    }

    @Override
    @Deprecated
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        return insertMetaObject(user, metaObject, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject insertMetaObject(final User user,
            final MetaObject metaObject,
            final ConnectionContext connectionContext) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<html>insert MetaObject for User :+:"
                            + user
                            + "  MO "
                            + metaObject.getDebugString()
                            + "</html>");
            }
        }
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForMetaObject(
                            metaObject,
                            connectionContext,
                            user,
                            "insertMetaObject",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("metaObject", metaObject);
                                    }
                                })));
        }
        try {
            final int key = dbServer.getObjectPersitenceManager().insertMetaObject(user, metaObject);

            return this.getMetaObject(user, key, metaObject.getClassID(), connectionContext);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public int updateMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        return updateMetaObject(user, metaObject, ConnectionContext.createDeprecated());
    }

    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject, final ConnectionContext connectionContext)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<html><body>update called for :+: <p>" + metaObject.getDebugString() + "</p></body></html>"); // NOI18N
        }

        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForMetaObject(
                            metaObject,
                            connectionContext,
                            user,
                            "updateMetaObject",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("metaObject", metaObject);
                                    }
                                })));
        }
        try {
            dbServer.getObjectPersitenceManager().updateMetaObject(user, metaObject);

            return 1;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public int deleteMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        return deleteMetaObject(user, metaObject, ConnectionContext.createDeprecated());
    }

    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject, final ConnectionContext connectionContext)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete called for" + metaObject); // NOI18N
        }

        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForMetaObject(
                            metaObject,
                            connectionContext,
                            user,
                            "deleteMetaObject",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("metaObject", metaObject);
                                    }
                                })));
        }

        try {
            final Collection<CustomDeletionProvider> matchingCustomDeletionProviders = new ArrayList<>();
            for (final CustomDeletionProvider customDeletionProvider
                        : (Collection<CustomDeletionProvider>)Lookup.getDefault().lookupAll(
                            CustomDeletionProvider.class)) {
                try {
                    if (customDeletionProvider != null) {
                        if (customDeletionProvider instanceof ConnectionContextStore) {
                            ((ConnectionContextStore)customDeletionProvider).initWithConnectionContext(
                                connectionContext);
                        }
                        if (customDeletionProvider instanceof MetaServiceStore) {
                            ((MetaServiceStore)customDeletionProvider).setMetaService(this);
                        }
                        if (customDeletionProvider.isMatching(user, metaObject)) {
                            matchingCustomDeletionProviders.add(customDeletionProvider);
                        }
                    }
                } catch (final Exception ex) {
                    logger.error("error while initializing customDeletionProvider", ex);
                }
            }
            if (!matchingCustomDeletionProviders.isEmpty()) {
                if (matchingCustomDeletionProviders.size() > 1) {
                    logger.warn("Multiple customDeletionProviders are matching. Executing them all now.");
                }
                for (final CustomDeletionProvider customDeletionProvider : matchingCustomDeletionProviders) {
                    try {
                        customDeletionProvider.customDeleteMetaObject(user, metaObject);
                    } catch (final Exception ex) {
                        throw new RemoteException("Error while custom-deletion", ex);
                    }
                }
                return 0;
            }
            return dbServer.getObjectPersitenceManager().deleteMetaObject(user, metaObject);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return getInstance(user, c, ConnectionContext.createDeprecated());
    }

    // creates an Instance of a MetaObject with all attribute values set to default
    @Override
    public MetaObject getInstance(final User user, final MetaClass metaClass, final ConnectionContext connectionContext)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("usergetInstance :: " + user + "  class " + metaClass); // NOI18N
        }
        try {
            final Sirius.server.localserver.object.Object o = dbServer.getObjectFactory()
                        .getInstance(metaClass.getID());
            if (o != null) {
                final DefaultMetaObject mo = new DefaultMetaObject(o, metaClass.getDomain());
                mo.initWithConnectionContext(connectionContext);
                mo.setAllStatus(MetaObject.TEMPLATE);
                if (ConnectionContextBackend.getInstance().isEnabled()) {
                    ConnectionContextBackend.getInstance()
                            .log(ConnectionContextLog.createForMetaObject(
                                    mo,
                                    connectionContext,
                                    user,
                                    "getInstance",
                                    Collections.unmodifiableMap(new HashMap<String, Object>() {

                                            {
                                                put("metaClass", metaClass);
                                            }
                                        })));
                }
                return mo;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException("<LS> ", e); // NOI18N
        }
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user) throws RemoteException {
        return getMethods(user, ConnectionContext.createDeprecated());
    }

    @Override
    public MethodMap getMethods(final User user, final ConnectionContext connectionContext) throws RemoteException {
        // if(userstore.validateUser(user))
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(connectionContext,
                            user,
                            "getMethods"));
        }

        return dbServer.getMethods(); // dbServer.getMethods(user.getuserGroup()); // instead

        // return new MethodMap();
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
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

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            final LightweightMetaObject[] lwmos = dbServer.getAllLightweightMetaObjectsForClass(
                    classId,
                    user,
                    representationFields,
                    representationPattern);
            for (final LightweightMetaObject lwmo : lwmos) {
                lwmo.initWithConnectionContext(connectionContext);
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObjects(
                                lwmos,
                                connectionContext,
                                user,
                                "getAllLightweightMetaObjectsForClass",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classId", classId);
                                            put("representationFields", representationFields);
                                            put("representationPattern", representationPattern);
                                        }
                                    })));
            }
            return lwmos;
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex); // NOI18N
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                ConnectionContext.createDeprecated());
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            final LightweightMetaObject[] lwmos = dbServer.getAllLightweightMetaObjectsForClass(
                    classId,
                    user,
                    representationFields);
            for (final LightweightMetaObject lwmo : lwmos) {
                lwmo.initWithConnectionContext(connectionContext);
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObjects(
                                lwmos,
                                connectionContext,
                                user,
                                "getAllLightweightMetaObjectsForClass",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classId", classId);
                                            put("representationFields", representationFields);
                                        }
                                    })));
            }
            return lwmos;
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex); // NOI18N
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
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

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            final LightweightMetaObject[] lwmos = dbServer.getLightweightMetaObjectsByQuery(
                    classId,
                    user,
                    query,
                    representationFields,
                    representationPattern);
            for (final LightweightMetaObject lwmo : lwmos) {
                lwmo.initWithConnectionContext(connectionContext);
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObjects(
                                lwmos,
                                connectionContext,
                                user,
                                "getLightweightMetaObjectsByQuery",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classId", classId);
                                            put("query", query);
                                            put("representationFields", representationFields);
                                            put("representationPattern", representationPattern);
                                        }
                                    })));
            }
            return lwmos;
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex); // NOI18N
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
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

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            final LightweightMetaObject[] lwmos = dbServer.getLightweightMetaObjectsByQuery(
                    classId,
                    user,
                    query,
                    representationFields);
            for (final LightweightMetaObject lwmo : lwmos) {
                lwmo.initWithConnectionContext(connectionContext);
            }
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.createForMetaObjects(
                                lwmos,
                                connectionContext,
                                user,
                                "getLightweightMetaObjectsByQuery",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classId", classId);
                                            put("query", query);
                                            put("representationFields", representationFields);
                                        }
                                    })));
            }
            return lwmos;
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex); // NOI18N
        }
    }

    @Override
    public Sirius.util.image.Image[] getDefaultIcons() throws RemoteException {
        return properties.getDefaultIcons();
    }

    @Override
    @Deprecated
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException {
        return changePassword(user, oldPassword, newPassword, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext connectionContext) throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "changePassword",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("oldPassword", "***censored***");
                                        put("newPassword", "***censored***");
                                    }
                                })));
        }
        try {
            return userstore.changePassword(user, oldPassword, newPassword);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("changePassword at remotedbserverimpl", e); // NOI18N
        }
    }

    @Override
    @Deprecated
    public boolean validateUser(final User user, final String password) throws RemoteException {
        return validateUser(user, password, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean validateUser(final User user, final String password, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            user,
                            "validateUser",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("password", "***censored***");
                                    }
                                })));
        }
        try {
            return userstore.validateUserPassword(user, password);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("Exception validateUser at remotedbserverimpl", e); // NOI18N
        }
    }

    @Override
    @Deprecated
    public ArrayList<ArrayList> performCustomSearch(final String query) throws RemoteException {
        return performCustomSearch(query, ConnectionContext.createDeprecated());
    }

    @Override
    public ArrayList<ArrayList> performCustomSearch(final String query, final ConnectionContext connectionContext)
            throws RemoteException {
        return performCustomSearch(query, null, connectionContext);
    }

    @Override
    @Deprecated
    public ArrayList<ArrayList> performCustomSearch(final String query, final QueryPostProcessor qpp)
            throws RemoteException {
        return performCustomSearch(query, qpp, ConnectionContext.createDeprecated());
    }

    @Override
    public ArrayList<ArrayList> performCustomSearch(final String query,
            final QueryPostProcessor qpp,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            final Statement s = getConnectionPool().getDBConnection().getConnection().createStatement();
            final ResultSet rs = s.executeQuery(query);
            final ArrayList<ArrayList> result = collectResults(rs);
            if (qpp != null) {
                return qpp.postProcess(result);
            } else {
                return result;
            }
        } catch (Exception e) {
            final String msg = "Error during sql statement: " + query;
            logger.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException           DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public ArrayList<ArrayList> collectResults(final ResultSet rs) throws SQLException {
        final ArrayList<ArrayList> result = new ArrayList<ArrayList>();
        while (rs.next()) {
            final ArrayList row = new ArrayList();
            for (int i = 0; i < rs.getMetaData().getColumnCount(); ++i) {
                final Object objectFromResultSet = rs.getObject(i + 1);

                if (objectFromResultSet instanceof Clob) {
                    // we convert the clob to a string, otherwise the value is not serialisable out of the
                    // box due to the direct connection to the database
                    // TODO: handle overflows, i.e. clob too big
                    final Clob clob = (Clob)objectFromResultSet;
                    if (clob.length() <= Integer.valueOf(Integer.MAX_VALUE).longValue()) {
                        row.add(clob.getSubString(1, Long.valueOf(clob.length()).intValue()));
                    } else {
                        throw new IllegalStateException(
                            "cannot handle clobs larger than Integer.MAX_VALUE)");
                    }
                } else {
                    row.add(objectFromResultSet);
                }
            }
            result.add(row);
        }

        return result;
    }

    @Override
    @Deprecated
    public ArrayList<ArrayList> performCustomSearch(final PreparableStatement ps) throws RemoteException {
        return performCustomSearch(ps, ConnectionContext.createDeprecated());
    }

    @Override
    public ArrayList<ArrayList> performCustomSearch(final PreparableStatement ps,
            final ConnectionContext connectionContext) throws RemoteException {
        return performCustomSearch(ps, null, connectionContext);
    }

    @Override
    @Deprecated
    public ArrayList<ArrayList> performCustomSearch(final PreparableStatement ps, final QueryPostProcessor qpp)
            throws RemoteException {
        return performCustomSearch(ps, qpp, ConnectionContext.createDeprecated());
    }

    @Override
    public ArrayList<ArrayList> performCustomSearch(final PreparableStatement ps,
            final QueryPostProcessor qpp,
            final ConnectionContext connectionContext) throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.create(
                            connectionContext,
                            null,
                            "performCustomSearch",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("ps", ps);
                                        put("qpp", qpp);
                                    }
                                })));
        }
        try {
            final PreparedStatement stmt = ps.parameterise(getConnectionPool().getDBConnection().getConnection());
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<ArrayList> result = collectResults(rs);
            if (qpp != null) {
                return qpp.postProcess(result);
            } else {
                return result;
            }
        } catch (final Exception e) {
            final String msg = "Error during sql statement: " + ps;
            logger.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected void register() throws Throwable {
        int registered = 0;

        try {
            final String lsName = serverInfo.getName();
            final String ip = serverInfo.getIP();
            final String[] registryIPs = properties.getRegistryIps();
            final String rmiPort = serverInfo.getRMIPort();

            for (int i = 0; i < registryIPs.length; i++) {
                try {
                    nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + ":" + rmiPort + "/nameServer");
                    userServer = (UserServer)nameServer; // (UserServer)
                    // Naming.lookup("rmi://"+registryIPs[i]+"/userServer");

                    nameServer.registerServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);

                    logger.info(
                        "\n<LS> registered at SiriusRegistry "
                                + registryIPs[i]
                                + " with "
                                + lsName
                                + "  "
                                + ip);

                    final UserStore userStore = dbServer.getUserStore();

                    userServer.registerUsers(userStore.getUsers());
                    userServer.registerUserGroups(userStore.getUserGroups());
                    userServer.registerUserMemberships(userStore.getMemberships());

                    registered++;
                    logger.info(
                        "<LS> users registered at SiriusRegistry"
                                + registryIPs[i]
                                + " with "
                                + lsName
                                + "  "
                                + ip);
                } catch (NotBoundException nbe) {
                    System.err.println("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i]); // NOI18N
                    logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);  // NOI18N
                } catch (RemoteException re) {
                    System.err.println(
                        "<LS> No RMIRegistry on "
                                + registryIPs[i]
                                + ", therefore SiriusRegistry could not be contacted");
                    logger.error(
                        "<LS> No RMIRegistry on "
                                + registryIPs[i]
                                + ", therefore SiriusRegistry could not be contacted",
                        re);
                }
            }
        } catch (Throwable e) {
            logger.error(e, e);
            throw new ServerExitError(e);
        }

        if (registered == 0) {
            throw new ServerExitError("registration failed"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     * @throws  ServerExit       DOCUMENT ME!
     */
    public void shutdown() throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("shutting down domainserver impl: " + this); // NOI18N
        }

        final Shutdown shutdown = Shutdown.createShutdown(this);
        shutdown.shutdown();

        final String ip = serverInfo.getIP();
        final String lsName = properties.getServerName();
        final String[] registryIPs = properties.getRegistryIps();
        final String rmiPort = serverInfo.getRMIPort();

        for (int i = 0; i < registryIPs.length; i++) {
            try {
                nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + ":" + rmiPort + "/nameServer");
                userServer = (UserServer)nameServer;

                // User und UserGroups bei Registry abmelden
                userServer.unregisterUsers(userstore.getUsers());
                userServer.unregisterUserGroups(userstore.getUserGroups());

                // LocalServer bei Registry abmelden
                nameServer.unregisterServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);
            } catch (NotBoundException nbe) {
                logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe); // NOI18N
            } catch (RemoteException re) {
                logger.error("<LS> RMIRegistry on " + registryIPs[i] + "could not be contacted", re);  // NOI18N
            } catch (Throwable e) {
                logger.error(e, e);
            }
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("<LS> unbind for " + serverInfo.getBindString()); // NOI18N
            }
            Naming.unbind(serverInfo.getBindString());

            if (properties.getStartMode().equalsIgnoreCase("simple")) { // NOI18N
                if (logger.isDebugEnabled()) {
                    logger.debug("shutting down restful interface");    // NOI18N
                }
                RESTfulService.down();
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("shutting down startproxy");       // NOI18N
                    }
                    StartProxy.getInstance().shutdown();
                } catch (final ServerExit serverExit) {
                    // skip
                }
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("shutting down registry");         // NOI18N
                    }
                    Registry.getServerInstance(Integer.valueOf(properties.getRMIRegistryPort())).shutdown();
                } catch (final ServerExit serverExit) {
                    // skip
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("shutting down db connections"); // NOI18N
            }

            // alle offenen Verbindungen schliessen
            dbServer.getConnectionPool().closeConnections();

            dbServer = null;

            // userservice of a localserver
            userstore = null;

            // executing the searchservice
            // this servers configuration
            properties = null;

            // for storing and loading prdefinded queries
            queryStore = null;

            System.gc();
        } catch (final Exception t) {
            logger.error("caught exception during shutdown", t);
            throw new ServerExitError(t);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("freeing instance"); // NOI18N
            }

            instance = null;
        }

        throw new ServerExit("Server exited regularly"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DomainServerImpl getServerInstance() {
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DBConnectionPool getConnectionPool() {
        return dbServer.getConnectionPool();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Throwable              DOCUMENT ME!
     * @throws  ServerExitError        DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Throwable {
        // first of all register the default exception handler for all threads
        Thread.setDefaultUncaughtExceptionHandler(new DefaultServerExceptionHandler());

        ServerProperties properties = null;
        int rmiPort;

        if (args == null) {
            throw new ServerExitError("args == null no commandline parameter given (Configfile / port)"); // NOI18N
        } else if (args.length < 1) {
            throw new ServerExitError("insufficient arguments given");                                    // NOI18N
        }

        if (instance != null) {
            throw new IllegalStateException("an instance was already created"); // NOI18N
        }

        try {
            try {
                properties = new ServerProperties(args[0]);
                rmiPort = new Integer(properties.getRMIRegistryPort()).intValue();
            } catch (MissingResourceException mre) {
                System.err.println("Info :: <LS> Key  rmiRegistryPort  in ConfigFile +" + args[0] + " is Missing!"); // NOI18N
                System.err.println("Info :: <LS> Set Default to 1099");                                              // NOI18N
                rmiPort = 1099;
            }

            try {
                initLog4J(properties);
            } catch (final Exception e) {
                System.err.println("WARN :: <LS> Could not init log4j_: " + e); // NOI18N
            }

            System.out.println("<LS> ConfigFile: " + args[0]); // NOI18N

            try {
                LocateRegistry.createRegistry(rmiPort);
            } catch (final Exception e) {
                System.out.println("could not create rmi registry. it probably already has been created on port: "
                            + rmiPort);
                LocateRegistry.getRegistry(rmiPort);
            }

            if (properties.getStartMode().equalsIgnoreCase("simple")) { // NOI18N
                Sirius.server.registry.Registry.getServerInstance(rmiPort);
                StartProxy.getInstance(args[0]);
            }

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new ServerSecurityManager());
            }

            instance = new DomainServerImpl(new ServerProperties(args[0]));

            System.out.println("Info :: <LS>  !!!LocalSERVER started!!!!");               // NOI18N
        } catch (Exception e) {
            System.err.println("Error while starting domainserver :: " + e.getMessage()); // NOI18N
            e.printStackTrace();
            if (instance != null) {
                instance.shutdown();
            }
            throw new ServerExitError(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private static void initLog4J(final ServerProperties properties) {
        final File log4jPropFile = new File(properties.getLog4jPropertyFile());
        if ((log4jPropFile == null) || !log4jPropFile.isFile() || !log4jPropFile.canRead()) {
            throw new IllegalArgumentException("serverproperties provided invalid log4j config file: " + log4jPropFile); // NOI18N
        } else {
            PropertyConfigurator.configureAndWatch(log4jPropFile.getAbsolutePath(), 10000);
        }
    }

    @Override
    @Deprecated
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        return getConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public String getConfigAttr(final User user, final String key, final ConnectionContext connectionContext)
            throws RemoteException {
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForConfigAttr(
                            connectionContext,
                            user,
                            key,
                            "getConfigAttr",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("key", key);
                                    }
                                })));
        }
        try {
            return userstore.getConfigAttr(user, key);
        } catch (final SQLException ex) {
            final String message = "could not retrieve config attr: user: " + user + " || key: " + key;
            logger.error(message, ex);
            throw new RemoteException(message, ex);
        }
    }

    @Override
    @Deprecated
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return hasConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key, final ConnectionContext connectionContext)
            throws RemoteException {
        return getConfigAttr(user, key, connectionContext) != null;
    }

    @Override
    @Deprecated
    public HistoryObject[] getHistory(final int classId, final int objectId, final User user, final int elements)
            throws RemoteException {
        return getHistory(classId, objectId, user, elements, ConnectionContext.createDeprecated());
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final User user,
            final int elements,
            final ConnectionContext connectionContext) throws RemoteException {
        try {
            if (ConnectionContextBackend.getInstance().isEnabled()) {
                ConnectionContextBackend.getInstance()
                        .log(ConnectionContextLog.create(
                                connectionContext,
                                user,
                                "getHistory",
                                Collections.unmodifiableMap(new HashMap<String, Object>() {

                                        {
                                            put("classId", classId);
                                            put("objectId", objectId);
                                            put("elements", elements);
                                        }
                                    })));
            }

            return historyServer.getHistory(classId, objectId, user, elements);
        } catch (final HistoryException e) {
            final String message = "could not retrieve history: user: " + user + " || classid: " + classId // NOI18N
                        + "|| objectId: " + objectId + " || elements: " + elements;                        // NOI18N
            logger.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    @Override
    @Deprecated
    public Object executeTask(final User user,
            final String taskname,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return executeTask(user, taskname, body, ConnectionContext.createDeprecated(), params);
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final Object body,
            final ConnectionContext connectionContext,
            final ServerActionParameter... params) throws RemoteException {
        logger.info("executing task '" + taskname + "' with " + params.length
                    + " server action parameters and body object: " + (body != null));
        if (ConnectionContextBackend.getInstance().isEnabled()) {
            ConnectionContextBackend.getInstance()
                    .log(ConnectionContextLog.createForTask(
                            connectionContext,
                            user,
                            taskname,
                            "executeTask",
                            Collections.unmodifiableMap(new HashMap<String, Object>() {

                                    {
                                        put("taskname", taskname);
                                        put("body", body);
                                        put("params", Arrays.toString(params));
                                    }
                                })));
        }
        if (hasConfigAttr(user, SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX + taskname, connectionContext)) {
            final ServerAction serverAction = serverActionMap.get(taskname);
            if (serverAction != null) {
                if (serverAction instanceof ConnectionContextStore) {
                    ((ConnectionContextStore)serverAction).initWithConnectionContext(connectionContext);
                }

                if (serverAction instanceof MetaServiceStore) {
                    ((MetaServiceStore)serverAction).setMetaService(this);
                }
                if (serverAction instanceof CatalogueServiceStore) {
                    ((CatalogueServiceStore)serverAction).setCatalogueService(this);
                }
                if (serverAction instanceof UserServiceStore) {
                    ((UserServiceStore)serverAction).setUserService(this);
                }
                if (serverAction instanceof Sirius.server.middleware.interfaces.domainserver.UserStore) {
                    ((Sirius.server.middleware.interfaces.domainserver.UserStore)serverAction).setUser(user);
                }

                if (serverAction instanceof ScheduledServerAction) {
                    if (ScheduledServerActionManager.isScheduledServerActionFeatureSupported(
                                    dbServer.getActiveDBConnection())) {
                        final String key = ((ScheduledServerAction)serverAction).createKey();
                        try {
                            return scheduledManager.scheduleAction(
                                    user,
                                    key,
                                    (ScheduledServerAction)serverAction,
                                    body,
                                    params);
                        } catch (Exception ex) {
                            logger.error("error whhile scheduling serveraction", ex);
                            return null;
                        }
                    } else {
                        throw new UnsupportedOperationException(
                            "this server instance does not support scheduled server action feature"); // NOI18N
                    }
                } else {
                    final long start = System.currentTimeMillis();
                    final Object result = serverAction.execute(body, params);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Server Action '" + taskname + "' successfully executed in "
                                    + (System.currentTimeMillis() - start) + "ms.");
                    }
                    return result;
                }
            } else {
                logger.warn("Server Action '" + taskname + "' not found! Returning null.");
                return null;
            }
        } else {
            final String message = "The user " + user
                        + "has no permission to execute this task. (Should have an action attribute like this: "
                        + SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX + taskname;
            logger.error(message);
            throw new RemoteException(message);
        }
    }
}
