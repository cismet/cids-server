package Sirius.server.middleware.impls.domainserver;

import Sirius.server.middleware.types.*;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.interfaces.domainserver.*;
import Sirius.server.localserver.method.*;
import Sirius.server.localserver.*;
import Sirius.server.naming.*;
import Sirius.server.newuser.*;
import Sirius.server.localserver.user.*;
import Sirius.server.search.*;
import Sirius.server.property.*;
import Sirius.server.*;
import Sirius.server.sql.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import Sirius.server.search.store.*;
import Sirius.server.localserver.query.querystore.*;
import Sirius.server.localserver.query.*;


import org.apache.log4j.*;

public class DomainServerImpl extends UnicastRemoteObject implements CatalogueService, MetaService, SystemService, UserService, QueryStore, SearchService { //ActionListener

    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    protected static DomainServerImpl THIS;
    // dbaccess of the mis (catalogue, classes and objects
    protected DBServer dbServer;
    // userservice of a localserver
    protected UserStore userstore;
    //executing the searchservice
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
    //this severs info object
    protected Server serverInfo;

    // protected ServerStatus status;
    public DomainServerImpl(ServerProperties properties) throws Throwable {
        // export object
        super(properties.getServerPort());

        try {
            this.properties = properties;
            String fileName;
            if ((fileName = properties.getLog4jPropertyFile()) != null && !fileName.equals("")) {
                PropertyConfigurator.configure(fileName);
            }

            try {
                this.myPort = properties.getServerPort();
                serverInfo = new Server(ServerType.LOCALSERVER, properties.getServerName(),
                        InetAddress.getLocalHost().getHostAddress(), properties.getRMIRegistryPort());
            } catch (Throwable e) {
                logger.debug("<LS> ERROR ::  Key serverPort is Missing!");

                this.myPort = 8912;
                serverInfo = new Server(ServerType.LOCALSERVER, properties.getServerName(),
                        InetAddress.getLocalHost().getHostAddress(), properties.getRMIRegistryPort());
            }

            dbServer = new DBServer(properties);

            userstore = dbServer.getUserStore();

            seeker = new Seeker(dbServer);

            queryStore = new Store(dbServer.getActiveDBConnection().getConnection(), properties);

            //All executable queries
            queryCache = new QueryCache(dbServer.getActiveDBConnection(), properties.getServerName());

            System.out.println("\n<LS> DBConnection: " + dbServer.getActiveDBConnection().getURL() + "\n");

            System.out.println(serverInfo.getRMIAddress());
            logger.info(serverInfo.getRMIAddress());
            System.out.println("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());
            logger.info("Info <LS> bind on RMIRegistry as: " + serverInfo.getBindString());
            Naming.bind(serverInfo.getBindString(), this);


            // status = new ServerStatus();

            register();

            THIS = this;

            logger.debug("Server Referenz " + this);

            //initFrame();
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public static final DomainServerImpl getServerInstance() {
        return THIS;
    }

    // public ServerStatus getStatus()
    //{return status;}
    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin CatalogueService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //---------------------------------------------------------------------------------
//    public NodeReferenceList getChildren(User user,int nodeID ) throws RemoteException
//    {
//        logger.debug("getchildren f\u00FCr" + nodeID);
//        
//        try
//        {	if(userstore.validateUser(user))
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
    public NodeReferenceList getChildren(Node node, User user) throws RemoteException {


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
//    //--------------------------------------------------------------------------------
//    public Node[] getParents(User user,int nodeID ) throws RemoteException
//    {
//        
//        try
//        {	//if(userstore.validateUser(user))
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

    //---------------------------------------------------------------------------------------------------
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
            //throw new RemoteException(e.getMessage());
        }

    }

    public Node addNode(Node node, Link parent, User user) throws RemoteException {
        try {
            return dbServer.getTree().addNode(node, parent, user);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }

    }

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
    //---------------------------------------------------------------------------------------------------
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

    //--------------------------------------------------------------------------------------------------------------------
//    public Node[] getObjectNodes(User user, String[] objectIDs) throws RemoteException
//    {
//        
//        Node[] nodes = null ;
//        
//        try
//        {
//            UserGroup ug = user.getUserGroup();
//            nodes = dbServer.getObjectNodes(objectIDs,ug).getLocalNodes();
//        }
//        catch(Throwable e)
//        { if (logger!=null) logger.error(e);throw new RemoteException(e.getMessage(),e);}
//        
//        return nodes;
//    }
//    
    //---------------------------------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------------------------------
    public MetaClass[] getClasses(User user) throws RemoteException {
        try {	//if(userstore.validateUser(user))
            return dbServer.getClasses(user.getUserGroup());

            // return new MetaClass[0];
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    //---------------------------------------------------------------------------------------------------
    public MetaClass getClass(User user, int classID) throws RemoteException {
        try {	//if(userstore.validateUser(user))
            return dbServer.getClass(user.getUserGroup(), classID);


            //return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    public MetaClass getClassByTableName(User user, String tableName) throws RemoteException {
        try {	//if(userstore.validateUser(user))
            return dbServer.getClassByTableName(user.getUserGroup(), tableName);
            //return null;
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    //---------------------------------------------------------------------------------------------------
    //????????????
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

    //---------------------------------------------------------------------------------------------------
    public MetaObject getObject(User user, String objectID) throws RemoteException {
        try {
            return dbServer.getObject(objectID, user.getUserGroup());
        } catch (Throwable e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }

    //---------------------------------------------------------------------------------------------------
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin   Metaservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    // retrieves a Meta data object( as Node)  referenced by a symbolic pointer to the MIS
    public Node getMetaObjectNode(User usr, int nodeID) throws RemoteException {
        int[] tmp = {nodeID};

        // single value directly referenced
        return getNodes(usr, tmp)[0];

    }

    // retrieves a Meta data object  referenced by a symbolic pointer to the MIS
    // MetaObject ersetzt DefaultObject
    public MetaObject getMetaObject(User usr, int objectID, int classID)
            throws RemoteException {

        return getObject(usr, objectID + "@" + classID);
    }

    // retrieves Meta data objects with meta data matching query (Search)
    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {

        try {
            //user spaeter erweitern
            return (MetaObject[]) seeker.search(query, new int[0], usr.getUserGroup(), 0).getObjects();

        } catch (Throwable e) {

            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage());
        }
    }
    // retrieves Meta data objects with meta data matching query (Search)

    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {

        //      return getMetaObject( usr,new Query(new  SystemStatement(true,-1,"",false,SearchResult.NODE,query),usr.getDomain() )  );

        MetaObject[] o = (MetaObject[]) getMetaObject(usr, new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, query), usr.getDomain()));

        return o;
    }

    /* MetaService - MetaJDBC*/
    // inserts metaObject in the MIS
    public MetaObject insertMetaObject(User user, MetaObject metaObject) throws RemoteException {
        if (logger != null) {
            logger.debug("<html>insert MetaObject for User :+:" + user + "  MO " + metaObject.getDebugString() + "</html>");
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

    public int insertMetaObject(User user, Query query) throws RemoteException {
        try {
            // XXX unterst\u00FCtzt keine batch queries
            //return metaJDBCService.insertMetaObject(user, query);

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

    public int updateMetaObject(User user, MetaObject metaObject) throws RemoteException {
        logger.debug("<html><body>update called for :+: <p>" + metaObject.getDebugString() + "</p></body></html>");
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
    public int update(User user, String metaSQL) throws RemoteException {

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

    public int deleteMetaObject(User user, MetaObject metaObject) throws RemoteException {

        logger.debug("delete called for" + metaObject);

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
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        logger.debug("user :: " + user + "  class " + c);
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
            throw new RemoteException("<LS> ", e);
        }

    }

    // retrieves Meta data objects with meta data matching query (Search)
    // Query not yet defined but will be MetaSQL
    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        return getMetaObjectNode(usr, new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, query), usr.getDomain()));
    }

    // retrieves Meta data objects with meta data matching query (Search)
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

    public MethodMap getMethods(User user) throws RemoteException {
        //  if(userstore.validateUser(user))
        return dbServer.getMethods(); // dbServer.getMethods(user.getuserGroup()); // instead

        //return new MethodMap();

    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields, String representationPattern) throws RemoteException {
        try {
            return dbServer.getObjectFactory().getAllLightweightMetaObjectsForClass(classId, user, representationFields, representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);
        }
    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classId, User user, String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory().getAllLightweightMetaObjectsForClass(classId, user, representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getAllLightweightMetaObjectsForClass(...)", ex);
        }
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields, String representationPattern) throws RemoteException {
        try {
            return dbServer.getObjectFactory().getLightweightMetaObjectsByQuery(classId, user, query, representationFields, representationPattern);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);
        }
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields) throws RemoteException {
        try {
            return dbServer.getObjectFactory().getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
        } catch (Throwable ex) {
            throw new RemoteException("Error on getLightweightMetaObjectsByQuery(...)", ex);
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
    public Sirius.util.image.Image[] getDefaultIcons() throws RemoteException {
        return properties.getDefaultIcons();
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End     SystemService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin     UserService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //---------------------------------------------------------------------------------------------------
    public boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException {

        try {
            return userstore.changePassword(user, oldPassword, newPassword);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("changePassword at remotedbserverimpl", e);
        }


    }
    //---------------------------------------------------------------------------------------------------

    public boolean validateUser(User user, String password) throws RemoteException {

        try {
            return userstore.validateUserPassword(user, password);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("Exception validateUser at remotedbserverimpl", e);
        }

    }
    //---------------------------------------------------------------------------------------------------

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////End   UserService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////Begin QueryStoreservice/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    public boolean delete(int id) throws RemoteException {
        return queryStore.delete(id);
    }

    public QueryData getQuery(int id) throws RemoteException {

        return queryStore.getQuery(id);
    }

    public Info[] getQueryInfos(UserGroup userGroup) throws RemoteException {
        return queryStore.getQueryInfos(userGroup);
    }

    public Info[] getQueryInfos(User user) throws RemoteException {
        return queryStore.getQueryInfos(user);
    }

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
    //    public Collection getAllSearchOptions(User user) throws RemoteException {
    //        return queryCache.getAllSearchOptions();
    //
    //    }
    //add single query root and leaf returns a query_id
    public int addQuery(String name, String description, String statement, int resultType, char isUpdate, char isBatch, char isRoot, char isUnion) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement, resultType, isUpdate, isBatch, isRoot, isUnion);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);
        }
    }

    public int addQuery(String name, String description, String statement) throws RemoteException {
        try {
            return queryCache.addQuery(name, description, statement);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);
        }
    }

    public boolean addQueryParameter(int queryId, int typeId, String paramkey, String description, char isQueryResult, int queryPosition) throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, typeId, paramkey, description, isQueryResult, queryPosition);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);
        }
    }

    //position set in order of the addition
    public boolean addQueryParameter(int queryId, String paramkey, String description) throws RemoteException {
        try {
            return queryCache.addQueryParameter(queryId, paramkey, description);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException("addQuery error", e);
        }
    }

    public HashMap getSearchOptions(User user) throws RemoteException {
        HashMap r = queryCache.getSearchOptions();
        logger.debug("in Domainserverimpl :: " + r);
        return r;

    }

    ////////////////////////////////////////////////////////////////////////////////////
    public SearchResult search(User user, int[] classIds, Query query) throws RemoteException {

        try {
            // user sp\u00E4ter erweitern
            return seeker.search(query, classIds, user.getUserGroup(), 0);
        } catch (Throwable e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
    /////////////////END SearchService/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //---------------------------------------------------------------------------------------------------
    protected void register() throws Throwable {

        int registered = 0;

        try {
            String lsName = serverInfo.getName();
            String ip = serverInfo.getIP();
            String[] registryIPs = properties.getRegistryIps();
            String rmiPort = serverInfo.getRMIPort();


            for (int i = 0; i < registryIPs.length; i++) {
                try {
                    nameServer = (NameServer) Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");
                    userServer = (UserServer) nameServer; //(UserServer) Naming.lookup("rmi://"+registryIPs[i]+"/userServer");

                    nameServer.registerServer(ServerType.LOCALSERVER, lsName, ip, rmiPort);

                    logger.info("\n<LS> registered at SiriusRegistry " + registryIPs[i] + " with " + lsName + "  " + ip);

                    UserStore userstore = dbServer.getUserStore();

                    userServer.registerUsers(userstore.getUsers());
                    userServer.registerUserGroups(userstore.getUserGroups());
                    userServer.registerUserMemberships(userstore.getMemberships());

                    registered++;
                    logger.info("<LS> users registered at SiriusRegistry" + registryIPs[i] + " with " + lsName + "  " + ip);
                } catch (NotBoundException nbe) {
                    System.err.println("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i]);
                    logger.error("<LS> No SiriusRegistry bound on RMIRegistry at " + registryIPs[i], nbe);
                } catch (RemoteException re) {
                    System.err.println("<LS> No RMIRegistry on " + registryIPs[i] + ", therefore SiriusRegistry could not be contacted");
                    logger.error("<LS> No RMIRegistry on " + registryIPs[i] + ", therefore SiriusRegistry could not be contacted", re);
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

    //---------------------------------------------------------------------------------------------------
    public void shutdown() throws Throwable {
        String ip = serverInfo.getIP();
        String lsName = properties.getServerName();
        String[] registryIPs = properties.getRegistryIps();
        String rmiPort = serverInfo.getRMIPort();


        for (int i = 0; i < registryIPs.length; i++) {
            try {
                nameServer = (NameServer) Naming.lookup("rmi://" + registryIPs[i] + "/nameServer");
                userServer = (UserServer) nameServer;// Naming.lookup("rmi://"+registryIPs[i]+"/userServer");

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

        //Naming.unbind("localServer");
        try {
            Naming.unbind(serverInfo.getBindString());

            if (properties.getStartMode().equalsIgnoreCase("simple")) {
                Naming.unbind("nameServer");
                Naming.unbind("callServer");
                Naming.unbind("userServer");
            }
            logger.debug("<LS> unbind for " + serverInfo.getBindString());

            // alle offenen Verbindungen schliessen
            THIS.dbServer.getConnectionPool().closeConnections();

            dbServer = null;

            // userservice of a localserver
            userstore = null;

            //executing the searchservice
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

        //        THIS=null;
        //
        //        System.gc(); //;-)

        throw new ServerExit("Server ist regul\u00E4r beendet worden");
    }

    //    //---------------------------------Interface ActionListener------------------------------------------------------------------
    //    public void actionPerformed(ActionEvent event) {
    //        try {
    //            shutdown();
    //        }
    //        catch(Throwable e) { throw new ServerExit("Server ist regul\u00E4r beendet worden",e); }
    //    }
    //---------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Throwable {

        ServerProperties properties = null;
        int rmiPort;

        if (args == null) {
            throw new ServerExitError("args == null keine Kommandozeilenparameter \u00FCbergeben (Configfile / port)");
        } else if (args.length < 1) {
            throw new ServerExitError("zu wenig Argumente");
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

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }

            // abfragen, ob schon eine  RMI Registry exitiert.
            java.rmi.registry.Registry rmiRegistry;
            try {
                rmiRegistry = LocateRegistry.getRegistry(rmiPort);
                // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest

                //                String[] list = rmiRegistry.list();
                //                int number = rmiRegistry.list().length;
                //System.out.println("<LS> RMIRegistry still exists...");
                //System.out.println("Info :: <LS> Already registered with RMIRegistry:");
                //                for (int i=0; i< number; i++)
                //                    System.out.println("\t"+ list[i]);
            } catch (Exception e) {
                // wenn nicht, neue Registry starten und auf portnummer setzen
                rmiRegistry = LocateRegistry.createRegistry(rmiPort);

                //System.out.println("<LS> create RMIRegistry...");
            }

            if (properties.getStartMode().equalsIgnoreCase("simple")) {

                new Sirius.server.registry.Registry(rmiPort);
                new Sirius.server.middleware.impls.proxy.StartProxy(args[0]);

            }


            new DomainServerImpl(new ServerProperties(args[0]));

            System.out.println("Info :: <LS>  !!!LocalSERVER started!!!!");
        } catch (Exception e) {
            System.err.println("Fehler beim Start des Domainservers :: " + e.getMessage());
            e.printStackTrace();
            THIS.dbServer.getConnectionPool().closeConnections();
            Naming.unbind(THIS.serverInfo.getBindString());
            throw new ServerExitError(e);
        }
    }
}// end of class

