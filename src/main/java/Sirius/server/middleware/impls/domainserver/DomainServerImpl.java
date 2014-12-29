/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.Server;
import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.ServerType;
import Sirius.server.Shutdown;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.history.HistoryException;
import Sirius.server.localserver.history.HistoryServer;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.query.QueryCache;
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
import Sirius.server.middleware.types.Node;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.UserServer;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import Sirius.server.search.Query;
import Sirius.server.search.SearchResult;
import Sirius.server.search.Seeker;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.SystemStatement;

import org.apache.log4j.PropertyConfigurator;

import org.openide.util.Lookup;

import java.io.File;

import java.net.InetAddress;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
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
import de.cismet.cids.server.ws.rest.RESTfulService;

import de.cismet.cids.utils.ClassloadingHelper;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DomainServerImpl extends UnicastRemoteObject implements CatalogueService,
    MetaService,
    SystemService,
    UserService,
    QueryStore,
    SearchService,
    ActionService { // ActionListener

    //~ Static fields/initializers ---------------------------------------------

    private static final String EXTENSION_FACTORY_PREFIX = "de.cismet.cids.custom.extensionfactories."; // NOI18N
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
    protected Seeker seeker;
    // for storing and loading prdefinded queries
    protected Store queryStore;
    protected QueryCache queryCache;
    // references to the Registry
    protected NameServer nameServer;
    protected UserServer userServer;
    // this severs info object
    protected Server serverInfo;
    private HashMap<String, ServerAction> serverActionMap = new HashMap<String, ServerAction>();
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
                PropertyConfigurator.configure(fileName);
            }

            serverInfo = new Server(
                    ServerType.LOCALSERVER,
                    properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(),
                    properties.getRMIRegistryPort(),
                    String.valueOf(properties.getServerPort()));

            dbServer = new DBServer(properties);

            userstore = dbServer.getUserStore();

            seeker = new Seeker(dbServer);

            queryStore = new Store(dbServer.getActiveDBConnection().getConnection(), properties);

            // All executable queries
            queryCache = new QueryCache(dbServer.getActiveDBConnection(), properties.getServerName());

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

            final Collection<? extends DomainServerStartupHook> startupHooks = Lookup.getDefault()
                        .lookupAll(DomainServerStartupHook.class);
            for (final DomainServerStartupHook hook : startupHooks) {
                if (hook.getDomain() != null) {
                    if (hook.getDomain().equalsIgnoreCase(properties.getServerName())
                                || hook.getDomain().equalsIgnoreCase(
                                    DomainServerStartupHook.START_ON_DOMAIN.ANY.toString())) {
                        hook.domainServerStarted();
                    }
                }
            }

            final Collection<? extends ServerAction> serverActions = Lookup.getDefault().lookupAll(ServerAction.class);
            for (final ServerAction serverAction : serverActions) {
                serverActionMap.put(serverAction.getTaskName(), serverAction);
            }

            DomainServerClassCache.getInstance().setAllClasses(dbServer.getClasses(), properties.getServerName());

            scheduledManager = new ScheduledServerActionManager(
                    this,
                    dbServer.getActiveDBConnection(),
                    userServer,
                    serverInfo.getName());
            scheduledManager.resumeAll();

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

    @Override
    public NodeReferenceList getChildren(final Node node, final User user) throws RemoteException {
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
    public NodeReferenceList getRoots(final User user) throws RemoteException {
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
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        try {
            return dbServer.getTree().addNode(node, parent, user);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
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
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
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
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
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
    public Node[] getNodes(final User user, final int[] ids) throws RemoteException {
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
    public NodeReferenceList getClassTreeNodes(final User user) throws RemoteException {
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
    public MetaClass[] getClasses(final User user) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClasses(user);

            // return new MetaClass[0];
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public MetaClass getClass(final User user, final int classID) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClass(user, classID);

            // return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public MetaClass getClassByTableName(final User user, final String tableName) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClassByTableName(user, tableName);
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
    public MetaObject[] getObjects(final User user, final String[] objectIDs) throws RemoteException {
        try {
            return dbServer.getObjects(objectIDs, user);
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
    public MetaObject getObject(final User user, final String objectID) throws RemoteException {
        try {
            final MetaObject mo = dbServer.getObject(objectID, user);
            if (mo != null) {
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

    // retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
    @Override
    public Node getMetaObjectNode(final User usr, final int nodeID) throws RemoteException {
        final int[] tmp = { nodeID };

        // single value directly referenced
        return getNodes(usr, tmp)[0];
    }

    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    // MetaObject ersetzt DefaultObject
    @Override
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID) throws RemoteException {
        return getObject(usr, objectID + "@" + classID); // NOI18N
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query) throws RemoteException {
        try {
            // user spaeter erweitern
            return seeker.search(query, new int[0], usr, 0).getObjects();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }
    // retrieves Meta data objects with meta data matching query (Search)

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        final MetaObject[] o = getMetaObject(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain())); // NOI18N

        return o;
    }

    @Override
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
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
        try {
            final int key = dbServer.getObjectPersitenceManager().insertMetaObject(user, metaObject);

            return this.getMetaObject(user, key, metaObject.getClassID());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public int insertMetaObject(final User user, final Query query) throws RemoteException {
        try {
            // pfusch ...
            final SearchResult searchResult = this.search(user, null, query);
            return Integer.valueOf(searchResult.getResult().toString()).intValue();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<html><body>update called for :+: <p>" + metaObject.getDebugString() + "</p></body></html>"); // NOI18N
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

    // insertion, deletion or update of meta data according to the query returns how many object's are effected
    // XXX New Method XXX dummy
    @Override
    public int update(final User user, final String metaSQL) throws RemoteException {
        try {
            // return dbServer.getObjectPersitenceManager().update(user, metaSQL);

            logger.error("update with metaSql is no longer supported " + metaSQL + "leads to no result"); // NOI18N

            return -1;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete called for" + metaObject); // NOI18N
        }

        try {
            return dbServer.getObjectPersitenceManager().deleteMetaObject(user, metaObject);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // creates an Instance of a MetaObject with all attribute values set to default
    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("usergetInstance :: " + user + "  class " + c); // NOI18N
        }
        try {
            final Sirius.server.localserver.object.Object o = dbServer.getObjectFactory().getInstance(c.getID());
            if (o != null) {
                final MetaObject mo = new DefaultMetaObject(o, c.getDomain());
                mo.setAllStatus(MetaObject.TEMPLATE);
                return mo;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException("<LS> ", e);                       // NOI18N
        }
    }

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, query), usr.getDomain())); // NOI18N
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public Node[] getMetaObjectNode(final User usr, final Query query) throws RemoteException {
        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, new int[0], usr, 0).getNodes();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        // if(userstore.validateUser(user))
        return dbServer.getMethods(); // dbServer.getMethods(user.getuserGroup()); // instead

        // return new MethodMap();
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        try {
            return dbServer.getAllLightweightMetaObjectsForClass(
                    classId,
                    user,
                    representationFields,
                    representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex); // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        try {
            return dbServer.getAllLightweightMetaObjectsForClass(
                    classId,
                    user,
                    representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex); // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        try {
            return dbServer.getLightweightMetaObjectsByQuery(
                    classId,
                    user,
                    query,
                    representationFields,
                    representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex); // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        try {
            return dbServer.getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex); // NOI18N
        }
    }

    @Override
    public Sirius.util.image.Image[] getDefaultIcons() throws RemoteException {
        return properties.getDefaultIcons();
    }

    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException {
        try {
            return userstore.changePassword(user, oldPassword, newPassword);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("changePassword at remotedbserverimpl", e); // NOI18N
        }
    }

    @Override
    public boolean validateUser(final User user, final String password) throws RemoteException {
        try {
            return userstore.validateUserPassword(user, password);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("Exception validateUser at remotedbserverimpl", e); // NOI18N
        }
    }

    @Override
    public boolean delete(final int id) throws RemoteException {
        return queryStore.delete(id);
    }

    @Override
    public QueryData getQuery(final int id) throws RemoteException {
        return queryStore.getQuery(id);
    }

    @Override
    public Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        return queryStore.getQueryInfos(userGroup);
    }

    @Override
    public Info[] getQueryInfos(final User user) throws RemoteException {
        return queryStore.getQueryInfos(user);
    }

    @Override
    public boolean storeQuery(final User user, final QueryData data) throws RemoteException {
        return queryStore.storeQuery(user, data);
    }

    // add single query root and leaf returns a query_id
    @Override
    public int addQuery(final String name,
            final String description,
            final String statement,
            final int resultType,
            final char isUpdate,
            final char isBatch,
            final char isRoot,
            final char isUnion) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement, resultType, isUpdate, isBatch, isRoot, isUnion);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e); // NOI18N
        }
    }

    @Override
    public int addQuery(final String name, final String description, final String statement) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e); // NOI18N
        }
    }

    @Override
    public boolean addQueryParameter(final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, typeId, paramkey, description, isQueryResult, queryPosition);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e); // NOI18N
        }
    }

    // position set in order of the addition
    @Override
    public boolean addQueryParameter(final int queryId, final String paramkey, final String description)
            throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, paramkey, description);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e); // NOI18N
        }
    }

    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        final HashMap r = queryCache.getSearchOptions();
        if (logger.isDebugEnabled()) {
            logger.debug("in Domainserverimpl :: " + r); // NOI18N
        }
        return r;
    }

    @Override
    public SearchResult search(final User user, final int[] classIds, final Query query) throws RemoteException {
        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, classIds, user, 0);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<ArrayList> performCustomSearch(final String query) throws RemoteException {
        try {
            final Statement s = getConnectionPool().getDBConnection().getConnection().createStatement();
            final ResultSet rs = s.executeQuery(query);
            final ArrayList<ArrayList> result = new ArrayList<ArrayList>();
            while (rs.next()) {
                final ArrayList row = new ArrayList();
                for (int i = 0; i < rs.getMetaData().getColumnCount(); ++i) {
                    row.add(rs.getObject(i + 1));
                }
                result.add(row);
            }
            return result;
        } catch (Exception e) {
            final String msg = "Error during sql statement: " + query;
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
            seeker = null;

            // this servers configuration
            properties = null;

            // for storing and loading prdefinded queries
            queryStore = null;

            queryCache = null;

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
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
        p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
        p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
        p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N
        p.put("log4j.rootLogger", "DEBUG,Remote");                             // NOI18N
        org.apache.log4j.PropertyConfigurator.configure(p);

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

            // abfragen, ob schon eine  RMI Registry exitiert.
            try {
                LocateRegistry.getRegistry(rmiPort);
                // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
            } catch (Exception e) {
                // wenn nicht, neue Registry starten und auf portnummer setzen
                LocateRegistry.createRegistry(rmiPort);
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
            PropertyConfigurator.configure(log4jPropFile.getAbsolutePath());
        }
    }

    @Override
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        try {
            return userstore.getConfigAttr(user, key);
        } catch (final SQLException ex) {
            final String message = "could not retrieve config attr: user: " + user + " || key: " + key;
            logger.error(message, ex);
            throw new RemoteException(message, ex);
        }
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return getConfigAttr(user, key) != null;
    }

    @Override
    public HistoryObject[] getHistory(final int classId, final int objectId, final User user, final int elements)
            throws RemoteException {
        try {
            return historyServer.getHistory(classId, objectId, user, elements);
        } catch (final HistoryException e) {
            final String message = "could not retrieve history: user: " + user + " || classid: " + classId // NOI18N
                        + "|| objectId: " + objectId + " || elements: " + elements;                        // NOI18N
            logger.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        if (hasConfigAttr(user, SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX + taskname)) {
            final ServerAction serverAction = serverActionMap.get(taskname);

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

            if (serverAction != null) {
                if (serverAction instanceof ScheduledServerAction) {
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
                    return serverAction.execute(body, params);
                }
            } else {
                return null;
            }
        } else {
            throw new RemoteException("The user " + user
                        + "has no permission to execute this task. (Should have an action attribute like this: "
                        + SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX + taskname);
        }
    }
}
