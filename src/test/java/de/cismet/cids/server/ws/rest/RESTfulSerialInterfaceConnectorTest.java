/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;
import Sirius.server.sql.SystemStatement;

import Sirius.util.image.Image;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class RESTfulSerialInterfaceConnectorTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ROOT_RESOURCE = "http://localhost:9987/callserver/binary/";                               // NOI18N
    private static final String SERVER_CONFIG =
        "src/test/resources/Sirius/server/localserver/object/runtime.properties";                                         // NOI18N
    private static final String STARTMODE = "simple";                                                                     // NOI18N

    private static RESTfulSerialInterfaceConnector connector;
    private static Registry registry;
    private static StartProxy proxy;
    private static DomainServerImpl server;

    //~ Instance fields --------------------------------------------------------

    private User admin;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceManagerTest object.
     */
    public RESTfulSerialInterfaceConnectorTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Throwable {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);

        registry = new Sirius.server.registry.Registry(1099);
        proxy = StartProxy.getInstance(SERVER_CONFIG);
        RESTfulService.up(8011);
        final PropertiesWrapper pw = new PropertiesWrapper(SERVER_CONFIG);
        pw.setStartMode(STARTMODE);
        server = new DomainServerImpl(pw);
        connector = new RESTfulSerialInterfaceConnector(ROOT_RESOURCE);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        RESTfulService.down();
        try {
            server.shutdown();
        } catch (final ServerExit e) {
            // success
        } catch (final ServerExitError e) {
            System.err.println("exit error");
        }
        try {
            proxy.shutdown();
        } catch (final ServerExit e) {
            // success
        } catch (final ServerExitError e) {
            System.err.println("exit error");
        }
        try {
            registry.shutdown();
        } catch (final ServerExit serverExit) {
            // success
        } catch (final ServerExitError e) {
            System.err.println("exit error");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
        admin = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @After
    public void tearDown() throws Exception {
        final File queryStore = new File("querystore");
        if (queryStore.exists()) {
            FileUtils.deleteDirectory(queryStore);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUser() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        assertNotNull(user);
        System.out.println("user: " + user);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testChangePassword() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        assertNotNull(user);
        boolean changed = connector.changePassword(user, "cismet", "sbs");
        assertTrue(changed);
        user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sbs");
        assertNotNull(user);
        changed = connector.changePassword(user, "sbs", "cismet");
        assertTrue(changed);
        user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        assertNotNull(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetDomains() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final String[] domains = connector.getDomains();
        assertNotNull(domains);
        for (final String domain : domains) {
            System.out.println("domain: " + domain);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUserGroupNames() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Vector ugNames = connector.getUserGroupNames();
        assertNotNull(ugNames);
        for (final Object o : ugNames) {
            final String[] ug = (String[])o;
            System.out.println("ug: " + ug[0] + "@" + ug[1]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetUserGroupNames_String_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Vector ugNames = connector.getUserGroupNames("admin", "WUNDA_BLAU");
        assertNotNull(ugNames);
        for (final Object o : ugNames) {
            final String[] ug = (String[])o;
            System.out.println("ug: " + ug[0] + "@" + ug[1]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetDefaultIcons() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Image[] icons = connector.getDefaultIcons();
        assertNotNull(icons);
        for (final Image icon : icons) {
            System.out.println("icon: " + icon);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetDefaultIcons_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Image[] icons = connector.getDefaultIcons("WUNDA_BLAU");
        assertNotNull(icons);
        for (final Image icon : icons) {
            System.out.println("icon: " + icon);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testSearch() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final SearchResult result = connector.search(admin, new String[] { "1", "2", "3" }, new SearchOption[0]);
        assertNotNull(result);
        System.out.println("searchresult: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetSearchOptions() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final HashMap result1 = connector.getSearchOptions(user, "WUNDA_BLAU");
        assertNotNull(result1);
        System.out.println("getsearchoptions: " + result1);

        
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetSearchOptionsByUser() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        final HashMap result2 = connector.getSearchOptions(user);
        assertNotNull(result2);
        System.out.println("getsearchoptions: " + result2);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddQueryParameterAllParam() throws Exception {
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        System.out.println("\nTEST : " + getCurrentMethodName());
        
        final int queryId = 1;
        final int typeId = 1;
        final char isQueryResult = 'f';
        final int queryPosition = 1;
        final String paramKey = "testparam";
        final String description = "A test parameter";
        
        final boolean result = connector.addQueryParameter(
                user,
                queryId,
                typeId,
                paramKey,
                description,
                isQueryResult,
                queryPosition);
        assertNotNull(result);

        System.out.println("addQueryParameter: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddQueryParameter() throws Exception {
        System.out.println("\nTEST : " + getCurrentMethodName());

        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final int queryId = 1;
        final String paramKey = "testparam2";
        final String description = "a second test parameter";

        final boolean result2 = connector.addQueryParameter(user, queryId,
                paramKey, description);
        assertNotNull(result2);
        
        System.out.println("addQueryParameter: " + result2);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String name = "testquery";
        final String description = "a simple statement";
        final String statement = "Select * from cs_query";
        final int result = connector.addQuery(user, name, description, statement);
        assertNotNull(result);
        System.out.println("addQuery: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddQueryAllParam() throws Exception {
        System.out.println("\nTEST : " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String name = "testquery2";
        final String description = "a simple statement";
        final String statement = "Select * from cs_query";
        final int resultType = 2;
        final char isUpdate = 't';
        final char isUnion = 't';
        final char isBatch = 't';
        final char isRoot = 't';

        final int result = connector.addQuery(
                user,
                name,
                description,
                statement,
                resultType,
                isUpdate,
                isBatch,
                isRoot,
                isUnion);
        assertNotNull(result);
        System.out.println("addQuery: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final int id = 24;
        final String domain = "WUNDA_BLAU";
        final boolean result = connector.delete(id, domain);
        assertFalse(result);
        System.out.println("delete: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final int id = 17;
        final String domain = "WUNDA_BLAU";
        final QueryData result = connector.getQuery(id, domain);
        assertNotNull(result);
        System.out.println("getQuery: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testStoreQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final QueryData data = new QueryData("WUNDA_BLAU", "testQueryStore", STARTMODE, new byte[0]);
        final boolean result = connector.storeQuery(user, data);
        assertNotNull(result);
        System.out.println("storeQuery: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetLightweightMetaObjectsByQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        // just random values ... have to be checked wether they are senseful
        final int classId = 1;
        final String query = "Select * from cs_usr";
        final String[] representationFields = new String[0];

        final LightweightMetaObject[] result = connector.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields);
        assertNotNull(result);
        System.out.println("testGetLightweightMetaObjectsByQuery: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetLightweightMetaObjectsByQueryWithRepPattern() throws Exception {
        System.out.println("\nTEST : " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        // just random values ... have to be checked wether they are senseful
        final int classId = 1;
        final String query = "Select * from cs_usr";
        final String[] representationFields = new String[0];
        final String representationPattern = "";

        final LightweightMetaObject[] result = connector.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern);
        assertNotNull(result);
        System.out.println("testGetLightweightMetaObjectsByQuery: " + result);
    }

    /**
     * Check what this method actually does ... b4 testing
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetAllLightweightMetaObjectsForClass() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        // just random values ... have to be checked wether they are senseful
        final int classId = 1;
        final String[] representationFields = new String[0];

        final LightweightMetaObject[] result = connector.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields);
        assertNotNull(result);
        System.out.println("getAllLightweightMetaObjectsForClass: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetAllLightweightMetaObjectsForClassRepPattern() throws Exception {
        System.out.println("\nTEST : " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        // just random values ... have to be checked wether they are senseful
        final int classId = 1;
        final String[] representationFields = new String[0];
        final String representationPattern = "";

        final LightweightMetaObject[] result = connector.getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern);
        assertNotNull(result);
        System.out.println("getAllLightweightMetaObjectsForClass: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMethods_User() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        final MethodMap result = connector.getMethods(user);
        assertNotNull(result);
        System.out.println("getMethods: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMethods_User_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String localServerName = "WUNDA_BLAU";

        final MethodMap result = connector.getMethods(user, localServerName);
        assertNotNull(result);
        System.out.println("getMethods: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetClassTreeNodes_User() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        final Node[] result = connector.getClassTreeNodes(user);
        assertNotNull(result);
        System.out.println("getClassTreeNodes: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetClassTreeNodes_User_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";

        final Node[] result = connector.getClassTreeNodes(user, domain);
        assertNotNull(result);
        System.out.println("getClassTreeNodes: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetClass() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final int classId = 10;
        final String domain = "WUNDA_BLAU";

        final MetaClass result = connector.getClass(user, classId, domain);
        assertNotNull(result);
        System.out.println("getClass: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetClassByTableName() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final String tableName = "b_teil";

        final MetaClass result = connector.getClassByTableName(user, tableName, domain);
        assertNotNull(result);
        System.out.println("getClassTreeNodes: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetClasses() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";

        final MetaClass[] result = connector.getClasses(user, domain);
        assertNotNull(result);
        System.out.println("getClasses: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObject() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int objectID = 3;
        final int classID = 4;

        final MetaObject result = connector.getMetaObject(user, objectID, classID, domain);
        assertNotNull(result);
        System.out.println("getMetaObject: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObject_User_Query() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        
        final String queryString = "select 11, postleitzahl from a_stadt";
        final Query query = new Query(new SystemStatement(true, -1, "", false, SearchResult.OBJECT, queryString),
                domain);

        final MetaObject[] result = connector.getMetaObject(user, query);
        assertNotNull(result);

        for (MetaObject mo : result) {
            System.out.println("getMetaObject: " + mo);
        }
        
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObject_User_String() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";

        final String queryString = "select 11, postleitzahl from a_stadt";

        final MetaObject[] result = connector.getMetaObject(user, queryString);
        assertNotNull(result);

        for (MetaObject mo : result) {
            System.out.println("getMetaObject: " + mo);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testDeleteMetaObject() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int objectID = 3;
        final int classID = 4;
        final MetaObject mo = connector.getMetaObject(user, objectID, classID, domain);

        final int result = connector.deleteMetaObject(user, mo, domain);
        assertNotNull(result);
        System.out.println("deleteMetaObject: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testUpdate() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final String query = "SELECT * FROM cs_class";

        final int result = connector.update(user, query, domain);
        assertNotNull(result);
        System.out.println("update: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObjectNode_User_int_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeID = 2;
        final Node result = connector.getMetaObjectNode(user, nodeID, domain);
        assertNotNull(result);
        System.out.println("update: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObjectNode_User_String() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        
        final String query = "select 11, postleitzahl from a_stadt";
        
        final Node[] result = connector.getMetaObjectNode(user, query);
        assertNotNull(result);
        
        for (Node node : result) {
            System.out.println("update: " + node);
        }

    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObjectNode_User_Query() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final String queryString = "select 11, postleitzahl from a_stadt";
        final Query query = new Query(
                new SystemStatement(true, -1, "", false, SearchResult.NODE, queryString),
                domain);

        final Node[] result = connector.getMetaObjectNode(user, query);
        assertNotNull(result);

        for (Node node : result) {
            System.out.println("update: " + node);
        }

    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetInstance() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int classID = 10;
        final MetaClass mc = connector.getClass(user, classID, domain);
        final MetaObject result = connector.getInstance(user, mc);
        assertNotNull(result);

        System.out.println("getInstance: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddNode() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeID = 2;
        final Node node = connector.getMetaObjectNode(user, nodeID, domain);
        node.setName("new TestNode");
        final Link parent = new Link(nodeID, domain);
        final Node result = connector.addNode(node, parent, user);
        assertNotNull(result);
        System.out.println("addNode: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testAddLink() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeFromID = 2;
        final int nodeToID = 4;

        final Node nodeFrom = connector.getMetaObjectNode(user, nodeFromID, domain);
        final Node nodeTo = connector.getMetaObjectNode(user, nodeToID, domain);
        final boolean result = connector.addLink(nodeFrom, nodeTo, user);
        assertNotNull(result);
        assertTrue(result);
        System.out.println("addLink: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    // the underlying implementation has obviously never been used as it contains illegal sql
    @Test
    public void testDeleteLink() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeFromID = 2;
        final int nodeToID = 4;

        final Node nodeFrom = connector.getMetaObjectNode(user, nodeFromID, domain);
        final Node nodeTo = connector.getMetaObjectNode(user, nodeToID, domain);
        final boolean result = connector.deleteLink(nodeFrom, nodeTo, user);
        assertNotNull(result);
        assertTrue(result);
        System.out.println("deleteLink: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testDeleteNode() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeID = 4;
        final Node node = connector.getMetaObjectNode(user, nodeID, domain);
        final boolean result = connector.deleteNode(node, user);
        assertNotNull(result);
        assertTrue(result);
        System.out.println("deleteNode: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetQueryInfo_User() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        final Info[] result = connector.getQueryInfos(user);
        assertNotNull(result);
        System.out.println("getQueryInfo_User: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetQueryInfo_UserGroup() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final String domain = "WUNDA_BLAU";
        final UserGroup ug = new UserGroup(1, "Administratoren", domain);

        final Info[] result = connector.getQueryInfos(ug);
        assertNotNull(result);
        System.out.println("getQueryInfo_UserGroup: " + result);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetChildren_Node_User() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int nodeID = 2;
        final Node node = connector.getMetaObjectNode(user, nodeID, domain);

        final Node[] result = connector.getChildren(node, user);
        assertNotNull(result);
        for (final Node n : result) {
            System.out.println("getChildren " + n);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetRoots_User() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");

        final Node[] result = connector.getRoots(user);
        assertNotNull(result);
        for (final Node n : result) {
            System.out.println("getRoots " + n);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetRoots_User_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";

        final Node[] result = connector.getRoots(user, domain);
        assertNotNull(result);
        for (final Node n : result) {
            System.out.println("getRoots " + n);
        }
    }

     /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testUpdateMetaObject() throws Exception {

         System.out.println("\nTEST: " + getCurrentMethodName());

        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int objectID = 42329;
        final int classID = 11;

        final MetaObject mo = connector.getMetaObject(user, objectID, classID, domain);
        mo.getAttributeByFieldName("ORTSNAME").setValue("Cismethausen");
        final int result = connector.updateMetaObject(user, mo, domain);
        assertTrue(result > 0);

        System.out.println("updateMetaObject: " + result);

    }

     /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testInsertMetaObject() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());

        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int objectID = 42329;
        final int classID = 11;

        final MetaObject mo = connector.getMetaObject(user, objectID, classID, domain);
//        mo.setID(42330);
        final MetaObject result = connector.insertMetaObject(user, mo, domain);
        assertNotNull(result);

        System.out.println("insertMetaObject: " + result.getClassKey());
    }

     /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    @Ignore
    // could not determine how to specify the query object correctly
    public void testInsertMetaObjectByQuery() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());

        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final String domain = "WUNDA_BLAU";
        final int objectID = 42329;
        final int classID = 11;

        final String queryString = "Insert into b_teil (bezeichnung,id,bestandteile) values (\"welle\",1,4) ";
        final Query query = new Query(
                new SystemStatement(true, -1, "", false, 0, queryString), domain);

        final int result = connector.insertMetaObject(user, query, domain);
        assertNotNull(result);

        System.out.println("insertMetaObject: " + result);

    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class PropertiesWrapper extends ServerProperties {

        //~ Instance fields ----------------------------------------------------

        private transient String startMode;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PropertiesWrapper object.
         *
         * @param   configFile  DOCUMENT ME!
         *
         * @throws  FileNotFoundException  DOCUMENT ME!
         * @throws  IOException            DOCUMENT ME!
         */
        public PropertiesWrapper(final String configFile) throws FileNotFoundException, IOException {
            super(configFile);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public String getStartMode() {
            if (startMode == null) {
                return super.getStartMode();
            } else {
                return startMode;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  startMode  DOCUMENT ME!
         */
        public void setStartMode(final String startMode) {
            this.startMode = startMode;
        }
    }
}
