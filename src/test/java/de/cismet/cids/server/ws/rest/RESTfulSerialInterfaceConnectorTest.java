/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExit;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.newuser.User;
import Sirius.server.property.ServerProperties;
import Sirius.server.registry.Registry;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.server.search.store.QueryData;
import Sirius.util.image.Image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import java.util.Properties;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class RESTfulSerialInterfaceConnectorTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final String ROOT_RESOURCE = "http://localhost:8011/callserver/binary/"; // NOI18N
    private static final String SERVER_CONFIG =
            "src/test/resources/Sirius/server/localserver/object/runtime.properties"; // NOI18N
    private static final String STARTMODE = "notSimple"; // NOI18N

    private static RESTfulSerialInterfaceConnector connector;
    private static Registry registry;
    private static StartProxy proxy;
    private static DomainServerImpl server;

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
        RESTfulSerialInterface.up(8011);
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
        try {
            RESTfulSerialInterface.down();
            server.shutdown();
            proxy.shutdown();
            registry.shutdown();
        } catch (final ServerExit serverExit) {
            // success...
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
//        admin = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "sb");
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    // TODO: why does this test fail when run with the other tests but succeeds otherwise ?
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
    public void testGetUser() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        assertNotNull(user);
        System.out.println("user: " + user);
    }


//    @Test
//    public void testGetDefaultIcons() throws Exception {
//        System.out.println("\nTEST: " + getCurrentMethodName());
//        final Image[] icons = connector.getDefaultIcons();
//        assertNotNull(icons);
//        for(final Image icon : icons){
//            System.out.println("icon: " + icon);
//        }
//    }

    @Test
    public void testGetDefaultIcons_String() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final Image[] icons = connector.getDefaultIcons("WUNDA_BLAU");
        assertNotNull(icons);
        for(final Image icon : icons){
            System.out.println("icon: " + icon);
        }

    }

    @Test
    public void testSearch() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final SearchResult result = connector.search(admin, new String[]{"1", "2", "3"}, new SearchOption[0]);
        assertNotNull(result);
        System.out.println("searchresult: " + result);
    }

    @Test
    public void testGetSearchOptions() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final HashMap result1 = connector.getSearchOptions(user, "WUNDA_BLAU");
        assertNotNull(result1);
        System.out.println("getsearchoptions: " + result1);

        final HashMap result2 = connector.getSearchOptions(user);
        assertNotNull(result2);
        System.out.println("getsearchoptions: " + result2);
    }

    @Test
    public void testAddQueryParameter() throws Exception {
        
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        
        System.out.println("\nTEST 1: " + getCurrentMethodName());
        Integer queryId = 1;
        Integer typeId = 1;
        Character isQueryResult = 'f';
        Integer queryPosition = 1;
        String paramKey = "testparam";
        String description = "A test parameter";
        final boolean result = connector.addQueryParameter(user, queryId,
                typeId, paramKey, description, isQueryResult, queryPosition);
        assertNotNull(result);
        System.out.println("addQueryParameter: " + result);

        System.out.println("\nTEST 2: " + getCurrentMethodName());
        queryId = 1;
        typeId = null;
        paramKey = "testparam2";
        description = "a second test parameter";
        isQueryResult = null;
        queryPosition = null;
//        final boolean result2 = connector.addQueryParameter(user, queryId,
//                typeId, paramKey, description, isQueryResult, queryPosition);
        final boolean result2 = connector.addQueryParameter(user, queryId,
                paramKey, description);
        assertNotNull(result2);
        System.out.println("addQueryParameter: " + result2);
    }

    @Test
    public void testAddQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        String name = "testquery";
        String description = "a simple statement";
        String statement = "Select * from cs_query";
        int resultType;
        char isUpdate;
        char isBatch;
        char isRoot;
        char isUnion;
        final int result = connector.addQuery(user, name, description, statement);
        assertNotNull(result);
        System.out.println("addQuery: " + result);

        System.out.println("\nTEST 2: " + getCurrentMethodName());
        name = "testquery2";
        description = "a simple statement";
        statement = "Select * from cs_query";
        resultType = 2;
        isUpdate = 't';
        isUnion = 't';
        isBatch = 't';
        isRoot ='t';
        final int result2 = connector.addQuery(user, name, description, statement,
                resultType, isUpdate, isBatch, isRoot, isUnion);
        assertNotNull(result2);
        System.out.println("addQuery: " + result2);
    }

    @Test
    public void testDelete() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final int id = 24;
        final String domain = "WUNDA_BLAU";
        final boolean result = connector.delete(id, domain);
        assertTrue(result);
        System.out.println("delete: " + result);
    }

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
     * TODO: storeQuery creates files which will currently not be removed by the
     * test framework. This problem has to be resolved before this test may be run.
     * @throws Exception
     */
    @Test
    @Ignore
    public void testStoreQuery() throws Exception {

        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        final QueryData data  = new QueryData("WUNDA_BLAU", "testQueryStore", STARTMODE, new byte[0]);
        final boolean result = connector.storeQuery(user, data);
        assertNotNull(result);
        System.out.println("getQuery: " + result);

    }

    /**
     * Check what this method actually does ... b4 testing
     * @throws Exception
     */
    @Test
    @Ignore
    public void testGetLightweightMetaObjectsByQuery() throws Exception {
        System.out.println("\nTEST: " + getCurrentMethodName());
        final User user = connector.getUser("WUNDA_BLAU", "Administratoren", "WUNDA_BLAU", "admin", "cismet");
        // just random values ... have to be checked wether they are senseful
        int classId = 1;
        String query = "Select * from cs_usr";
        String[] representationFields = new String[0];
        String representationPattern = "";
        final LightweightMetaObject[] result = connector.getLightweightMetaObjectsByQuery(
                classId, user, query, representationFields, representationPattern);
        assertNotNull(result);
        System.out.println("getQuery: " + result);

        System.out.println("\nTEST 2: " + getCurrentMethodName());
        final LightweightMetaObject[] result2 = connector.getLightweightMetaObjectsByQuery(
                classId, user, query, representationFields, representationPattern);
        assertNotNull(result2);
        System.out.println("getQuery: " + result2);
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
