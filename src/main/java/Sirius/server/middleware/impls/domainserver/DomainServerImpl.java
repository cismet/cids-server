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
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.query.QueryCache;
import Sirius.server.localserver.query.querystore.Store;
import Sirius.server.localserver.user.UserStore;
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
import Sirius.server.search.Query;
import Sirius.server.search.SearchResult;
import Sirius.server.search.Seeker;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;
import Sirius.server.sql.SystemStatement;

import java.net.InetAddress;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.MissingResourceException;

import org.apache.log4j.PropertyConfigurator;

import de.cismet.cids.objectextension.ObjectExtensionFactory;

import de.cismet.tools.BlacklistClassloading;

import java.util.Hashtable;

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

    protected static DomainServerImpl THIS;
    private static final String EXTENSION_FACTORY_PREFIX = "de.cismet.cids.custom.extensionfactories.";   // NOI18N

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
    public DomainServerImpl(ServerProperties properties) throws Throwable {
        // export object
        super(properties.getServerPort());

        try {
            this.properties = properties;
            String fileName;
            if (((fileName = properties.getLog4jPropertyFile()) != null) && !fileName.equals("")) {   // NOI18N
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
                    logger.debug("<LS> ERROR ::  Key serverPort is Missing!");   // NOI18N
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

            System.out.println("\n<LS> DBConnection: " + dbServer.getActiveDBConnection().getURL() + "\n");   // NOI18N

            System.out.println(serverInfo.getRMIAddress());
            logger.info(serverInfo.getRMIAddress());
            System.out.println("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());   // NOI18N
            logger.info("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());   // NOI18N
            Naming.bind(serverInfo.getBindString(), this);

            // status = new ServerStatus();

            register();

            THIS = this;
            if (logger.isDebugEnabled()) {
                logger.debug("Server Referenz " + this);   // NOI18N
            }

            // initFrame();
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * //////////////////////////////////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public static final DomainServerImpl getServerInstance() {
        return THIS;
    }

    // public ServerStatus getStatus()
    // {return status;}
    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin CatalogueService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // ---------------------------------------------------------------------------------
//    public NodeReferenceList getChildren(User user,int nodeID ) throws RemoteException
//    {
//        logger.debug("getchildren f\u00FCr" + nodeID);
//
//        try
//        {     if(userstore.validateUser(user))
//                    return dbServer.getChildren(nodeID,user.getUserGroup());
//
//                return new NodeReferenceList(); // no permission
//        }
//        catch(Throwable e)
//        {
//            if (logger!=null) logger.error(e.getMessage(),e);
//            throw new RemoteException(e.getMessage());
//        }
//    }
    @Override
    public NodeReferenceList getChildren(Node node, User user) throws RemoteException {
        try {
            if (userstore.validateUser(user)) {
                return dbServer.getChildren(node, user.getUserGroup());
            }

            return new NodeReferenceList(); // no permission
        } catch (Throwable e) {
            if (logger != null) {
                logger.error("Error in getChildren()", e);   // NOI18N
            }
            throw new RemoteException(e.getMessage());
        }
    }
//    //--------------------------------------------------------------------------------
//    public Node[] getParents(User user,int nodeID ) throws RemoteException
//    {
//
//        try
//        {     //if(userstore.validateUser(user))
//                    return dbServer.getParents(nodeID,user.getUserGroup());
//
//               // return new Node[0]; // no permission
//        }
//        catch(Throwable e)
//        {
//            if (logger!=null) logger.error(e,e);
//            throw new RemoteException(e.getMessage());
//        }
//    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    public NodeReferenceList getRoots(User user) throws RemoteException {
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
    public Node addNode(Node node, Link parent, User user) throws RemoteException {
        try {
            return dbServer.getTree().addNode(node, parent, user);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public boolean deleteNode(Node node, User user) throws RemoteException {
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
    public boolean addLink(Node from, Node to, User user) throws RemoteException {
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
    public boolean deleteLink(Node from, Node to, User user) throws RemoteException {
        try {
            return dbServer.getTree().deleteLink(from, to, user);
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }
//    public boolean copySubTree(Node root, User user) throws RemoteException
//    {
//        try
//        {
//            return dbServer.getTree().copySubTree(root,user);
//
//        }
//        catch(Throwable e)
//        {
//            if (logger!=null) logger.error(e,e);
//            throw new RemoteException(e.getMessage());
//        }
//
//    }

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End   CatalogueService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // ---------------------------------------------------------------------------------------------------
    @Override
    public Node[] getNodes(User user, int[] ids) throws RemoteException {
        try {
            return dbServer.getNodes(ids, user.getUserGroup());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    // --------------------------------------------------------------------------------------------------------------------
// public Node[] getObjectNodes(User user, String[] objectIDs) throws RemoteException
// {
//
// Node[] nodes = null ;
//
// try
// {
// UserGroup ug = user.getUserGroup();
// nodes = dbServer.getObjectNodes(objectIDs,ug).getLocalNodes();
// }
// catch(Throwable e)
// { if (logger!=null) logger.error(e);throw new RemoteException(e.getMessage(),e);}
//
// return nodes;
// }
//
    // ---------------------------------------------------------------------------------------------------
    @Override
    public NodeReferenceList getClassTreeNodes(User user) throws RemoteException {
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

    // ---------------------------------------------------------------------------------------------------
    @Override
    public MetaClass[] getClasses(User user) throws RemoteException {
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

    // ---------------------------------------------------------------------------------------------------
    @Override
    public MetaClass getClass(User user, int classID) throws RemoteException {
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
    public MetaClass getClassByTableName(User user, String tableName) throws RemoteException {
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
     * --------------------------------------------------------------------------------------------------- ????????????
     *
     * @param   user       DOCUMENT ME!
     * @param   objectIDs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject[] getObjects(User user, String[] objectIDs) throws RemoteException {
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
        return EXTENSION_FACTORY_PREFIX + mo.getDomain().toLowerCase() + "." + className + "ExtensionFactory";   // NOI18N
    }
    /**
     * ---------------------------------------------------------------------------------------------------
     *
     * @param   user      DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public MetaObject getObject(User user, String objectID) throws RemoteException {
        try {
            MetaObject mo = dbServer.getObject(objectID, user.getUserGroup());

            final MetaClass[] classes = dbServer.getClasses(user.getUserGroup());
            mo.setAllClasses(getClassHashTable(classes, serverInfo.getName()));

            // Check if Object can be extended
            if (mo.getMetaClass().hasExtensionAttributes()) {
                // TODO:Check if there is a ExtensionFactory
                String className = getExtensionFactoryClassnameByCOnvention(mo);

                Class extensionFactoryClass = BlacklistClassloading.forName(className);

                if (extensionFactoryClass != null) {
                    ObjectExtensionFactory ef = (ObjectExtensionFactory)extensionFactoryClass.newInstance();
                    try {
                        ef.extend(mo.getBean());
                    } catch (Exception e) {
                        logger.error("Error during ObjectExtension", e);   // NOI18N
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
    public static Hashtable getClassHashTable(MetaClass[] classes, String serverName) {
        Hashtable classHash = new Hashtable();
        for (int i = 0; i < classes.length; i++) {
            String key = new String(serverName + classes[i].getID());
            if (!classHash.containsKey(key)) {
                classHash.put(key, classes[i]);
            }
        }

        return classHash;
    }

    // ---------------------------------------------------------------------------------------------------
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin   Metaservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
    @Override
    public Node getMetaObjectNode(User usr, int nodeID) throws RemoteException {
        int[] tmp = { nodeID };

        // single value directly referenced
        return getNodes(usr, tmp)[0];
    }

    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    // MetaObject ersetzt DefaultObject
    @Override
    public MetaObject getMetaObject(User usr, int objectID, int classID) throws RemoteException {
        return getObject(usr, objectID + "@" + classID);   // NOI18N
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {
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
    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {
        // return getMetaObject( usr,new Query(new
        // SystemStatement(true,-1,"",false,SearchResult.NODE,query),usr.getDomain() )  );

        MetaObject[] o = (MetaObject[])getMetaObject(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain()));   // NOI18N

        return o;
    }

    /* MetaService - MetaJDBC*/
    // inserts metaObject in the MIS
    @Override
    public MetaObject insertMetaObject(User user, MetaObject metaObject) throws RemoteException {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "<html>insert MetaObject for User :+:" + user + "  MO " + metaObject.getDebugString() + "</html>");   // NOI18N
            }
        }
        try {
            int key = dbServer.getObjectPersitenceManager().insertMetaObject(user, metaObject);

            return this.getMetaObject(user, key, metaObject.getClassID());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int insertMetaObject(User user, Query query) throws RemoteException {
        try {
            // XXX unterst\u00FCtzt keine batch queries
            // return metaJDBCService.insertMetaObject(user, query);

            // pfusch ...
            SearchResult searchResult = this.search(user, null, query);
            return Integer.valueOf(searchResult.getResult().toString()).intValue();
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int updateMetaObject(User user, MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<html><body>update called for :+: <p>" + metaObject.getDebugString() + "</p></body></html>");   // NOI18N
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
    public int update(User user, String metaSQL) throws RemoteException {
        try {
            // return dbServer.getObjectPersitenceManager().update(user, metaSQL);

            logger.error("update with metaSql is no longer supported " + metaSQL + "leads to no result");   // NOI18N

            return -1;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int deleteMetaObject(User user, MetaObject metaObject) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete called for" + metaObject);   // NOI18N
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
    /* /MetaService - MetaJDBC*/

    // creates an Instance of a MetaObject with all attribute values set to default
    @Override
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("usergetInstance :: " + user + "  class " + c);   // NOI18N
        }
        try {
            Sirius.server.localserver.object.Object o = dbServer.getObjectFactory().getInstance(c.getID());
            if (o != null) {
                MetaObject mo = new DefaultMetaObject(o, c.getDomain());
                mo.setAllStatus(MetaObject.TEMPLATE);
                return mo;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException("<LS> ", e);   // NOI18N
        }
    }

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    @Override
    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        return getMetaObjectNode(
                usr,
                new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, query), usr.getDomain()));   // NOI18N
    }

    // retrieves Meta data objects with meta data matching query (Search)
    @Override
    public Node[] getMetaObjectNode(User usr, Query query) throws RemoteException {
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
    public MethodMap getMethods(User user) throws RemoteException {
        // if(userstore.validateUser(user))
        return dbServer.getMethods(); // dbServer.getMethods(user.getuserGroup()); // instead

        // return new MethodMap();

    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getAllLightweightMetaObjectsForClass(
                            classId,
                            user,
                            representationFields,
                            representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);   // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory().getAllLightweightMetaObjectsForClass(
                    classId,
                    user,
                    representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);   // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getLightweightMetaObjectsByQuery(
                            classId,
                            user,
                            query,
                            representationFields,
                            representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);   // NOI18N
        }
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory()
                        .getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);   // NOI18N
        }
    }

//    //---!!!
//    public LightweightMetaObject[] getLightweightMetaObjects(User usr, String query) throws RemoteException {
//        final ObjectFactory factory = dbServer.getObjectFactory();
//        final java.sql.Connection javaCon = dbServer.getConnectionPool().getConnection().getConnection();
////        Query q = new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain());
//        return null;
//    }
//
//    //---!!!
//    public LightweightMetaObject getLightweightMetaObject(User usr, int objectID, int classID) throws RemoteException {
//        return null;
//    }
    ///////////////////////////////////////////////////////////////////////////////////
    /////////////////End   Metaservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin     Systemservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Sirius.util.image.Image[] getDefaultIcons() throws RemoteException {
        return properties.getDefaultIcons();
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End     SystemService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin     UserService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // ---------------------------------------------------------------------------------------------------
    @Override
    public boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException {
        try {
            return userstore.changePassword(user, oldPassword, newPassword);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("changePassword at remotedbserverimpl", e);   // NOI18N
        }
    }
    // ---------------------------------------------------------------------------------------------------

    @Override
    public boolean validateUser(User user, String password) throws RemoteException {
        try {
            return userstore.validateUserPassword(user, password);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("Exception validateUser at remotedbserverimpl", e);   // NOI18N
        }
    }
    // ---------------------------------------------------------------------------------------------------

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End   UserService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin QueryStoreservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean delete(int id) throws RemoteException {
        return queryStore.delete(id);
    }

    @Override
    public QueryData getQuery(int id) throws RemoteException {
        return queryStore.getQuery(id);
    }

    @Override
    public Info[] getQueryInfos(UserGroup userGroup) throws RemoteException {
        return queryStore.getQueryInfos(userGroup);
    }

    @Override
    public Info[] getQueryInfos(User user) throws RemoteException {
        return queryStore.getQueryInfos(user);
    }

    @Override
    public boolean storeQuery(User user, QueryData data) throws RemoteException {
        return queryStore.storeQuery(user, data);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End   QueryStoreService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin SearchService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //
    // public Collection getAllSearchOptions(User user) throws RemoteException {
    // return queryCache.getAllSearchOptions();
    //
    // }
    // add single query root and leaf returns a query_id
    @Override
    public int addQuery(
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement, resultType, isUpdate, isBatch, isRoot, isUnion);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);   // NOI18N
        }
    }

    @Override
    public int addQuery(String name, String description, String statement) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);   // NOI18N
        }
    }

    @Override
    public boolean addQueryParameter(
            int queryId,
            int typeId,
            String paramkey,
            String description,
            char isQueryResult,
            int queryPosition) throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, typeId, paramkey, description, isQueryResult, queryPosition);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);   // NOI18N
        }
    }

    // position set in order of the addition
    @Override
    public boolean addQueryParameter(int queryId, String paramkey, String description) throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, paramkey, description);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);   // NOI18N
        }
    }

    @Override
    public HashMap getSearchOptions(User user) throws RemoteException {
        HashMap r = queryCache.getSearchOptions();
        if (logger.isDebugEnabled()) {
            logger.debug("in Domainserverimpl :: " + r);   // NOI18N
        }
        return r;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public SearchResult search(User user, int[] classIds, Query query) throws RemoteException {
        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, classIds, user.getUserGroup(), 0);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }
    /**
     * ///////////////////////////////////////////////////////////////////////////////// ///////////////END
     * SearchService/////////////////////////////////////////////
     * //////////////////////////////////////////////////////////////////////////////////
     * ---------------------------------------------------------------------------------------------------
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected void register() throws Throwable {
        int registered = 0;

        try {
            String lsName = serverInfo.getName();
            String ip = serverInfo.getIP();
            String[] registryIPs = properties.getRegistryIps();
            String rmiPort = serverInfo.getRMIPort();

            for (int i = 0; i < registryIPs.length; i++) {
                try {
                    nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");   // NOI18N
                    userServer = (UserServer)nameServer;    // (UserServer)
                                                            // Naming.lookup("rmi://"+registryIPs[i]+"/userServer");

                    nameServer.registerServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);

                    logger.info(
                        "\n<LS> registered at SiriusRegistry " + registryIPs[i] + " with " + lsName + "  " + ip);   // NOI18N

                    UserStore userStore = dbServer.getUserStore();

                    userServer.registerUsers(userStore.getUsers());
                    userServer.registerUserGroups(userStore.getUserGroups());
                    userServer.registerUserMemberships(userStore.getMemberships());

                    registered++;
                    logger.info(
                        "<LS> users registered at SiriusRegistry" + registryIPs[i] + " with " + lsName + "  " + ip);   // NOI18N
                } catch (NotBoundException nbe) {
                    System.err.println("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i]);   // NOI18N
                    logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);   // NOI18N
                } catch (RemoteException re) {
                    System.err.println(
                        "<LS> No RMIRegistry on " + registryIPs[i]   // NOI18N
                        + ", therefore SiriusRegistry could not be contacted");   // NOI18N
                    logger.error(
                        "<LS> No RMIRegistry on " + registryIPs[i]   // NOI18N
                        + ", therefore SiriusRegistry could not be contacted",   // NOI18N
                        re);
                }
            }
        } catch (Throwable e) {
            logger.error(e, e);
            throw new ServerExitError(e);
        }

        if (registered == 0) {
            throw new ServerExitError("registration failed");   // NOI18N
        }
    }
    /**
     * ---------------------------------------------------------------------------------------------------
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     * @throws  ServerExit       DOCUMENT ME!
     */
    public void shutdown() throws Throwable {
        String ip = serverInfo.getIP();
        String lsName = properties.getServerName();
        String[] registryIPs = properties.getRegistryIps();
        String rmiPort = serverInfo.getRMIPort();

        for (int i = 0; i < registryIPs.length; i++) {
            try {
                nameServer = (NameServer)Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");   // NOI18N
                userServer = (UserServer)nameServer; // Naming.lookup("rmi://"+registryIPs[i]+"/userServer");

                // User und UserGroups bei Registry abmelden
                userServer.unregisterUsers(userstore.getUsers());
                userServer.unregisterUserGroups(userstore.getUserGroups());

                // LocalServer bei Registry abmelden
                nameServer.unregisterServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);
            } catch (NotBoundException nbe) {
                logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);   // NOI18N
            } catch (RemoteException re) {
                logger.error("<LS> RMIRegistry on " + registryIPs[i] + "could not be contacted", re);   // NOI18N
            } catch (Throwable e) {
                logger.error(e, e);
            }
        }

        // Naming.unbind("localServer");
        try {
            Naming.unbind(serverInfo.getBindString());

            if (properties.getStartMode().equalsIgnoreCase("simple")) {   // NOI18N
                Naming.unbind("nameServer");   // NOI18N
                Naming.unbind("callServer");   // NOI18N
                Naming.unbind("userServer");   // NOI18N
            }
            if (logger.isDebugEnabled()) {
                logger.debug("<LS> unbind for " + serverInfo.getBindString());   // NOI18N
            }

            // alle offenen Verbindungen schliessen
            THIS.dbServer.getConnectionPool().closeConnections();

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
        } catch (Exception re) {
            logger.error(re, re);
            throw new ServerExitError(re);
        }

        // THIS=null;
        //
        // System.gc(); //;-)

        throw new ServerExit("Server exited regularly");   // NOI18N
    }
    /**
     * //---------------------------------Interface
     * ActionListener------------------------------------------------------------------ public void
     * actionPerformed(ActionEvent event) { try { shutdown(); } catch(Throwable e) { throw new ServerExit("Server ist
     * regul\u00E4r beendet worden",e); } }
     * ---------------------------------------------------------------------------------------------
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static void main(String[] args) throws Throwable {
        ServerProperties properties = null;
        int rmiPort;

        if (args == null) {
            throw new ServerExitError("args == null no commandline parameter given (Configfile / port)");   // NOI18N
        } else if (args.length < 1) {
            throw new ServerExitError("insufficient arguments given");   // NOI18N
        }

        try {
            try {
                properties = new ServerProperties(args[0]);
                rmiPort = new Integer(properties.getRMIRegistryPort()).intValue();
            } catch (MissingResourceException mre) {
                System.err.println("Info :: <LS> Key  rmiRegistryPort  in ConfigFile +" + args[0] + " is Missing!");   // NOI18N
                System.err.println("Info :: <LS> Set Default to 1099");   // NOI18N
                rmiPort = 1099;
            }

            System.out.println("<LS> ConfigFile: " + args[0]);   // NOI18N

            // abfragen, ob schon eine  RMI Registry exitiert.
            java.rmi.registry.Registry rmiRegistry;
            try {
                rmiRegistry = LocateRegistry.getRegistry(rmiPort);
                // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest

                // String[] list = rmiRegistry.list();
                // int number = rmiRegistry.list().length;
                // System.out.println("<LS> RMIRegistry still exists...");
                // System.out.println("Info :: <LS> Already registered with RMIRegistry:");
                // for (int i=0; i< number; i++)
                // System.out.println("\t"+ list[i]);
            } catch (Exception e) {
                // wenn nicht, neue Registry starten und auf portnummer setzen
                rmiRegistry = LocateRegistry.createRegistry(rmiPort);

                // System.out.println("<LS> create RMIRegistry...");
            }

            if (properties.getStartMode().equalsIgnoreCase("simple")) {   // NOI18N
                new Sirius.server.registry.Registry(rmiPort);
                new Sirius.server.middleware.impls.proxy.StartProxy(args[0]);
            }

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }

            new DomainServerImpl(new ServerProperties(args[0]));

            System.out.println("Info :: <LS>  !!!LocalSERVER started!!!!");   // NOI18N
        } catch (Exception e) {
            System.err.println("Error while starting domainserver :: " + e.getMessage());   // NOI18N
            e.printStackTrace();
            THIS.dbServer.getConnectionPool().closeConnections();
            Naming.unbind(THIS.serverInfo.getBindString());
            throw new ServerExitError(e);
        }
    }
} // end of class
