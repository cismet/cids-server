package de.cismet.cids.integrationtests;

import Sirius.server.MetaClassCache;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.OfflineMetaClassCacheService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.util.image.Image;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static de.cismet.cids.integrationtests.TestEnvironment.INTEGRATIONBASE_CONTAINER;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * Integration Test for Legacy Rest Interface (Broker)
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@RunWith(DataProviderRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LegacyRESTfulInterfaceTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(LegacyRESTfulInterfaceTest.class);

    /**
     * The static cids Reference Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initcidsRefContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static DockerComposeContainer dockerEnvironment = null;

    protected static final Properties PROPERTIES = TestEnvironment.getProperties();

    protected static Connection jdbcConnection = null;
    protected static RESTfulSerialInterfaceConnector connector = null;
    protected static User user = null;
    protected static boolean connectionFailed = false;
    protected static Map<String, Integer> dbEntitiesCount = new HashMap<String, Integer>();
    protected static Map<String, MetaObject> newMetaObjects = new HashMap<String, MetaObject>();
    protected static Map<String, List<Integer>> metaObjectIds = new HashMap<String, List<Integer>>();

    /**
     * This ClassRule is executed only once before any test run (@Test method)
     * starts. It checks whether the cids testing enviroment is enabled or not.
     * If the testing enviroment is enabled, it creates a new managed
     * ComposeContainer that provides access to a dockerized cids
     * integrationtests system.
     *
     * @return ComposeContainer or dummy ClassRule
     */
    @ClassRule
    public static TestRule initCidsRefContainer() {
        LOGGER.info("@ClassRule initCidsRefContainer(): activating cids Integration Tests: "
                + !TestEnvironment.isIntegrationTestsDisabled());

        // check if integration tests are enabled in current maven profile
        if (TestEnvironment.isIntegrationTestsDisabled()) {
            // return Dummy ClassRule that skips the test
            return TestEnvironment.SKIP_INTEGRATION_TESTS;
        } else {

            try {
                // create new ComposeContainer
                dockerEnvironment = TestEnvironment.createDefaultDockerEnvironment();
                // Important: return the container instance. Otherwise start/stop
                // of the container is not called!
                return dockerEnvironment;
            } catch (Exception ex) {
                LOGGER.fatal("could not initialize docker interation test environment, integration tests disabled!", ex);
                return TestEnvironment.SKIP_INTEGRATION_TESTS;
            }
        }
    }

    /**
     * Executed only once after @ClassRule. Assumes that testcontainers docker
     * compose started the required cids containers and checks if the cids
     * services are up and rennung. Initializes legacy and rest connectors.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.debug("beforeClass(): cids Integration Tests activated, loading properties");
        Assert.assertNotNull("TestEnvironment created", PROPERTIES);

        // check container creation succeeded 
        Assert.assertNotNull("cidsRefContainer sucessfully created", dockerEnvironment);

        Assert.assertFalse("connecting to cids integrationtest docker containers could not be established",
                connectionFailed);

        try {
            // connect to Broker (legacy server) -------------------------------
            final String brokerUrl = TestEnvironment.getCallserverUrl(dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                    Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))));

            // check jdbcConnection to legacy server (broker f.k.a. callserver)
            if (!TestEnvironment.pingHostWithPost(dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                    Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    "/callserver/binary/getUserGroupNames",
                    12)) {
                connectionFailed = true;
                throw new Exception(brokerUrl + " did not answer after 12 retries");
            }

            LOGGER.info("connecting to cids reference docker legacy server: " + brokerUrl);
            connector = new RESTfulSerialInterfaceConnector(brokerUrl);

            // authenticate user in rest connector
            user = connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("password", "cismet"));

            LOGGER.info("sucessfully authenticated cids user: " + user.toString());

            // connect to integration base (postgred DB) -----------------------
            final String integrationBaseUrl = "jdbc:postgresql://"
                    + dockerEnvironment.getServiceHost(INTEGRATIONBASE_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("integrationbase.port", "5432")))
                    + ":"
                    + dockerEnvironment.getServicePort(INTEGRATIONBASE_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("integrationbase.port", "5432")))
                    + "/"
                    + PROPERTIES.getProperty("integrationbase.dbname", "cids_reference");

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                LOGGER.error("JDBC Driver 'org.postgresql.Driver' not available", e);
                throw e;
            }

            LOGGER.info("connecting to cids reference integration base postgres db: " + integrationBaseUrl);
            jdbcConnection = DriverManager.getConnection(
                    integrationBaseUrl,
                    PROPERTIES.getProperty("integrationbase.username", "postgres"),
                    PROPERTIES.getProperty("integrationbase.password", "postgres"));

        } catch (Exception e) {

            LOGGER.error("Unexpected exception during Global Test initialisation :" + e.getMessage(), e);
            throw e;
        }
    }

    @AfterClass
    public static void afterClass() {
        try {
            if (jdbcConnection != null) {
                jdbcConnection.close();
            }
        } catch (Exception ex) {
            LOGGER.error("could not close DB connection", ex);
        }
    }

    @Before
    public void beforeTest() throws Exception {
        try {
            Assert.assertNotNull("cids integration base connection successfully established", jdbcConnection);

            Assert.assertNotNull("cids legacy server connection successfully established", connector);
            Assert.assertNotNull("user authenticated", user);
        } catch (AssertionError ae) {
            LOGGER.error("test initialisation failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during test initialisation: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="DATA PROVIDERS ----------------------------------------------------------">
    @DataProvider
    public final static String[] getMetaClassTableNames() throws Exception {

        if (TestEnvironment.isIntegrationTestsEnabled()) {
            final Collection<MetaClass> metaClasses
                    = OfflineMetaClassCacheService.getInstance().getAllClasses(
                            PROPERTIES.getProperty("userDomain", "CIDS_REF")).values();

            final String[] metaClassTableNames = new String[metaClasses.size()];
            int i = 0;
            for (MetaClass metaClass : metaClasses) {
                metaClassTableNames[i] = metaClass.getTableName();
                i++;
            }
            return metaClassTableNames;

        } else {
            return new String[]{"THIS_TABLE_DOES_NOT_EXIST"};
        }
    }

    /**
     * This is madness: @DataProviderClass does not support MetaClass, not
     * Interger Arrays not Integer Lists:
     * initializationError(de.cismet.cids.integrationtests.RESTfulInterfaceTest)
     * Time elapsed: 0.005 sec java.lang.Exception: Dataprovider method
     * 'getMetaClassIds' must either return Object[][], Object[], String[],
     * List<List<Object>> or List<Object>
     *
     * has to return String[]
     *
     * @return String[]
     * @throws Exception
     */
    @DataProvider
    public final static String[] getMetaClassIds() throws Exception {

        if (TestEnvironment.isIntegrationTestsEnabled()) {
            final Collection<MetaClass> metaClasses
                    = OfflineMetaClassCacheService.getInstance().getAllClasses(
                            PROPERTIES.getProperty("userDomain", "CIDS_REF")).values();

            // 'MetaClass' is not supported as parameter type of test methods. 
            // Supported types are primitive types and their wrappers, case-sensitive 
            // 'Enum' values, 'String's, and types having a single 'String' parameter constructor.
            final String[] metaClassIds = new String[metaClasses.size()];
            int i = 0;
            for (MetaClass metaClass : metaClasses) {
                metaClassIds[i] = String.valueOf(metaClass.getId());
                i++;
            }
            return metaClassIds;

        } else {
            return new String[]{"-1"};
        }
    }

    @DataProvider
    public final static String[] getConfigAttributes() {
        return new String[]{
            "csa://bandwidthTest",
            "csa://downloadFile",
            "csa://httpTunnelAction",
            "csa://passwordSwitcherAdminAction",
            "csa://testAction",
            "csa://webDavTunnelAction"
        };
    }

    // </editor-fold>
    @Test
    @UseDataProvider("getMetaClassTableNames")
    public void test00integrationBase00countDbEntities(final String tableName) throws Exception {
        LOGGER.debug("[00.00] testing countDbEntities(" + tableName + ")");

        try {
            final int count = RESTfulInterfaceTest.countDbEntities(jdbcConnection, tableName);
            if (!tableName.equalsIgnoreCase("URL_BASE") && !tableName.equalsIgnoreCase("URL")) {
                Assert.assertTrue(tableName + " entities available in integration base", count > 0);
            }

            Assert.assertNull(tableName + " entities counted only once",
                    dbEntitiesCount.put(tableName, count));

            LOGGER.info("countDbEntities(" + tableName + ") test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("countDbEntities(" + tableName + ") test failed with: "
                    + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during countDbEntities(" + tableName + "): "
                    + ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("countDbEntities(" + tableName + ") test passed!");
    }

    // SERVER CORE / INFRASTRUCTURE TESTS --------------------------------------
    @Test
    public void test01server00getDefaultIcons() throws Exception {
        LOGGER.debug("[01.00] testing getDefaultIcons()");
        final String domain = PROPERTIES.getProperty("userDomain", "CIDS_REF");

        final Image[] defaultIcons = connector.getDefaultIcons();
        final Image[] defaultIconsForDomain = connector.getDefaultIcons(domain);

        try {
            Assert.assertNotNull("default Icons array not null", defaultIcons);

            // FIXME: CIDS_REF Server Configuration should contain default icons
            // see https://github.com/cismet/docker-volumes/issues/3
            // java.lang.Exception: file is not an Directory in Sirius.server.property.ServerProperties.getDefaultIcons(ServerProperties.java:721)
            //Assert.assertTrue("default Icons array not empty", defaultIcons.length > 0);
            Assert.assertNotNull("default Icons array for domain '" + domain + "' not null",
                    defaultIconsForDomain);

            //Assert.assertTrue("default Icons array for domain '" + domain + "' not empty",
            //        defaultIconsForDomain.length > 0);
            Assert.assertEquals("default Icons array for domain '" + domain + "' matches size",
                    defaultIcons.length, defaultIconsForDomain.length);

            LOGGER.info("getDefaultIcons() test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("getDefaultIcons() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getDefaultIcons(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test01server01getDomains() throws Exception {
        LOGGER.debug("[01.01] testing getDomains()");

        try {

            final String domain = PROPERTIES.getProperty("userDomain", "CIDS_REF");
            final String[] domains = connector.getDomains();

            Assert.assertNotNull("domains array not null", domains);
            Assert.assertTrue("domains array not empty", domains.length > 0);
            Assert.assertTrue("only one domain registered", domains.length == 1);
            Assert.assertArrayEquals("domains array matches",
                    new String[]{domain}, domains);

        } catch (AssertionError ae) {
            LOGGER.error("getDomains() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getDomains(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // USER SERVICE TESTS ------------------------------------------------------
    @Test
    public void test02userService00changePasswordSuccess() throws Exception {
        LOGGER.debug("[02.00] testing changePasswordSuccess()");
        final String newPassword = "DADhejPYEDtym:8hej54umVEB0hag25y";
        final String oldPassword = PROPERTIES.getProperty("password", "cismet");

        try {

            Assert.assertTrue("password of user changed",
                    connector.changePassword(user, oldPassword, newPassword));

            Assert.assertTrue("password of user changed",
                    connector.changePassword(user, newPassword, oldPassword));

            LOGGER.info("changePasswordSuccess() passed!");

        } catch (AssertionError ae) {
            LOGGER.error("changePasswordSuccess() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during changePasswordSuccess(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test02userService01changePasswordError() throws Exception {
        LOGGER.debug("[02.01] testing changePasswordError()");

        try {

            Assert.assertFalse(connector.changePassword(user, "wrong_password", "wrong_password"));
            LOGGER.info("changePasswordError() passed!");
        } catch (AssertionError ae) {
            LOGGER.error("changePasswordError() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during changePasswordError(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test(expected = UserException.class)
    public void test02userService02getUserPasswordError() throws Exception {

        LOGGER.debug("[02.02] testing getUserPasswordError()");

        try {

            connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("username", "admin"),
                    "wrong_password");
        } catch (UserException ex) {
            LOGGER.debug(ex.getClass(), ex);
            LOGGER.info("getUserPasswordError() test passed!");
            throw ex;
        } catch (AssertionError ae) {
            LOGGER.error("getUserPasswordError() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getUserPasswordError(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test(expected = UserException.class)
    public void test02userService03getUserDomainError() throws Exception {

        LOGGER.debug("[02.03] testing getUserDomainError()");

        try {
            connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    "WRONG_DOMAIN",
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("password", "cismet"));
        } catch (UserException ex) {
            LOGGER.debug(ex.getClass(), ex);
            LOGGER.info("getUserDomainError() passed!");
            throw ex;
        } catch (AssertionError ae) {
            LOGGER.error("getUserDomainError() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getUserDomainError(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test02userService04getDomains() throws Exception {
        LOGGER.debug("[02.04] testing getDomains()");

        try {

            final String domains[] = connector.getDomains();

            Assert.assertTrue("one domain available", domains.length == 1);

            Assert.assertEquals("domain matches from properties",
                    PROPERTIES.getProperty("domain", "CIDS_REF"), domains[0]);

            Assert.assertEquals("domain matches from user",
                    user.getDomain(), domains[0]);

            LOGGER.info("getDomains() test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getDomains() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getDomains(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test02userService05getUserGroupNames() throws Exception {
        LOGGER.debug("[02.05] testing getUserGroupNames");

        try {

            Vector userGroupNames = connector.getUserGroupNames();
            Assert.assertTrue("user groups available on server", userGroupNames.size() > 0);

            userGroupNames = connector.getUserGroupNames(
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"));

            Assert.assertTrue("user groups for user available on server", userGroupNames.size() > 0);

            Assert.assertEquals("usergroup matches from properties",
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    ((String[]) userGroupNames.get(0))[0]);

            Assert.assertEquals("usergroup matches from user",
                    user.getUserGroup().getName(),
                    ((String[]) userGroupNames.get(0))[0]);

            Assert.assertEquals("usergroup domain matches from properties",
                    PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    ((String[]) userGroupNames.get(0))[1]);

            Assert.assertEquals("usergroup domain matches from user",
                    user.getUserGroup().getDomain(),
                    ((String[]) userGroupNames.get(0))[1]);

            LOGGER.info("getUserGroupNames test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getUserGroupNames() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getUserGroupNames(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test02userService06getUserGroupNamesError() throws Exception {
        LOGGER.debug("[02.06] testing getUserGroupNamesError()");

        try {

            Vector userGroupNames = connector.getUserGroupNames(
                    "does-not-exist",
                    "does-not-exist");

            Assert.assertTrue("no groups found for wrong user and domain",
                    userGroupNames.isEmpty());
            LOGGER.info("getUserGroupNamesError() test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getUserGroupNames() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getUserGroupNames(): " + ex.getMessage(), ex);
            throw ex;
        }

    }

    @Test
    @UseDataProvider("getConfigAttributes")
    public void test02userService07hasConfigAttribute(final String configAttribute) throws Exception {
        LOGGER.debug("[02.07] testing hasConfigAttribute(" + configAttribute + ")");

        try {

            final boolean hasConfigAttribute = connector.hasConfigAttr(user, configAttribute);
            Assert.assertTrue("user has config attribute '" + configAttribute + "'", hasConfigAttribute);
            LOGGER.info("hasConfigAttribute(" + configAttribute + ") test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("hasConfigAttribute(" + configAttribute + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during hasConfigAttribute(" + configAttribute + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getConfigAttributes")
    public void test02userService08getConfigAttribute(final String configAttribute) throws Exception {
        LOGGER.debug("[02.08] testing getConfigAttribute(" + configAttribute + ")");

        try {

            final String userConfigAttribute = connector.getConfigAttr(user, configAttribute);
            Assert.assertNotNull("user has config attribute '" + configAttribute + "'", userConfigAttribute);
            Assert.assertEquals("config attribute '" + configAttribute + "' is true", userConfigAttribute, "true");
            LOGGER.info("getConfigAttribute(" + configAttribute + ") test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getConfigAttribute(" + configAttribute + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getConfigAttribute(" + configAttribute + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // CLASS SERVICE TEST ------------------------------------------------------
    @Test
    public void test03classService00getClasses() throws Exception {
        LOGGER.debug("[03.00] testing getClasses");

        try {

            final MetaClass[] metaClasses = connector.getClasses(user, user.getDomain());

            Assert.assertNotNull("meta classes array not null", metaClasses);
            Assert.assertTrue("meta classes array not empty", metaClasses.length > 0);
            Assert.assertEquals("meta classes array size matches local classes",
                    dbEntitiesCount.size(), metaClasses.length);

            MetaClassCache.getInstance().setAllClasses(metaClasses, user.getDomain());

            Assert.assertEquals("meta classes array size matches meta class cache size",
                    MetaClassCache.getInstance().getAllClasses(user.getDomain()).size(), metaClasses.length);

            LOGGER.info("getClasses test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("getClasses() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getClasses(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassTableNames")
    public void test03classService01getClassByTableName(final String tableName) throws Exception {
        LOGGER.debug("[03.01] testing getClassByTableName(" + tableName + ")");
        try {
            final MetaClass metaClass = connector.getClassByTableName(user, tableName.toLowerCase(), user.getDomain());
            Assert.assertNotNull("meta class '" + tableName + "' not null", metaClass);
            LOGGER.info("getClassByTableName(" + tableName + ") test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getClassByTableName(" + tableName + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getClassByTableName(" + tableName + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassIds")
    public void test03classService02getClass(final Integer classId) throws Exception {
        LOGGER.debug("[03.02] testing getClass(" + classId + ")");
        try {
            final MetaClass metaClass = connector.getClass(user, classId, user.getDomain());
            Assert.assertNotNull("meta class '" + classId + "' not null", metaClass);
            LOGGER.info("getClass(" + classId + ") test passed!");
        } catch (AssertionError ae) {
            LOGGER.error("getClass(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getClass(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * ObjectFactory().getInstance() seems to be broken. See
     * https://github.com/cismet/cids-server/issues/159
     *
     * @param classId
     * @throws Exception
     */
    @Test
    @UseDataProvider("getMetaClassIds")
    public void test03classService03getInstance(final Integer classId) throws Exception {
        LOGGER.debug("[03.03] testing getInstance(" + classId + ")");
        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);
            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);

            // FIXME: ObjectFactory().getInstance() fails with NPE for Classes SPH_SPIELHALLE and SPH_Betreiber
            //            final MetaObject metaObjectFromService = connector.getInstance(user, metaClass);
            //            Assert.assertNotNull("new meta object of meta class '" + classId + "' from service not null",
            //                    metaObjectFromService);
            final MetaObject metaObjectFromClass = metaClass.getEmptyInstance();
            Assert.assertNotNull("new meta object of meta class '" + classId + "' from meta class not null",
                    metaObjectFromClass);

            // FIXME:  MetaObject].getPropertyString()  does not match!
            // ID=[] name= > but was: ID=[-1] name
            //this.compareMetaObjects(metaObjectFromService, metaObjectFromClass);
            LOGGER.info("getInstance(" + classId + ") test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("getInstance(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getInstance(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // OBJECT SERVICE TEST -----------------------------------------------------
    /**
     * Test getMetaObjects by uery and getMetaObject by id
     *
     * @param classId
     * @throws Exception
     */
    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService00getMetaObjects(final Integer classId) throws Exception {
        LOGGER.debug("[04.00] testing getMetaObjects(" + classId + ")");
        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final int count = dbEntitiesCount.get(metaClass.getTableName());

            final String query = "SELECT " + metaClass.getID() + ", " + metaClass.getTableName() + "."
                    + metaClass.getPrimaryKey() + " FROM " + metaClass.getTableName() + " ORDER BY "
                    + metaClass.getTableName() + "."
                    + metaClass.getPrimaryKey() + " ASC";

            final MetaObject[] metaObjects = connector.getMetaObject(user, query);
            Assert.assertEquals(count + " '" + metaClass.getTableName() + "' entities in Integration Base",
                    count, metaObjects.length);

            final ArrayList metaObjectIdList = new ArrayList<Integer>(count);
            metaObjectIds.put(metaClass.getTableName().toLowerCase(), metaObjectIdList);

            for (int i = 0; i < metaObjects.length; i++) {
                Assert.assertNotNull("meta object #" + i + "/" + count + " for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") from meta class cache not null", metaObjects[i]);
                final MetaObject metaObject = connector.getMetaObject(
                        user, metaObjects[i].getId(), classId, user.getDomain());
                Assert.assertNotNull("meta object #" + i + "/" + count + " (id:" + metaObjects[i].getId() + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                        metaObject);

                this.compareMetaObjects(metaObjects[i], metaObject, false, false, false);

                metaObjectIdList.add(metaObject.getID());
            }

            LOGGER.info("getMetaObjects(" + classId + ") test passed!");

        } catch (AssertionError ae) {
            LOGGER.error("getMetaObjects(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getMetaObjects(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService01insertMetaObject(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.01] testing insertMetaObject(" + classId + ")");
                final int expectedCount = dbEntitiesCount.get(metaClass.getTableName()) + 1;

                final MetaObject newMetaObject = metaClass.getEmptyInstance();
                Assert.assertNotNull("new meta object of meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not null",
                        newMetaObject);

                final MetaObject insertedMetaObject = connector.insertMetaObject(user, newMetaObject, user.getDomain());
                Assert.assertNotNull("inserted meta object of meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not null",
                        insertedMetaObject);

                newMetaObjects.put(metaClass.getTableName(), insertedMetaObject);

                final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, metaClass.getTableName());
                Assert.assertEquals(expectedCount + " '" + metaClass.getTableName() + "' entities in Integration Base",
                        expectedCount, actualCount);

                this.compareMetaObjects(newMetaObject, insertedMetaObject, false, true, false);

                LOGGER.info("insertMetaObject(" + classId + ") test passed! New id is " + insertedMetaObject.getID());
            }

        } catch (AssertionError ae) {
            LOGGER.error("insertMetaObject(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during insertMetaObject(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService02deleteMetaObject(final Integer classId) throws Exception {

        try {

            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {
                LOGGER.debug("[04.02] testing deleteMetaObject(" + classId + ")");

                final int expectedCount = dbEntitiesCount.get(metaClass.getTableName());
                Assert.assertTrue("new '" + metaClass.getTableName() + "' (id:" + classId + ") entity created",
                        newMetaObjects.containsKey(metaClass.getTableName()));

                final MetaObject metaObject = newMetaObjects.remove(metaClass.getTableName());
                final int rowCount = connector.deleteMetaObject(user, metaObject, user.getDomain());

                Assert.assertEquals("One '" + metaClass.getTableName() + "' (id:" + classId + ") entity deleted",
                        1, rowCount);
                final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, metaClass.getTableName());

                Assert.assertEquals(expectedCount + " '" + metaClass.getTableName() + "' (id:" + classId + ") entities in Integration Base",
                        expectedCount, actualCount);

                LOGGER.info("deleteMetaObject(" + classId + ") test passed!");
            }

        } catch (AssertionError ae) {
            LOGGER.error("deleteMetaObject(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during deleteMetaObject(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test changing the 'name' property of all objects
     *
     * @param classId
     * @throws Exception
     */
    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService03updateMetaObjectNameProperty(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.03] testing updateMetaObjectNameProperty(" + classId + ")");
                final int expectedCount = dbEntitiesCount.get(tableName);

                Assert.assertTrue("meta object ids for meta class '" + tableName + "' cached",
                        metaObjectIds.containsKey(tableName.toLowerCase()));
                final List<Integer> metaObjectIdList = metaObjectIds.get(tableName.toLowerCase());
                Assert.assertEquals(expectedCount + " meta object ids for meta class '" + tableName + "' cached",
                        expectedCount, metaObjectIdList.size());

                int i = 0;
                for (final int metaObjectId : metaObjectIdList) {
                    i++;
                    final MetaObject metaObject = connector.getMetaObject(
                            user, metaObjectId, classId, user.getDomain());
                    Assert.assertNotNull("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                            metaObject);

                    final ObjectAttribute nameAttribute = metaObject.getAttributeByFieldName("name");
                    if (nameAttribute != null && nameAttribute.getValue() != null) {
                        final String originalObjectName = nameAttribute.getValue().toString();
                        final String updatedObjectName = originalObjectName + " (updated)";
                        nameAttribute.setValue(updatedObjectName);
                        nameAttribute.setChanged(true);

                        // probaby set by the server:
                        //metaObject.setChanged(true);
                        //metaObject.setStatus(MetaObject.MODIFIED);
                        int response = connector.updateMetaObject(user, metaObject, user.getDomain());
                        Assert.assertEquals("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") successfully updated from server",
                                1, response);

                        final MetaObject updatedMetaObject = connector.getMetaObject(
                                user, metaObjectId, classId, user.getDomain());

                        Assert.assertNotNull("updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                                updatedMetaObject);
                        Assert.assertNotNull("name attribute of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                updatedMetaObject.getAttributeByFieldName("name"));
                        Assert.assertNotNull("name of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                updatedMetaObject.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("name of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") changed to '" + updatedObjectName + "'",
                                updatedObjectName,
                                updatedMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // Don't compare SPH_SPIELHALLE/SPH_BETREIBER recursively, because 
                        // SPH_BETREIBER contains a back reference to SPH_SPIELHALLE -> comparison will fail!
                        if (tableName.equalsIgnoreCase("SPH_SPIELHALLE")) {
                            //compare only top level child objects and arrays
                            this.compareMetaObjects(metaObject, updatedMetaObject, true, false, true);
                        } else {
                            this.compareMetaObjects(metaObject, updatedMetaObject, false, false, true);
                        }

                        // revert changes!
                        final ObjectAttribute updatedNameAttribute = updatedMetaObject.getAttributeByFieldName("name");
                        updatedNameAttribute.setValue(originalObjectName);
                        updatedNameAttribute.setChanged(true);

                        response = connector.updateMetaObject(user, updatedMetaObject, user.getDomain());
                        Assert.assertEquals("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") successfully updated (reverted) from server",
                                1, response);

                        final MetaObject revertedMetaObject = connector.getMetaObject(
                                user, metaObjectId, classId, user.getDomain());

                        Assert.assertNotNull("updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                                revertedMetaObject);
                        Assert.assertNotNull("name attribute of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                revertedMetaObject.getAttributeByFieldName("name"));
                        Assert.assertNotNull("name of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                revertedMetaObject.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("name of meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") reverted to '" + originalObjectName + "'",
                                originalObjectName,
                                revertedMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // Don't compare SPH_SPIELHALLE/SPH_BETREIBER recursively, because 
                        // SPH_BETREIBER contains a back reference to SPH_SPIELHALLE -> comparison will fail!
                        if (tableName.equalsIgnoreCase("SPH_SPIELHALLE")) {
                            //compare only top level child objects and arrays
                            this.compareMetaObjects(updatedMetaObject, revertedMetaObject, true, false, true);
                        } else {
                            this.compareMetaObjects(updatedMetaObject, revertedMetaObject, false, false, true);
                        }
                    }
                }

                final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, metaClass.getTableName());
                Assert.assertEquals(expectedCount + " '" + metaClass.getTableName() + "' entities in Integration Base",
                        expectedCount, actualCount);

                LOGGER.info("updateMetaObjectNameProperty(" + classId + ") test passed! "
                        + expectedCount + " meta objects updated");
            }

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObjectNameProperty(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObjectNameProperty(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * FIXME: Test ignored: Expected behaviour of MetaObject.setChanged(),
     * MetaObject.setStatus(), ObjectAttribute.setChanged() no clear - cannot be
     * tested
     *
     * Test changing the 'name' property of all objects when no changed flag is
     * set on meta object (server should perform no changes!).
     *
     * @param classId
     * @throws Exception
     */
    @Test
    @UseDataProvider("getMetaClassIds")
    @Ignore
    public void test04objectService04updateMetaObjectNamePropertyNoObjectChangeFlag(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.04] testing updateMetaObjectNamePropertyNoObjectChangeFlag(" + classId + ")");
                final int expectedCount = dbEntitiesCount.get(tableName);

                Assert.assertTrue("meta object ids for meta class '" + tableName + "' cached",
                        metaObjectIds.containsKey(tableName.toLowerCase()));
                final List<Integer> metaObjectIdList = metaObjectIds.get(tableName.toLowerCase());
                Assert.assertEquals(expectedCount + " meta object ids for meta class '" + tableName + "' cached",
                        expectedCount, metaObjectIdList.size());

                int i = 0;
                for (final int metaObjectId : metaObjectIdList) {
                    i++;
                    final MetaObject metaObject = connector.getMetaObject(
                            user, metaObjectId, classId, user.getDomain());
                    Assert.assertNotNull("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                            metaObject);

                    final ObjectAttribute nameAttribute = metaObject.getAttributeByFieldName("name");
                    if (nameAttribute != null && nameAttribute.getValue() != null) {
                        final String originalObjectName = nameAttribute.getValue().toString();
                        final String updatedObjectName = originalObjectName + " (updated)";
                        nameAttribute.setValue(updatedObjectName);
                        nameAttribute.setChanged(true);
                        // don't set the meta object changed flag!
                        //metaObject.setChanged(true);

                        int response = connector.updateMetaObject(user, metaObject, user.getDomain());
                        Assert.assertEquals("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") successfully updated from server",
                                1, response);

                        final MetaObject notUpdatedMetaObject = connector.getMetaObject(
                                user, metaObjectId, classId, user.getDomain());

                        Assert.assertNotNull("not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                                notUpdatedMetaObject);
                        Assert.assertNotNull("name attribute of not updated  meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                notUpdatedMetaObject.getAttributeByFieldName("name"));
                        Assert.assertNotNull("name of not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                notUpdatedMetaObject.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("name of not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not changed to '" + updatedObjectName + "'",
                                originalObjectName,
                                notUpdatedMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // reset for comparision!
                        nameAttribute.setValue(originalObjectName);
                        nameAttribute.setChanged(false);

                        this.compareMetaObjects(metaObject, notUpdatedMetaObject, false, false, false);
                    }
                }

                final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, metaClass.getTableName());
                Assert.assertEquals(expectedCount + " '" + metaClass.getTableName() + "' entities in Integration Base",
                        expectedCount, actualCount);

                LOGGER.info("updateMetaObjectNamePropertyNoObjectChangeFlag(" + classId + ") test passed! "
                        + expectedCount + " meta objects updated");
            }

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObjectNamePropertyNoObjectChangeFlag(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObjectNamePropertyNoObjectChangeFlag(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test changing the 'name' property of all objects when no changed flag is
     * set on the changed object attribute (server should perform no changes!).
     *
     * @param classId
     * @throws Exception
     */
    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService05updateMetaObjectNamePropertyNoAttributeChangeFlag(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.05] testing updateMetaObjectNamePropertyNoAttributeChangeFlag(" + classId + ")");
                final int expectedCount = dbEntitiesCount.get(tableName);

                Assert.assertTrue("meta object ids for meta class '" + tableName + "' cached",
                        metaObjectIds.containsKey(tableName.toLowerCase()));
                final List<Integer> metaObjectIdList = metaObjectIds.get(tableName.toLowerCase());
                Assert.assertEquals(expectedCount + " meta object ids for meta class '" + tableName + "' cached",
                        expectedCount, metaObjectIdList.size());

                int i = 0;
                for (final int metaObjectId : metaObjectIdList) {
                    i++;
                    final MetaObject metaObject = connector.getMetaObject(
                            user, metaObjectId, classId, user.getDomain());
                    Assert.assertNotNull("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                            metaObject);

                    final ObjectAttribute nameAttribute = metaObject.getAttributeByFieldName("name");
                    if (nameAttribute != null && nameAttribute.getValue() != null) {
                        final String originalObjectName = nameAttribute.getValue().toString();
                        final String updatedObjectName = originalObjectName + " (updated)";
                        nameAttribute.setValue(updatedObjectName);
                        // don't set the attribute changed flag!
                        //nameAttribute.setChanged(true);
                        metaObject.setChanged(true);

                        int response = connector.updateMetaObject(user, metaObject, user.getDomain());
                        Assert.assertEquals("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") successfully updated from server",
                                1, response);

                        final MetaObject notUpdatedMetaObject = connector.getMetaObject(
                                user, metaObjectId, classId, user.getDomain());

                        Assert.assertNotNull("not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                                notUpdatedMetaObject);
                        Assert.assertNotNull("not updated name attribute of not updated  meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                notUpdatedMetaObject.getAttributeByFieldName("name"));
                        Assert.assertNotNull("not updated name of not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") is not null",
                                notUpdatedMetaObject.getAttributeByFieldName("name").getValue());
                        Assert.assertEquals("not updated name of not updated meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not changed to '" + updatedObjectName + "'",
                                originalObjectName,
                                notUpdatedMetaObject.getAttributeByFieldName("name").getValue().toString());

                        // reset for comparision!
                        nameAttribute.setValue(originalObjectName);
                        metaObject.setChanged(false);

                        this.compareMetaObjects(metaObject, notUpdatedMetaObject, false, false, false);
                    }
                }

                final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, metaClass.getTableName());
                Assert.assertEquals(expectedCount + " '" + metaClass.getTableName() + "' entities in Integration Base",
                        expectedCount, actualCount);

                LOGGER.info("updateMetaObjectNamePropertyNoAttributeChangeFlag(" + classId + ") test passed! "
                        + expectedCount + " meta objects updated");
            }

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObjectNamePropertyNoAttributeChangeFlag(" + classId + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObjectNamePropertyNoAttributeChangeFlag(" + classId + "): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test04objectService06replaceMetaObjectObjectProperty() throws Exception {

        try {
            LOGGER.debug("[04.06] testing replaceMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");

            final List<MetaObject> kategorien = this.getAllMetaObjects("SPH_KATEGORIE");
            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedCount = dbEntitiesCount.get("SPH_SPIELHALLE");

            Assert.assertTrue("SPH_KATEGORIE meta objects available",
                    !kategorien.isEmpty());
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject metaObject : spielhallen) {
                i++;

                final ObjectAttribute objectAttribute = metaObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is not null",
                        objectAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is not null",
                        objectAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(objectAttribute.getValue().getClass()));

                final MetaObject oldKategorie = (MetaObject) objectAttribute.getValue();
                MetaObject newKategorie = oldKategorie;
                while (oldKategorie.getID() == newKategorie.getID()) {
                    newKategorie = kategorien.get(new Random().nextInt(kategorien.size()));
                }

                objectAttribute.setValue(newKategorie);
                objectAttribute.setChanged(true);

                int response = connector.updateMetaObject(user, metaObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' (id:" + metaObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedMetaObject = connector.getMetaObject(
                        user, metaObject.getID(), metaObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' (id:" + metaObject.getMetaClass().getID() + ") retrieved from server",
                        updatedMetaObject);
                Assert.assertNotNull("replaced hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' (id:" + metaObject.getMetaClass().getID() + ") is not null",
                        updatedMetaObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("replaced hauptkategorie of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' (id:" + metaObject.getMetaClass().getID() + ") is not null",
                        updatedMetaObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("replaced hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' (id:" + metaObject.getMetaClass().getID() + ") changed from '" + oldKategorie.getName() + "' to '" + newKategorie.getName() + "'",
                        newKategorie.getName(),
                        ((MetaObject) updatedMetaObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                this.compareMetaObjects(metaObject, updatedMetaObject, true, false, true);
            }

            final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, "SPH_SPIELHALLE");
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            LOGGER.info("replaceMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("replaceMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during replaceMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test04objectService07updateMetaObjectObjectProperty() throws Exception {

        try {
            LOGGER.debug("[04.07] testing replaceMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final ObjectAttribute hauptkategorieAttribute = spielhalleObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(hauptkategorieAttribute.getValue().getClass()));

                final MetaObject kategorieObject = (MetaObject) hauptkategorieAttribute.getValue();

                final ObjectAttribute hauptkategorieNameAttribute = kategorieObject.getAttributeByFieldName("name");
                final String oldlKategorieName = hauptkategorieNameAttribute.getValue().toString();
                final String updatedKategorieName = oldlKategorieName + " (updated)";

                hauptkategorieNameAttribute.setValue(updatedKategorieName);
                hauptkategorieNameAttribute.setChanged(true);
                kategorieObject.setChanged(true);
                kategorieObject.setStatus(MetaObject.MODIFIED);

                hauptkategorieAttribute.setChanged(true);
                spielhalleObject.setChanged(true);
                spielhalleObject.setStatus(MetaObject.MODIFIED);

                Assert.assertEquals("changed hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldlKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        ((MetaObject) spielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldlKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        ((MetaObject) updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                this.compareMetaObjects(spielhalleObject, updatedSpielhalleObject, true, false, true);

                // revert changes!
                final ObjectAttribute updatedHauptkategorieAttribute = updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie");
                final MetaObject updatedKategorie = (MetaObject) updatedHauptkategorieAttribute.getValue();

                final ObjectAttribute updatedHauptkategorieNameAttribute = updatedKategorie.getAttributeByFieldName("name");
                updatedHauptkategorieNameAttribute.setValue(oldlKategorieName);

                // flag changed attributes:
                updatedHauptkategorieNameAttribute.setChanged(true);
                updatedHauptkategorieAttribute.setChanged(true);

                // flag changed metaobjects:
                updatedKategorie.setChanged(true);
                updatedKategorie.setStatus(MetaObject.MODIFIED);

                response = connector.updateMetaObject(user, updatedSpielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject revertedMetaObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") reverted from '" + updatedKategorieName + "' to '" + oldlKategorieName + "'",
                        oldlKategorieName,
                        ((MetaObject) updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                // Don't compare SPH_SPIELHALLE/SPH_BETREIBER recursively, because 
                // SPH_BETREIBER contains a back reference to SPH_SPIELHALLE -> comparison will fail!
                this.compareMetaObjects(updatedSpielhalleObject, revertedMetaObject, true, false, true);

            }

            final int actualCount = RESTfulInterfaceTest.countDbEntities(jdbcConnection, "SPH_SPIELHALLE");
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            LOGGER.info("updateMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HELPER METHODS ----------------------------------------------------------">
    protected List<MetaObject> getAllMetaObjects(final String metaClassTableName) throws RemoteException {
        final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), metaClassTableName);

        Assert.assertNotNull("meta class '" + metaClassTableName + "' from meta class cache not null", metaClass);
        Assert.assertTrue(metaClass.getTableName() + " entities counted",
                dbEntitiesCount.containsKey(metaClassTableName));
        Assert.assertTrue("meta object ids for meta class '" + metaClassTableName + "' cached",
                metaObjectIds.containsKey(metaClassTableName.toLowerCase()));
        final List<Integer> metaObjectIdList = metaObjectIds.get(metaClassTableName.toLowerCase());
        final int expectedCount = dbEntitiesCount.get(metaClassTableName);
        Assert.assertEquals(expectedCount + " meta object ids for meta class '" + metaClassTableName + "' cached",
                expectedCount, metaObjectIdList.size());

        final ArrayList<MetaObject> metaObjects = new ArrayList<MetaObject>(expectedCount);
        int i = 0;
        for (final int metaObjectId : metaObjectIdList) {
            i++;
            final MetaObject metaObject = connector.getMetaObject(
                    user, metaObjectId, metaClass.getId(), user.getDomain());
            Assert.assertNotNull("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + metaClass.getId() + ") retrieved from server",
                    metaObject);
            metaObjects.add(metaObject);

        }
        return metaObjects;
    }

    /**
     * Compares recursivly MetaObjects and thier attributes. If compareChanged
     * or compareNew, are true, some fields are not compared. If limitRecursion
     * is set to true, the recursive comparision stops if the an object of the
     * same type (MetaClass) has already been compared. E.g. a/b/c/a compares
     * only down to level c
     *
     * @param expectedMetaObject
     * @param actualMetaObject
     * @param limitRecursion
     * @param compareChanged
     * @param compareNew
     *
     * @throws AssertionError
     */
    protected void compareMetaObjects(
            final MetaObject expectedMetaObject,
            final MetaObject actualMetaObject,
            final boolean limitRecursion,
            final boolean compareNew,
            final boolean compareChanged) throws AssertionError {

        this.compareMetaObjects(expectedMetaObject, actualMetaObject, limitRecursion, new ArrayList<String>(), compareNew, compareChanged);
    }

    /**
     * Method should only be invoked during recursive object comparision.
     *
     * @param expectedMetaObject
     * @param actualMetaObject
     * @param limitRecursion
     * @param objectHierarchy recursive object hierarchy
     * @param compareChanged
     * @param compareNew
     *
     * @throws AssertionError
     */
    protected void compareMetaObjects(
            final MetaObject expectedMetaObject,
            final MetaObject actualMetaObject,
            final boolean limitRecursion,
            final List<String> objectHierarchy,
            final boolean compareNew,
            final boolean compareChanged) throws AssertionError {

        final String name = expectedMetaObject.getAttribute("name") != null
                ? expectedMetaObject.getAttribute("name").toString()
                : String.valueOf(expectedMetaObject.getId());

        Assert.assertEquals("expected MetaObject [" + name + "].getClassID() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getClassID(),
                actualMetaObject.getClassID());

        Assert.assertEquals("expected MetaObject [" + name + "].getClassKey() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getClassKey(),
                actualMetaObject.getClassKey());
        objectHierarchy.add(expectedMetaObject.getClassKey());

        Assert.assertEquals("expected MetaObject [" + name + "].getComplexEditor() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getComplexEditor(),
                actualMetaObject.getComplexEditor());

        Assert.assertEquals("expected MetaObject [" + name + "].getDescription() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getDescription(),
                actualMetaObject.getDescription());

        Assert.assertEquals("expected MetaObject [" + name + "].getDomain() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getDomain(),
                actualMetaObject.getDomain());

        Assert.assertEquals("expected MetaObject [" + name + "].getEditor() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getEditor(),
                actualMetaObject.getEditor());

        Assert.assertEquals("expected MetaObject [" + name + "].getGroup() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getGroup(),
                actualMetaObject.getGroup());

        // don't compare ids of new objects (id is -1 before insert!)
        if (!compareNew) {
            Assert.assertEquals("expected MetaObject [" + name + "].getId() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getId(),
                    actualMetaObject.getId());

            Assert.assertEquals("expected MetaObject [" + name + "].getID() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getID(),
                    actualMetaObject.getID());

            Assert.assertEquals("expected MetaObject [" + name + "].getKey() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getKey(),
                    actualMetaObject.getKey());
        }

        Assert.assertEquals("expected MetaObject [" + name + "].getName() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getName(),
                actualMetaObject.getName());

        Assert.assertEquals("expected MetaObject [" + name + "].getRenderer() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getRenderer(),
                actualMetaObject.getRenderer());

        Assert.assertEquals("expected MetaObject [" + name + "].getSimpleEditor() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getSimpleEditor(),
                actualMetaObject.getSimpleEditor());

        // id and status of new / changed objects do not match remotely objects inserted / upated
        if (!compareNew && !compareChanged) {
            Assert.assertEquals("expected MetaObject [" + name + "].getPropertyString() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getPropertyString(),
                    actualMetaObject.getPropertyString());

            Assert.assertEquals("expected MetaObject [" + name + "].getStatus() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getStatus(),
                    actualMetaObject.getStatus());

            Assert.assertEquals("expected MetaObject [" + name + "].getStatusDebugString() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getStatusDebugString(),
                    actualMetaObject.getStatusDebugString());
        }

        final ObjectAttribute[] expectedObjectAttributes = expectedMetaObject.getAttribs();
        final ObjectAttribute[] actualObjectAttributes = actualMetaObject.getAttribs();

        Assert.assertEquals("expected MetaObject [" + name + "].getAttribs() matches actual MetaObject.getAttribs()  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttributes.length,
                actualObjectAttributes.length);

        for (int i = 0; i < expectedObjectAttributes.length; i++) {
            this.compareObjectAttributes(
                    expectedObjectAttributes[i],
                    actualObjectAttributes[i],
                    limitRecursion,
                    objectHierarchy,
                    compareNew,
                    compareChanged);
        }
    }

    /**
     * Compares ObjectAttributes. If recursive is true and the object value is a
     * MetaObject, compareMetaObjects is invoked and compareNew and
     * compareChanged are passed as arguments.
     *
     * @param expectedObjectAttribute
     * @param actualObjectAttribute
     * @param limitRecursion
     * @param objectHierarchy
     * @param compareNew
     * @param compareChanged
     * @throws AssertionError
     */
    protected void compareObjectAttributes(
            final ObjectAttribute expectedObjectAttribute,
            final ObjectAttribute actualObjectAttribute,
            final boolean limitRecursion,
            final List<String> objectHierarchy,
            final boolean compareNew,
            final boolean compareChanged) throws AssertionError {

        final String name = expectedObjectAttribute.getName();

        Assert.assertEquals("expected objectAttribute[" + name + "].getClassID()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.getClassID(),
                actualObjectAttribute.getClassID());

        Assert.assertEquals("expected objectAttribute[" + name + "].getClassKey()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.getClassKey(),
                actualObjectAttribute.getClassKey());

        Assert.assertEquals("expected objectAttribute[" + name + "].getDescription()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.getDescription(),
                actualObjectAttribute.getDescription());

        // id and status of new / changed objects do not match remotely objects inserted / upated
        if (!compareNew) {
            Assert.assertEquals("expected objectAttribute[" + name + "].getID()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedObjectAttribute.getID(),
                    actualObjectAttribute.getID());

            Assert.assertEquals("expected objectAttribute[" + name + "].getKey()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedObjectAttribute.getKey(),
                    actualObjectAttribute.getKey());
        }

        // FIXME: MetaClass.getEmptyInstance does not set JavaType
        // https://github.com/cismet/cids-server/issues/166
//        Assert.assertEquals("expected objectAttribute[" + name + "].getJavaType()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
//                expectedObjectAttribute.getJavaType(),
//                actualObjectAttribute.getJavaType());
        Assert.assertEquals("expected objectAttribute[" + name + "].getPermissions().toString()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.getPermissions().getPolicy().toString(),
                actualObjectAttribute.getPermissions().getPolicy().toString());

        Assert.assertEquals("expected objectAttribute[" + name + "].getTypeId()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.getTypeId(),
                actualObjectAttribute.getTypeId());

        Assert.assertEquals("expected objectAttribute[" + name + "].isArray()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isArray(),
                actualObjectAttribute.isArray());

        // id and status of new / changed objects do not match remotely objects inserted / upated
        if (!compareNew && !compareChanged) {
            Assert.assertEquals("expected objectAttribute[" + name + "].isChanged()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedObjectAttribute.isChanged(),
                    actualObjectAttribute.isChanged());
        }

        Assert.assertEquals("expected objectAttribute[" + name + "].isOptional()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isOptional(),
                actualObjectAttribute.isOptional());

        Assert.assertEquals("expected objectAttribute[" + name + "].isPrimaryKey()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isPrimaryKey(),
                actualObjectAttribute.isPrimaryKey());

        Assert.assertEquals("expected objectAttribute[" + name + "].isSubstitute()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isSubstitute(),
                actualObjectAttribute.isSubstitute());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVisible()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isVisible(),
                actualObjectAttribute.isVisible());

        Assert.assertEquals("expected objectAttribute[" + name + "].isStringCreateable()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isStringCreateable(),
                actualObjectAttribute.isStringCreateable());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVirtualOneToManyAttribute()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isVirtualOneToManyAttribute(),
                actualObjectAttribute.isVirtualOneToManyAttribute());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVisible()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.isVisible(),
                actualObjectAttribute.isVisible());

        Assert.assertEquals("expected objectAttribute[" + name + "].referencesObject()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedObjectAttribute.referencesObject(),
                actualObjectAttribute.referencesObject());

        final MemberAttributeInfo expectedMemberAttributeInfo = expectedObjectAttribute.getMai();
        final MemberAttributeInfo actualMemberAttributeInfo = actualObjectAttribute.getMai();

        if (expectedMemberAttributeInfo != null) {
            Assert.assertNotNull("expected objectAttribute[" + name + "] has MemberAttributeInfo (" + this.getHierarchyPath(objectHierarchy) + ")",
                    actualMemberAttributeInfo);

            this.compareMemberAttributeInfos(
                    expectedMemberAttributeInfo,
                    actualMemberAttributeInfo,
                    this.getHierarchyPath(objectHierarchy));
        } else {
            Assert.assertNull("expected objectAttribute[" + name + "] has no MemberAttributeInfo (" + this.getHierarchyPath(objectHierarchy) + ")",
                    actualMemberAttributeInfo);
        }

        final Object expectedObjectAttributeValue = expectedObjectAttribute.getValue();
        final Object actualObjectAttributeValue = actualObjectAttribute.getValue();

        if (expectedObjectAttributeValue != null) {
            Assert.assertNotNull("actual objectAttribute[" + name + "] value is not null (" + this.getHierarchyPath(objectHierarchy) + ")",
                    actualObjectAttributeValue);

            final Class expectedObjectAttributeValueClass = expectedObjectAttributeValue.getClass();
            final Class actualObjectAttributeValueClass = actualObjectAttributeValue.getClass();

            Assert.assertEquals("actual objectAttribute[" + name + "] value from rest server is a "
                    + expectedObjectAttributeValueClass.getSimpleName() + " (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedObjectAttributeValueClass,
                    actualObjectAttributeValueClass);

            if (expectedObjectAttribute.referencesObject()) {
                Assert.assertTrue("expected objectAttribute[" + name + "] value is a MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                        MetaObject.class.isAssignableFrom(expectedObjectAttributeValueClass));
                Assert.assertTrue("actual objectAttribute[" + name + "] value is a MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                        MetaObject.class.isAssignableFrom(actualObjectAttributeValueClass));

                // if recursion shall be limited check if the an object of the
                // same type exists already in the hierarchy (parent object)
                if (!limitRecursion || !objectHierarchy.contains(((MetaObject) expectedObjectAttributeValue).getClassKey())) {
                    // recursively compare meta objects
                    this.compareMetaObjects(
                            (MetaObject) expectedObjectAttributeValue,
                            (MetaObject) actualObjectAttributeValue,
                            limitRecursion,
                            objectHierarchy,
                            compareNew,
                            compareChanged);
                }

            } else if (expectedObjectAttributeValueClass.isPrimitive()) {
                Assert.assertEquals("actual objectAttribute[" + name + "] primitive value matches (" + this.getHierarchyPath(objectHierarchy) + ")",
                        expectedObjectAttributeValue,
                        actualObjectAttributeValue);

                // disable value comparision for non-primitives due to unkown behaviour of object.equals
            }
        } else if (!compareNew && !compareChanged) {
            // disable null value comparison for new and changed objects
            // server may populate null values (e.g. dummy array objects, default values, etc.)
            Assert.assertNull("expected objectAttribute[" + name + "] value from rest server is null (" + this.getHierarchyPath(objectHierarchy) + ")",
                    actualObjectAttributeValue);
        }
    }

    protected void compareMemberAttributeInfos(
            final MemberAttributeInfo expectedMemberAttributeInfo,
            final MemberAttributeInfo actualMemberAttributeInfo,
            final String hierarchyPath) {

        final String name = expectedMemberAttributeInfo.getName();

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getArrayKeyFieldName matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getArrayKeyFieldName(),
                actualMemberAttributeInfo.getArrayKeyFieldName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getClassId() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getClassId(),
                actualMemberAttributeInfo.getClassId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getComplexEditor() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getComplexEditor(),
                actualMemberAttributeInfo.getComplexEditor());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getDefaultValue() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getDefaultValue(),
                actualMemberAttributeInfo.getDefaultValue());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getEditor() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getEditor(),
                actualMemberAttributeInfo.getEditor());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getFieldName() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getFieldName(),
                actualMemberAttributeInfo.getFieldName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getForeignKeyClassId() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getForeignKeyClassId(),
                actualMemberAttributeInfo.getForeignKeyClassId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getFromString() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getFromString(),
                actualMemberAttributeInfo.getFromString());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getId() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getId(),
                actualMemberAttributeInfo.getId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getJavaclassname() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getJavaclassname(),
                actualMemberAttributeInfo.getJavaclassname());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getKey() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getKey(),
                actualMemberAttributeInfo.getKey());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getName() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getName(),
                actualMemberAttributeInfo.getName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getPosition() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getPosition(),
                actualMemberAttributeInfo.getPosition());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getRenderer() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getRenderer(),
                actualMemberAttributeInfo.getRenderer());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getToString() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getToString(),
                actualMemberAttributeInfo.getToString());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getTypeId() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.getTypeId(),
                actualMemberAttributeInfo.getTypeId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isArray() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.isArray(),
                actualMemberAttributeInfo.isArray());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isExtensionAttribute() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.isExtensionAttribute(),
                actualMemberAttributeInfo.isExtensionAttribute());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isForeignKey() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.isForeignKey(),
                actualMemberAttributeInfo.isForeignKey());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isIndexed() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.isIndexed(),
                actualMemberAttributeInfo.isIndexed());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isOptional() matches actual MemberAttributeInfo (" + hierarchyPath + ")",
                expectedMemberAttributeInfo.isOptional(),
                actualMemberAttributeInfo.isOptional());
    }

    protected String getHierarchyPath(final List<String> objectHierarchy) {
        StringBuilder sb = new StringBuilder();
        final Iterator<String> iterator = objectHierarchy.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append('/');
            }
        }

        return sb.toString();
    }

    // </editor-fold>
}
