/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;

import Sirius.server.middleware.types.*;
import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.localserver.method.*;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.*;
import Sirius.server.naming.*;
import Sirius.server.newuser.*;

import java.net.*;

import Sirius.server.search.*;
import Sirius.server.observ.*;
import Sirius.server.property.*;

import java.util.*;

import Sirius.server.dataretrieval.*;

import Sirius.util.image.*;

import Sirius.server.search.store.*;

/**
 * Benoetigte Keys fuer configFile: registryIps<br>
 * serverName<br>
 * jdbcDriver *
 *
 * @version  $Revision$, $Date$
 */
public class ProxyImpl extends UnicastRemoteObject implements CatalogueService,
    MetaService,
    RemoteObserver,
    SystemService,
    UserService,
    DataService,
    QueryStore,
    SearchService,
    TransactionService {

    //~ Instance fields --------------------------------------------------------

    protected ServerProperties properties;
    // contains DomainServers
    protected java.util.Hashtable activeLocalServers;
    protected NameServer nameServer;
    protected UserServer userServer;
    String[] registryIps;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private CatalogueServiceImpl catService;
    private MetaServiceImpl metaService;
    private RemoteObserverImpl remoteObserver;
    private SystemServiceImpl systemService;
    private UserServiceImpl userService;
    private DataServiceImpl dataService;
    private QueryStoreImpl queryStore;
    private SearchServiceImpl searchService;
    private TransactionServiceImpl transactionService;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProxyImpl object.
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public ProxyImpl(ServerProperties properties) throws RemoteException {
        super(properties.getServerPort());

        try {
            this.properties = properties;
            this.registryIps = properties.getRegistryIps();

            nameServer = (NameServer)Naming.lookup("rmi://" + registryIps[0] + "/nameServer");

            userServer = (UserServer)nameServer; // Naming.lookup("rmi://"+registryIps[0]+"/userServer");

            activeLocalServers = new java.util.Hashtable(5);

            Server[] localServers = nameServer.getServers(ServerType.LOCALSERVER);
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> " + localServers.length + " LocalServer received from SiriusRegistry");
            }

            for (int i = 0; i < localServers.length; i++) {
                String address = localServers[i].getAddress();
                String name = localServers[i].getName();
                String lookupString = localServers[i].getRMIAddress();
                if (logger.isDebugEnabled()) {
                    logger.debug("<CS> lookup: " + lookupString);
                }

                Remote localServer = (Remote)Naming.lookup(lookupString);
                activeLocalServers.put(name, localServer);
            }

            register();
            registerAsObserver(registryIps[0]);

            initImplementations();
        } catch (RemoteException e) {
            logger.error(e);
            throw e;
        } catch (Exception e) {
            logger.error(e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    private void initImplementations() throws RemoteException {
        catService = new CatalogueServiceImpl(activeLocalServers);

        // Problem: localServers, Fkt  validateLocalServers(ls)
        metaService = new MetaServiceImpl(activeLocalServers, nameServer /*, localServers*/);

        // Problem: localServers
        remoteObserver = new RemoteObserverImpl(activeLocalServers, nameServer /*, localServers*/);

        systemService = new SystemServiceImpl(activeLocalServers, nameServer);

        userService = new UserServiceImpl(activeLocalServers, userServer);

        dataService = new DataServiceImpl(activeLocalServers);

        queryStore = new QueryStoreImpl(activeLocalServers, nameServer);

        searchService = new SearchServiceImpl(activeLocalServers, nameServer);

        transactionService = new TransactionServiceImpl(this);
    }
    // -------------------- remote methods-------------------------------------

    /**
     * CatalogueService Fkt-en.
     *
     * @param   node  DOCUMENT ME!
     * @param   usr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
// public Node[] getChildren(User usr, int nodeID, String localServerName)
// throws RemoteException
// {
// logger.debug("getChildren f\u00FCr"+ nodeID);
// return catService.getChildren(usr, nodeID, localServerName);
// }
    public Node[] getChildren(Node node, User usr) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getChildren f\u00FCr" + node);
        }
        return catService.getChildren(node, usr);
    }

//    public Node[] getParents(User usr, int nodeID, String localServerName)
//    throws RemoteException
//    {
//        return catService.getParents(usr, nodeID, localServerName);
//    }
    public Node[] getRoots(User user, String localServerName) throws RemoteException {
        return catService.getRoots(user, localServerName);
    }

    public Node[] getRoots(User user) throws RemoteException {
        return catService.getRoots(user);
    }

    public Node addNode(Node node, Link parent, User user) throws RemoteException {
        return catService.addNode(node, parent, user);
    }

    public boolean deleteNode(Node node, User user) throws RemoteException {
        return catService.deleteNode(node, user);
    }

    public boolean addLink(Node from, Node to, User user) throws RemoteException {
        return catService.addLink(from, to, user);
    }

    public boolean deleteLink(Node from, Node to, User user) throws RemoteException {
        return catService.deleteLink(from, to, user);
    }

//    public boolean copySubTree(Node root,User user) throws RemoteException
//    {return catService.copySubTree(root,user);}
    /**
     * /CatalogueService Fkt-en.
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    /**
     * /MetaService Fkt-en.
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getClassTreeNodes(User user, String localServerName) throws RemoteException {
        return metaService.getClassTreeNodes(user, localServerName);
    }

    public Node[] getClassTreeNodes(User user) throws RemoteException {
        return metaService.getClassTreeNodes(user);
    }

    public MetaClass getClass(User user, int classID, String localServerName) throws RemoteException {
        return metaService.getClass(user, classID, localServerName);
    }

    public MetaClass getClassByTableName(User user, String tableName, String localServerName) throws RemoteException {
        String lowerTableName = null;
        if (tableName != null) {
            lowerTableName = tableName.trim().toLowerCase();
        }
        return metaService.getClassByTableName(user, tableName, localServerName);
    }

    public MetaClass[] getClasses(User user, String localServerName) throws RemoteException {
        return metaService.getClasses(user, localServerName);
    }

    public String[] getDomains() throws RemoteException {
        return metaService.getDomains();
    }

    public Node getMetaObjectNode(User usr, int nodeID, String domain) throws RemoteException {
        return metaService.getMetaObjectNode(usr, nodeID, domain);
    }

    public MetaObject getMetaObject(User usr, int objectID, int classID, String domain) throws RemoteException {
        return metaService.getMetaObject(usr, objectID, classID, domain);
    }

    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        return metaService.getMetaObjectNode(usr, query);
    }

    public Node[] getMetaObjectNode(User usr, Query query) throws RemoteException {
        return metaService.getMetaObjectNode(usr, query);
    }

    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {
        return metaService.getMetaObject(usr, query);
    }

    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {
        return metaService.getMetaObject(usr, query);
    }

    public int deleteMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        return metaService.deleteMetaObject(user, metaObject, domain);
    }

    public int insertMetaObject(User user, Query query, String domain) throws RemoteException {
        return metaService.insertMetaObject(user, query, domain);
    }

    public MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        return metaService.insertMetaObject(user, metaObject, domain);
    }

    public int updateMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        return metaService.updateMetaObject(user, metaObject, domain);
    }

    public int update(User user, String query, String domain) throws RemoteException {
        return metaService.update(user, query, domain);
    }

    public MethodMap getMethods(User user) throws RemoteException {
        return metaService.getMethods(user);
    }

    public MethodMap getMethods(User user, String lsName) throws RemoteException {
        return metaService.getMethods(user, lsName);
    }

    /**
     * /MetaService Fkt-en.
     *
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    /**
     * SystemService Fkt-en.
     *
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Image[] getDefaultIcons(String lsName) throws RemoteException {
        return systemService.getDefaultIcons(lsName);
    }

    public Image[] getDefaultIcons() throws RemoteException {
        return systemService.getDefaultIcons();
    }

    /**
     * /SystemService Fkt-en.
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
    /**
     * UserService Fkt-en.
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
// public User getUser(
// String userLsName,
// String userName,
// String userGroupLsName,
// String userGroupName,
// String password) throws RemoteException, UserException
// {
    // ACHTUNG war falsch sortiert. wie konnte das jemals gehen
    public User getUser(
            String userGroupLsName,
            String userGroupName,
            String userLsName,
            String userName,
            String password) throws RemoteException, UserException {
        return userService.getUser(
                userLsName,
                userName,
                userGroupLsName,
                userGroupName,
                password);
    }

    public Vector getUserGroupNames() throws RemoteException {
        return userService.getUserGroupNames();
    }

    public Vector getUserGroupNames(String userName, String lsHome) throws RemoteException {
        return userService.getUserGroupNames(userName, lsHome);
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException,
        UserException {
        return userService.changePassword(user, oldPassword, newPassword);
    }

    /**
     * /UserService Fkt-en.
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    /**
     * DataService Fkt-en.
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    public DataObject getDataObject(User user, MetaObject metaObject) throws RemoteException, DataRetrievalException {
        return dataService.getDataObject(user, metaObject);
    }

    public DataObject[] getDataObject(User user, Query query) throws RemoteException, DataRetrievalException {
        return dataService.getDataObject(user, query);
    }

    /**
     * /DataService Fkt-en.
     *
     * @param   obs  DOCUMENT ME!
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    // ----------------------------------------------------------------------------------
    /**
     * RemoteObserver Fkt-en.
     *
     * @param   obs  DOCUMENT ME!
     * @param   arg  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
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
    public void update(RemoteObservable obs, java.lang.Object arg) throws RemoteException {
        remoteObserver.update(obs, arg);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected void register() throws Exception {
        nameServer.registerServer(
            ServerType.CALLSERVER,
            properties.getServerName(),
            InetAddress.getLocalHost().getHostAddress(),
            "1099"); // localSERvername in callSERname
    }

    /**
     * DOCUMENT ME!
     *
     * @param  siriusRegistryIP  DOCUMENT ME!
     */
    void registerAsObserver(String siriusRegistryIP) {
        if (logger.isDebugEnabled()) {
            logger.debug(" Info <CS> registerAsObserver:: " + siriusRegistryIP);
        }
        try {
            RemoteObservable server = (RemoteObservable)Naming.lookup("rmi://" + siriusRegistryIP + "/nameServer");
            server.addObserver(this);
            if (logger.isDebugEnabled()) {
                logger.debug("Info <CS> added as observer");
            }
        } catch (NotBoundException nbe) {
            System.err.println("<CS> No SiriusRegistry bound on RMIRegistry at " + siriusRegistryIP);
            logger.error("<CS> No SiriusRegistry bound on RMIRegistry at " + siriusRegistryIP, nbe);
        } catch (RemoteException re) {
            System.err.println("<CS> RMIRegistry on " + siriusRegistryIP + " could not be contacted");
            logger.error("<CS> RMIRegistry on " + siriusRegistryIP + " could not be contacted", re);
        } catch (Exception e) {
            logger.error(e);
        }
    }
    /**
     * ---------------------------------------------------------------------------------------------------
     *
     * @param  siriusRegistryIP  DOCUMENT ME!
     */
    void unregisterAsObserver(String siriusRegistryIP) {
        try {
            RemoteObservable server = (RemoteObservable)Naming.lookup("rmi://" + siriusRegistryIP + "/nameServer");
            server.deleteObserver(this);
            if (logger.isDebugEnabled()) {
                logger.debug("Info <CS> removed as observer");
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * /RemoteObserver Fkt-en.
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    /**
     * /QueryStore Fkt-en.
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean delete(int id, String domain) throws RemoteException {
        return queryStore.delete(id, domain);
    }

    public QueryData getQuery(int id, String domain) throws RemoteException {
        return queryStore.getQuery(id, domain);
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

    /**
     * /QueryStore Fkt-en.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    /**
     * /SearchService Fkt-en.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    //
    // public Collection getAllSearchOptions(User user) throws RemoteException
    // {return searchService.getAllSearchOptions(user);}
    //
    // public Collection getAllSearchOptions(User user,String domain) throws RemoteException
    // {return searchService.getAllSearchOptions(user,domain);}
    public HashMap getSearchOptions(User user) throws RemoteException {
        return searchService.getSearchOptions(user);
    }

    public HashMap getSearchOptions(User user, String domain) throws RemoteException {
        return searchService.getSearchOptions(user, domain);
    }

    public SearchResult search(User user, String[] classIds, SearchOption[] searchOptions) throws RemoteException {
        return searchService.search(user, classIds, searchOptions);
    }

    public int addQuery(
            User user,
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws RemoteException {
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

    public int addQuery(User user, String name, String description, String statement) throws RemoteException {
        return searchService.addQuery(user, name, description, statement);
    }

    public boolean addQueryParameter(
            User user,
            int queryId,
            int typeId,
            String paramkey,
            String description,
            char isQueryResult,
            int queryPosition) throws RemoteException {
        return searchService.addQueryParameter(
                user,
                queryId,
                typeId,
                paramkey,
                description,
                isQueryResult,
                queryPosition);
    }

    // position set in order of the addition
    public boolean addQueryParameter(User user, int queryId, String paramkey, String description)
        throws RemoteException {
        return searchService.addQueryParameter(user, queryId, paramkey, description);
    }

    /**
     * SearchService Fkt-en.
     *
     * @param   transactions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    /**
     * TransactionService Fkt-en.
     *
     * @param   transactions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int executeTransactionList(ArrayList transactions) throws RemoteException {
        return transactionService.executeTransactionList(transactions);
    }

    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        return metaService.getInstance(user, c);
    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern);
    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields) throws RemoteException {
        return metaService.getAllLightweightMetaObjectsForClass(classId, user, representationFields);
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern);
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws RemoteException {
        return metaService.getLightweightMetaObjectsByQuery(classId, user, query, representationFields);
    }
    /*************************  ende TransactionService Fkt-en *****************************/
    // ---------------------start callserver main
} // end class
