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
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.query.QueryCache;
import Sirius.server.localserver.query.querystore.Store;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.user.UserStore;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.interfaces.domainserver.CatalogueService;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.QueryStore;
import Sirius.server.middleware.interfaces.domainserver.SearchService;
import Sirius.server.middleware.interfaces.domainserver.SystemService;
import Sirius.server.middleware.interfaces.domainserver.UserService;
import Sirius.server.middleware.types.DefaultMetaObject;
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
import Sirius.server.sql.SystemStatement;

import org.apache.log4j.PropertyConfigurator;

import java.net.InetAddress;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.MissingResourceException;

import de.cismet.cids.objectextension.ObjectExtensionFactory;

import de.cismet.cids.server.DefaultServerExceptionHandler;
import de.cismet.cids.server.ServerSecurityManager;
import de.cismet.cids.server.ws.rest.RESTfulService;

import de.cismet.tools.BlacklistClassloading;

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
    SearchService { // ActionListener

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2905210154509680168L;

    private static final String EXTENSION_FACTORY_PREFIX = "de.cismet.cids.custom.extensionfactories.";

    private static transient DomainServerImpl instance;

    //~ Instance fields --------------------------------------------------------

    // dbaccess of the mis (catalogue, classes and objects
    protected DBServer dbServer;
    // userservice of a localserver
    protected UserStore userstore;
    // executing the searchservice
    protected Seeker seeker;
    // this servers configuration
    protected ServerProperties properties;
    // for storing and loading prdefinded queries
    protected Store queryStore;
    protected QueryCache queryCache;
    // this severs port
    protected int myPort;
    // references to the Registry
    protected NameServer nameServer;
    protected UserServer userServer;
    // this severs info object
    protected Server serverInfo;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

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
            if (((fileName = properties.getLog4jPropertyFile()) != null) && !fileName.equals("")) {
                PropertyConfigurator.configure(fileName);
            }

            try {
                this.myPort = properties.getServerPort();
                serverInfo = new Server(
                        ServerType.LOCALSERVER,
                        properties.getServerName(),
                        InetAddress.getLocalHost().getHostAddress(),
                        properties.getRMIRegistryPort());
            } catch (Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<LS> ERROR ::  Key serverPort is Missing!");
                }

                this.myPort = 8912;
                serverInfo = new Server(
                        ServerType.LOCALSERVER,
                        properties.getServerName(),
                        InetAddress.getLocalHost().getHostAddress(),
                        properties.getRMIRegistryPort());
            }

            dbServer = new DBServer(properties);

            userstore = dbServer.getUserStore();

            seeker = new Seeker(dbServer);

            queryStore = new Store(dbServer.getActiveDBConnection().getConnection(), properties);

            // All executable queries
            queryCache = new QueryCache(dbServer.getActiveDBConnection(), properties.getServerName());

            System.out.println("\n<LS> DBConnection: " + dbServer.getActiveDBConnection().getURL() + "\n");

            System.out.println(serverInfo.getRMIAddress());
            logger.info(serverInfo.getRMIAddress());
            System.out.println("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());
            logger.info("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());
            Naming.bind(serverInfo.getBindString(), this);

            // status = new ServerStatus();

            register();

            if (logger.isDebugEnabled()) {
                logger.debug("Server Referenz " + this);
            }

            // initFrame();
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public NodeReferenceList getChildren(final Node node, final User user) throws RemoteException {
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getChildren(node, user.getUserGroup());
            }

            return new NodeReferenceList(); // no permission
        } catch (Throwable e) {
            if (logger != null) {
                logger.error("Fehler in getChildren()", e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    public NodeReferenceList getRoots(final User user) throws RemoteException {
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getTops(user.getUserGroup());
            }

            return new NodeReferenceList(); // no permission => empty list
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            return new NodeReferenceList();
                // throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        try {
            return dbServer.getTree().addNode(node, parent, user);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
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
            throw new RemoteException(e.getMessage());
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
            throw new RemoteException(e.getMessage());
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
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Node[] getNodes(final User user, final int[] ids) throws RemoteException {
        try {
            return dbServer.getNodes(ids, user.getUserGroup());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public NodeReferenceList getClassTreeNodes(final User user) throws RemoteException {
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getClassTreeNodes(user.getUserGroup());
            }

            return new NodeReferenceList(); // no permission empty list
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public MetaClass[] getClasses(final User user) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClasses(user.getUserGroup());

            // return new MetaClass[0];
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public MetaClass getClass(final User user, final int classID) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClass(user.getUserGroup(), classID);

            // return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public MetaClass getClassByTableName(final User user, final String tableName) throws RemoteException {
        try { // if(userstore.validateUser(user))
            return dbServer.getClassByTableName(user.getUserGroup(), tableName);
                // return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
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
            return dbServer.getObjects(objectIDs, user.getUserGroup());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getExtensionFactoryClassnameByCOnvention(final MetaObject mo) {
        String className = mo.getMetaClass().getTableName().toLowerCase();
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        return EXTENSION_FACTORY_PREFIX + mo.getDomain().toLowerCase() + "." + className + "ExtensionFactory";
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
            final MetaObject mo = dbServer.getObject(objectID, user.getUserGroup());

            final MetaClass[] classes = dbServer.getClasses(user.getUserGroup());
            mo.setAllClasses(getClassHashTable(classes, serverInfo.getName()));

            // Check if Object can be extended
            if (mo.getMetaClass().hasExtensionAttributes()) {
                // TODO:Check if there is a ExtensionFactory
                final String className = getExtensionFactoryClassnameByCOnvention(mo);

                final Class extensionFactoryClass = BlacklistClassloading.forName(className);

                if (extensionFactoryClass != null) {
                    final ObjectExtensionFactory ef = (ObjectExtensionFactory)extensionFactoryClass.newInstance();
                    try {
                        ef.extend(mo.getBean());
                    } catch (Exception e) {
                        logger.error("Error during ObjectExtension", e);
                    }
                }
            }
            return mo;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
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
    public static Hashtable getClassHashTable(final MetaClass[] classes, final String serverName) {
        final Hashtable classHash = new Hashtable();
        for (int i = 0; i < classes.length; i++) {
            final String key = new String(serverName + classes[i].getID());
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
        return getObject(usr, objectID + "@" + classID);
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query) throws RemoteException {
        try {
            // user spaeter erweitern
            return (MetaObject[])seeker.search(query, new int[0], usr.getUserGroup(), 0).getObjects();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }
    // retrieves Meta data objects with meta data matching query (Search)

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        final MetaObject[] o = (MetaObject[])getMetaObject(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain()));

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
            throw new RemoteException(e.getMessage());
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
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<html><body>update called for :+: <p>" + metaObject.getDebugString() + "</p></body></html>");
        }
        try {
            dbServer.getObjectPersitenceManager().updateMetaObject(user, metaObject);

            return 1;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    // insertion, deletion or update of meta data according to the query returns how many object's are effected
    // XXX New Method XXX dummy
    @Override
    public int update(final User user, final String metaSQL) throws RemoteException {
        try {
            // return dbServer.getObjectPersitenceManager().update(user, metaSQL);

            logger.error("update with metaSql is no longer supported " + metaSQL + "leads to no result");

            return -1;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete called for" + metaObject);
        }

        try {
            return dbServer.getObjectPersitenceManager().deleteMetaObject(user, metaObject);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    // creates an Instance of a MetaObject with all attribute values set to default
    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("usergetInstance :: " + user + "  class " + c);
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
            throw new RemoteException("<LS> ", e);
        }
    }

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, query), usr.getDomain()));
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public Node[] getMetaObjectNode(final User usr, final Query query) throws RemoteException {
        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, new int[0], usr.getUserGroup(), 0).getNodes();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
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
            return dbServer.getObjectFactory()
                        .getAllLightweightMetaObjectsForClass(
                            classId,
                            user,
                            representationFields,
                            representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);
        }
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getAllLightweightMetaObjectsForClass(
                            classId,
                            user,
                            representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getLightweightMetaObjectsByQuery(
                            classId,
                            user,
                            query,
                            representationFields,
                            representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);
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
            throw new RemoteException("changePassword at remotedbserverimpl", e);
        }
    }

    @Override
    public boolean validateUser(final User user, final String password) throws RemoteException {
        try {
            return userstore.validateUserPassword(user, password);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("Exception validateUser at remotedbserverimpl", e);
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
            throw new RemoteException("addQuery error", e);
        }
    }

    @Override
    public int addQuery(final String name, final String description, final String statement) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);
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
            throw new RemoteException("addQuery error", e);
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
            throw new RemoteException("addQuery error", e);
        }
    }

    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        final HashMap r = queryCache.getSearchOptions();
        if (logger.isDebugEnabled()) {
            logger.debug("in Domainserverimpl :: " + r);
        }
        return r;
    }

    @Override
    public SearchResult search(final User user, final int[] classIds, final Query query) throws RemoteException {
        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, classIds, user.getUserGroup(), 0);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
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
                    nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");
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
                    System.err.println("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i]);
                    logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);
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
            throw new ServerExitError("registration failed");
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
            logger.info("shutting down domainserver impl: " + this);
        }
        final String ip = serverInfo.getIP();
        final String lsName = properties.getServerName();
        final String[] registryIPs = properties.getRegistryIps();
        final String rmiPort = serverInfo.getRMIPort();

        for (int i = 0; i < registryIPs.length; i++) {
            try {
                nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");
                userServer = (UserServer)nameServer;

                // User und UserGroups bei Registry abmelden
                userServer.unregisterUsers(userstore.getUsers());
                userServer.unregisterUserGroups(userstore.getUserGroups());

                // LocalServer bei Registry abmelden
                nameServer.unregisterServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);
            } catch (NotBoundException nbe) {
                logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);
            } catch (RemoteException re) {
                logger.error("<LS> RMIRegistry on " + registryIPs[i] + "could not be contacted", re);
            } catch (Throwable e) {
                logger.error(e, e);
            }
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("<LS> unbind for " + serverInfo.getBindString());
            }
            Naming.unbind(serverInfo.getBindString());

            if (properties.getStartMode().equalsIgnoreCase("simple")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("shutting down restful interface"); // NOI18N
                }
                RESTfulService.down();
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("shutting down startproxy");    // NOI18N
                    }
                    StartProxy.getInstance().shutdown();
                } catch (final ServerExit serverExit) {
                    // skip
                }
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("shutting down registry");      // NOI18N
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

        throw new ServerExit("Server ist regul\u00E4r beendet worden");
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
            throw new ServerExitError("args == null keine Kommandozeilenparameter \u00FCbergeben (Configfile / port)");
        } else if (args.length < 1) {
            throw new ServerExitError("zu wenig Argumente");
        }

        if (instance != null) {
            throw new IllegalStateException("an instance was already created");
        }

        try {
            try {
                properties = new ServerProperties(args[0]);
                rmiPort = new Integer(properties.getRMIRegistryPort()).intValue();
            } catch (MissingResourceException mre) {
                System.err.println("Info :: <LS> Key  rmiRegistryPort  in ConfigFile +" + args[0] + " is Missing!");
                System.err.println("Info :: <LS> Set Default to 1099");
                rmiPort = 1099;
            }

            System.out.println("<LS> ConfigFile: " + args[0]);

            // abfragen, ob schon eine  RMI Registry exitiert.
            try {
                LocateRegistry.getRegistry(rmiPort);
                // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
            } catch (Exception e) {
                // wenn nicht, neue Registry starten und auf portnummer setzen
                LocateRegistry.createRegistry(rmiPort);
            }

            if (properties.getStartMode().equalsIgnoreCase("simple")) {
                Sirius.server.registry.Registry.getServerInstance(rmiPort);
                StartProxy.getInstance(args[0]);
            }

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new ServerSecurityManager());
            }

            instance = new DomainServerImpl(new ServerProperties(args[0]));

            System.out.println("Info :: <LS>  !!!LocalSERVER started!!!!");
        } catch (Exception e) {
            System.err.println("Fehler beim Start des Domainservers :: " + e.getMessage());
            e.printStackTrace();
            if (instance != null) {
                instance.shutdown();
            }
            throw new ServerExitError(e);
        }
    }
}
