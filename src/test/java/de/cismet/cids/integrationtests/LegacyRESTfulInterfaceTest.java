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
import java.io.IOError;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
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
                + TestEnvironment.isIntegrationTestsEnabled());

        // check if integration tests are enabled in current maven profile
        if (!TestEnvironment.isIntegrationTestsEnabled()) {
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

            // check cnnection to legacy server (broker f.k.a. callserver)
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

            jdbcConnection = createJdbcConnection();

        } catch (Exception e) {

            LOGGER.error("Unexpected exception during Global Test initialisation :" + e.getMessage(), e);
            throw e;
        }
    }

    //@AfterClass
    public static void afterClass() {
        LOGGER.debug("afterClass() invoked, cleaning up");
        try {
            if (jdbcConnection != null) {
                jdbcConnection.close();
            }

            // finish outstanding DB operations (e.g. triggers)
            Thread.sleep(2000);

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
            final int count = countDbEntities(tableName, 3);
            if (!tableName.equalsIgnoreCase("URL_BASE") && !tableName.equalsIgnoreCase("URL")) {
                Assert.assertTrue(tableName + " entities available in integration base", count > 0);
            }

            Assert.assertNull(tableName + " entities counted only once",
                    dbEntitiesCount.put(tableName, count));

            LOGGER.info("countDbEntities(" + tableName + ") test passed: " + count);

        } catch (AssertionError ae) {
            LOGGER.error("countDbEntities(" + tableName + ") test failed with: "
                    + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during countDbEntities(" + tableName + "): "
                    + ex.getMessage(), ex);
            throw ex;
        }
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

            Assert.assertFalse("MetaClassCache is not empty",
                    MetaClassCache.getInstance().getAllClasses(user.getDomain()).isEmpty());

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
            Assert.assertFalse("MetaClassCache is not empty",
                    MetaClassCache.getInstance().getAllClasses(user.getDomain()).isEmpty());
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);
            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);

            // FIXME: ObjectFactory().getInstance() fails with NPE for Classes SPH_SPIELHALLE and SPH_Betreiber
            //            final parentObject metaObjectFromService = connector.getInstance(user, metaClass);
            //            Assert.assertNotNull("new meta object of meta class '" + classId + "' from service not null",
            //                    metaObjectFromService);
            final MetaObject metaObjectFromClass = metaClass.getEmptyInstance();
            Assert.assertNotNull("new meta object of meta class '" + classId + "' from meta class not null",
                    metaObjectFromClass);

            // FIXME:  parentObject].getPropertyString()  does not match!
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

    /**
     * Verify the order of n-m and 1-n array elements after repreated object
     * retrieval
     *
     * @throws Exception
     */
    @Test
    public void test04objectService01getMetaObjectArrays() throws Exception {
        try {
            LOGGER.debug("[04.01] testing getMetaObjectArrays()");

            final int expectedBetreiberCount = dbEntitiesCount.get("SPH_BETREIBER");
            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");

            final List<MetaObject> betreiber1 = this.getAllMetaObjects("SPH_BETREIBER");
            final List<MetaObject> betreiber2 = this.getAllMetaObjects("SPH_BETREIBER");

            Assert.assertFalse("SPH_BETREIBER meta objects available",
                    betreiber1.isEmpty());
            Assert.assertFalse("SPH_BETREIBER meta objects available",
                    betreiber2.isEmpty());
            Assert.assertEquals(betreiber1.size() + " SPH_BETREIBER meta objects in both arrays",
                    betreiber1.size(),
                    betreiber2.size());
            Assert.assertEquals(expectedBetreiberCount + " SPH_BETREIBER meta objects in database",
                    expectedBetreiberCount,
                    betreiber2.size());

            int i = 0;
            for (final MetaObject betreiberObject1 : betreiber1) {
                final MetaObject betreiberObject2 = betreiber2.get(i);
                i++;

                final MetaObject betreiberObject3 = connector.getMetaObject(user, betreiberObject1.getID(), betreiberObject1.getMetaClass().getID(), user.getDomain());

                Assert.assertEquals("order of SPH_BETREIBER meta objects matches",
                        betreiberObject1.getID(),
                        betreiberObject2.getID());
                Assert.assertEquals("order of SPH_BETREIBER meta objects matches",
                        betreiberObject2.getID(),
                        betreiberObject3.getID());

                final List<MetaObject> spielhallen1 = getArrayElements(betreiberObject1, "spielhallen");
                final List<MetaObject> spielhallen2 = getArrayElements(betreiberObject2, "spielhallen");
                final List<MetaObject> spielhallen3 = getArrayElements(betreiberObject3, "spielhallen");

                Assert.assertFalse("SPH_SPIELHALLE meta objects available in SPH_BETREIBER " + betreiberObject1.getID(),
                        spielhallen1.isEmpty());
                Assert.assertFalse("SPH_SPIELHALLE meta objects available in SPH_BETREIBER " + betreiberObject2.getID(),
                        spielhallen2.isEmpty());
                Assert.assertFalse("SPH_SPIELHALLE meta objects available in SPH_BETREIBER " + betreiberObject3.getID(),
                        spielhallen3.isEmpty());

                Assert.assertEquals(spielhallen1.size() + " SPH_SPIELHALLE meta objects available in SPH_BETREIBER " + betreiberObject1.getID(),
                        spielhallen1.size(),
                        spielhallen2.size());
                Assert.assertEquals(spielhallen2.size() + " SPH_SPIELHALLE meta objects available in SPH_BETREIBER " + betreiberObject1.getID(),
                        spielhallen2.size(),
                        spielhallen3.size());

                int j = 0;
                for (final MetaObject spielhalleObject1 : spielhallen1) {
                    final MetaObject spielhalleObject2 = spielhallen2.get(j);
                    final MetaObject spielhalleObject3 = spielhallen3.get(j);
                    j++;

                    Assert.assertEquals("order of SPH_SPIELHALLE meta objects in SPH_BETREIBER (" + betreiberObject1.getID() + ") array matches",
                            spielhalleObject1.getID(),
                            spielhalleObject2.getID());
                    Assert.assertEquals("order of SPH_SPIELHALLE meta objects in SPH_BETREIBER (" + betreiberObject2.getID() + ") array matches",
                            spielhalleObject2.getID(),
                            spielhalleObject3.getID());

                    this.compareMetaObjects(spielhalleObject1, spielhalleObject2, false, false, false);
                    this.compareMetaObjects(spielhalleObject2, spielhalleObject3, false, false, false);
                }

                this.compareMetaObjects(betreiberObject1, betreiberObject2, false, false, false);
                this.compareMetaObjects(betreiberObject2, betreiberObject3, false, false, false);
            }

            final List<MetaObject> spielhallen1 = this.getAllMetaObjects("SPH_SPIELHALLE");
            final List<MetaObject> spielhallen2 = this.getAllMetaObjects("SPH_SPIELHALLE");

            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    spielhallen1.isEmpty());
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    spielhallen2.isEmpty());
            Assert.assertEquals(spielhallen1.size() + " SPH_SPIELHALLE meta objects in both arrays",
                    spielhallen1.size(),
                    spielhallen2.size());
            Assert.assertEquals(expectedBetreiberCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    spielhallen2.size());

            i = 0;
            for (final MetaObject spielhalleObject1 : spielhallen1) {
                final MetaObject spielhalleObject2 = spielhallen2.get(i);
                i++;

                final MetaObject spielhalleObject3 = connector.getMetaObject(user, spielhalleObject1.getID(), spielhalleObject1.getMetaClass().getID(), user.getDomain());

                Assert.assertEquals("order of SPH_SPIELHALLE meta objects matches",
                        spielhalleObject1.getID(),
                        spielhalleObject2.getID());

                final List<MetaObject> kategorien1 = getArrayElements(spielhalleObject1, "kategorien");
                final List<MetaObject> kategorien2 = getArrayElements(spielhalleObject2, "kategorien");
                final List<MetaObject> kategorien3 = getArrayElements(spielhalleObject3, "kategorien");

                Assert.assertFalse("SPH_KATEGORIE meta objects available in SPH_SPIELHALLE " + spielhalleObject1.getID(),
                        kategorien1.isEmpty());
                Assert.assertFalse("SPH_KATEGORIE meta objects available in SPH_SPIELHALLE " + spielhalleObject2.getID(),
                        kategorien2.isEmpty());
                Assert.assertFalse("SPH_KATEGORIE meta objects available in SPH_SPIELHALLE " + spielhalleObject3.getID(),
                        kategorien3.isEmpty());

                Assert.assertEquals(kategorien1.size() + " SPH_KATEGORIE meta objects available in SPH_SPIELHALLE " + spielhalleObject1.getID(),
                        kategorien1.size(),
                        kategorien2.size());
                Assert.assertEquals(kategorien2.size() + " SPH_KATEGORIE meta objects available in SPH_SPIELHALLE " + spielhalleObject1.getID(),
                        kategorien2.size(),
                        kategorien3.size());

                int j = 0;
                for (final MetaObject kategorieObject1 : kategorien1) {

                    final MetaObject kategorieObject2 = kategorien2.get(j);
                    final MetaObject kategorieObject3 = kategorien3.get(j);
                    j++;

                    Assert.assertEquals("order of SPH_KATEGORIE meta objects in SPH_SPIELHALLE (" + spielhalleObject1.getID() + ") array matches",
                            kategorieObject1.getID(),
                            kategorieObject2.getID());
                    Assert.assertEquals("order of SPH_KATEGORIE meta objects in SPH_SPIELHALLE (" + spielhalleObject2.getID() + ") array matches",
                            kategorieObject2.getID(),
                            kategorieObject3.getID());

                    this.compareMetaObjects(kategorieObject1, kategorieObject2, false, false, false);
                }

                this.compareMetaObjects(spielhalleObject1, spielhalleObject2, false, false, false);
                this.compareMetaObjects(spielhalleObject2, spielhalleObject3, false, false, false);
            }

            LOGGER.info("getMetaObjectArrays() test passed! "
                    + (expectedSpielhallenCount + expectedBetreiberCount) + " meta objects with arrays compared");

        } catch (AssertionError ae) {
            LOGGER.error("getMetaObjectArrays() test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getMetaObjectArrays(): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    @UseDataProvider("getMetaClassIds")
    public void test04objectService02insertMetaObject(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.02] testing insertMetaObject(" + classId + ")");
                final int expectedCount = dbEntitiesCount.get(metaClass.getTableName()) + 1;

                final MetaObject newMetaObject = metaClass.getEmptyInstance();
                Assert.assertNotNull("new meta object of meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not null",
                        newMetaObject);

                final MetaObject insertedMetaObject = connector.insertMetaObject(user, newMetaObject, user.getDomain());
                Assert.assertNotNull("inserted meta object of meta class '" + metaClass.getTableName() + "' (id:" + classId + ") not null",
                        insertedMetaObject);

                newMetaObjects.put(metaClass.getTableName(), insertedMetaObject);

                final int actualCount = countDbEntities(metaClass.getTableName(), 3);
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
    public void test04objectService03deleteMetaObject(final Integer classId) throws Exception {

        try {

            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {
                LOGGER.debug("[04.03] testing deleteMetaObject(" + classId + ")");

                final int expectedCount = dbEntitiesCount.get(metaClass.getTableName());
                Assert.assertTrue("new '" + metaClass.getTableName() + "' (id:" + classId + ") entity created",
                        newMetaObjects.containsKey(metaClass.getTableName()));

                final MetaObject metaObject = newMetaObjects.remove(metaClass.getTableName());
                final int rowCount = connector.deleteMetaObject(user, metaObject, user.getDomain());

                Assert.assertEquals("One '" + metaClass.getTableName() + "' (id:" + classId + ") entity deleted",
                        1, rowCount);
                final int actualCount = countDbEntities(metaClass.getTableName(), 3);

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
    public void test04objectService04updateMetaObjectNameProperty(final Integer classId) throws Exception {

        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);

            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);
            Assert.assertTrue(metaClass.getTableName() + " entities counted",
                    dbEntitiesCount.containsKey(metaClass.getTableName()));

            final String tableName = metaClass.getTableName();
            if (!tableName.equalsIgnoreCase("URL_BASE")
                    && !tableName.equalsIgnoreCase("URL")
                    && !tableName.equalsIgnoreCase("sph_spielhalle_kategorien")) {

                LOGGER.debug("[04.04] testing updateMetaObjectNameProperty(" + classId + ")");
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

                    final MetaObject originalMetaObject = connector.getMetaObject(
                            user, metaObjectId, classId, user.getDomain());

                    Assert.assertNotNull("meta object #" + i + "/" + metaObjectIdList.size() + " (id:" + metaObjectId + ") for meta class '" + metaClass.getTableName() + "' (id:" + classId + ") retrieved from server",
                            originalMetaObject);

                    this.compareMetaObjects(metaObject, originalMetaObject, false, false, false);

                    final ObjectAttribute nameAttribute = metaObject.getAttributeByFieldName("name");
                    if (nameAttribute != null && nameAttribute.getValue() != null) {
                        final String originalObjectName = nameAttribute.getValue().toString();
                        final String updatedObjectName = originalObjectName + " (updated)";
                        nameAttribute.setValue(updatedObjectName);
                        nameAttribute.setChanged(true);

                        // probaby set by the server:
                        //metaObject.setChanged(true);
                        //metaObject.setStatus(parentObject.MODIFIED);
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

                        try {
                            this.compareMetaObjects(originalMetaObject, revertedMetaObject, false, false, false);
                        } catch (Throwable t) {
                            LOGGER.error("[" + i + "] " + t.getMessage(), t);
                        }
                    }
                }

                final int actualCount = countDbEntities(metaClass.getTableName(), 3);
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
     * Test changing the 'name' property of all objects when no changed flag is
     * set on the changed object arrayField (server should perform no changes!).
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
                        // don't set the arrayField changed flag!
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

                final int actualCount = countDbEntities(metaClass.getTableName(), 3);
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
    public void test04objectService06reassignMetaObjectUpdatedObjectProperty() throws Exception {

        try {
            LOGGER.debug("[04.06] testing reassignMetaObjectUpdatedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

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

                final ObjectAttribute hauptkategorieAttribute = metaObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + metaObject.getID() + ") for meta class '" + metaObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(hauptkategorieAttribute.getValue().getClass()));

                final MetaObject oldKategorie = (MetaObject) hauptkategorieAttribute.getValue();
                MetaObject newKategorie = oldKategorie;
                while (oldKategorie.getID() == newKategorie.getID()) {
                    newKategorie = kategorien.get(new Random().nextInt(kategorien.size()));
                }

                hauptkategorieAttribute.setValue(newKategorie);
                hauptkategorieAttribute.setChanged(true);

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

                // compare changed
                this.compareMetaObjects(metaObject, updatedMetaObject, true, false, true);
            }

            final int actualCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("reassignMetaObjectUpdatedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("reassignMetaObjectUpdatedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during reassignMetaObjectUpdatedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test updating a property of a nested meta object.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService07updateMetaObjectObjectProperty() throws Exception {
        try {
            LOGGER.debug("[04.07] testing updateMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_KATEGORIE");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final MetaObject originalSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        originalSpielhalleObject);
                Assert.assertNotNull("hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("hauptkategorie"));
                Assert.assertNotNull("hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());

                // safely compare recursively, because nothing has changed!
                this.compareMetaObjects(spielhalleObject, originalSpielhalleObject, false, false, false);

                final ObjectAttribute hauptkategorieAttribute = spielhalleObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(hauptkategorieAttribute.getValue().getClass()));

                final MetaObject kategorieObject = (MetaObject) hauptkategorieAttribute.getValue();

                final ObjectAttribute hauptkategorieNameAttribute = kategorieObject.getAttributeByFieldName("name");
                final String oldKategorieName = hauptkategorieNameAttribute.getValue().toString();
                final String updatedKategorieName = oldKategorieName + " (updated)";

                hauptkategorieNameAttribute.setValue(updatedKategorieName);

                // see https://github.com/cismet/developer-space/wiki/Set-parentObject-Status-explained
                // flag changed attributes:
                hauptkategorieNameAttribute.setChanged(true);
                hauptkategorieAttribute.setChanged(true);

                // set status of changed metaobjects:
                kategorieObject.setStatus(MetaObject.MODIFIED);

                Assert.assertEquals("changed hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        ((MetaObject) spielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                final String propertyString = spielhalleObject.getPropertyString();
                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") not changed by RMI",
                        propertyString, spielhalleObject.getPropertyString());

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        ((MetaObject) updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                this.compareMetaObjects(spielhalleObject, updatedSpielhalleObject, true, false, true);

                // revert changes!
                final ObjectAttribute updatedHauptkategorieAttribute = updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie");
                final MetaObject updatedKategorieObject = (MetaObject) updatedHauptkategorieAttribute.getValue();

                final ObjectAttribute updatedHauptkategorieNameAttribute = updatedKategorieObject.getAttributeByFieldName("name");
                updatedHauptkategorieNameAttribute.setValue(oldKategorieName);

                // flag changed attributes:
                updatedHauptkategorieNameAttribute.setChanged(true);
                updatedHauptkategorieAttribute.setChanged(true);

                // set status of changed metaobjects:
                updatedKategorieObject.setStatus(MetaObject.MODIFIED);

                response = connector.updateMetaObject(user, updatedSpielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject revertedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        revertedSpielhalleObject);
                Assert.assertNotNull("updated hauptkategorie attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        revertedSpielhalleObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        revertedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") reverted from '" + updatedKategorieName + "' to '" + oldKategorieName + "'",
                        oldKategorieName,
                        ((MetaObject) revertedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                // safely compare recursively, because nothing has changed!
                try {
                    this.compareMetaObjects(originalSpielhalleObject, revertedSpielhalleObject, false, false, false);
                } catch (Throwable t) {
                    LOGGER.error("[" + i + "] " + t.getMessage(), t);
                }
            }

            final int actualCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            final int actualKategorienCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
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

    /**
     * Test DON'T set status of changed (child)metaobjects: no changes applied!
     *
     * @throws Exception
     */
    @Test
    public void test04objectService08createMetaObjectObjectProperty() throws Exception {
        try {
            LOGGER.debug("[04.08] testing createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_KATEGORIE");
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
                final String oldKategorieName = hauptkategorieNameAttribute.getValue().toString();
                final String updatedKategorieName = oldKategorieName + " (updated)";

                hauptkategorieNameAttribute.setValue(updatedKategorieName);

                // see https://github.com/cismet/developer-space/wiki/Set-parentObject-Status-explained
                // flag changed attributes:
                hauptkategorieNameAttribute.setChanged(true);
                hauptkategorieAttribute.setChanged(true);

                // DON'T set status of changed metaobjects:
                //kategorieObject.setStatus(parentObject.MODIFIED);
                Assert.assertEquals("changed hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
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

                Assert.assertEquals("updated hauptkategorie of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") NOT changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "' due to missing MetaObject.STATUS",
                        oldKategorieName,
                        ((MetaObject) updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                // locally revert (unsaved!) changes
                hauptkategorieNameAttribute.setValue(oldKategorieName);
                // reset changed flags for better sibew-by-side comparision:
                hauptkategorieNameAttribute.setChanged(false);
                hauptkategorieAttribute.setChanged(false);

                this.compareMetaObjects(spielhalleObject, updatedSpielhalleObject, true, false, false);
            }

            final int actualCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            final int actualKategorienCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test create a new child meta object and set it as property.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService09createMetaObjectObjectProperty() throws Exception {
        try {
            LOGGER.debug("[04.09] testing createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            int expectedKategorienCount = dbEntitiesCount.get("SPH_KATEGORIE") + expectedSpielhallenCount;
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            final MetaClass kategorieClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), "SPH_KATEGORIE");
            Assert.assertNotNull("meta class 'SPH_KATEGORIE' from meta class cache not null", kategorieClass);
            final ArrayList<MetaObject> newCategories = new ArrayList<MetaObject>(expectedSpielhallenCount);

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final ObjectAttribute hauptkategorieAttribute = spielhalleObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(hauptkategorieAttribute.getValue().getClass()));

                final MetaObject oldKategorieObject = (MetaObject) hauptkategorieAttribute.getValue();
                final MetaObject newKategorieObject = kategorieClass.getEmptyInstance();
                Assert.assertNotNull("new 'SPH_KATEGORIE' instance created", newKategorieObject);

                final ObjectAttribute hauptkategorieNameAttribute = newKategorieObject.getAttributeByFieldName("name");
                hauptkategorieNameAttribute.setValue("TestKategorie #" + i);
                newCategories.add(newKategorieObject);

                // change the parent object's arrayField
                hauptkategorieAttribute.setValue(newKategorieObject);
                // flag changed attributes:
                hauptkategorieAttribute.setChanged(true);

                // set NEW status and trigger an insert:
                newKategorieObject.setStatus(MetaObject.NEW);

                Assert.assertNotEquals("new hauptkategorie of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") created as '"
                        + newKategorieObject.getAttributeByFieldName("name") + "' to replace '" + oldKategorieObject.getAttributeByFieldName("name") + "'",
                        oldKategorieObject.getAttributeByFieldName("name"),
                        ((MetaObject) spielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated hauptkategorie attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("updated hauptkategorie of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());

                Assert.assertEquals("new hauptkategorie of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ")  created as '"
                        + newKategorieObject.getAttributeByFieldName("name") + "' to replace '" + oldKategorieObject.getAttributeByFieldName("name") + "'",
                        ((MetaObject) spielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName(),
                        ((MetaObject) updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                // don't compare ids and status -> object is new
                this.compareMetaObjects(spielhalleObject, updatedSpielhalleObject, true, true, true);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_KATEGORIE' entities in Integration Base (" + actualSpielhallenCount + " are new)",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedSpielhallenCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during createMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test create a new child meta object and set it as property.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService10deleteMetaObjectObjectProperty() throws Exception {
        try {
            LOGGER.debug("[04.10] testing deleteMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_KATEGORIE");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final ObjectAttribute hauptkategorieAttribute = spielhalleObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNotNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute.getValue());
                Assert.assertTrue("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is a Meta Object",
                        MetaObject.class.isAssignableFrom(hauptkategorieAttribute.getValue().getClass()));

                final MetaObject kategorieObject = (MetaObject) hauptkategorieAttribute.getValue();
                // flag changed attributes (?):
                hauptkategorieAttribute.setChanged(true);

                // set TO_DELETE status and trigger an insert:
                kategorieObject.setStatus(MetaObject.TO_DELETE);

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNull("hauptkategorie of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") deleted",
                        updatedSpielhalleObject.getAttributeByFieldName("hauptkategorie").getValue());

                // compare as changed -> property string do not match (bestreiber->spiehalle->hauptkategorie != spiehalle->hauptkategorie)
                // set deleted object to null to pass comparison
                hauptkategorieAttribute.setValue(null);
                this.compareMetaObjects(spielhalleObject, updatedSpielhalleObject, true, false, true);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_KATEGORIE' entities in Integration Base (" + actualSpielhallenCount + " deleted)",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("deleteMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedSpielhallenCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("deleteMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during deleteMetaObjectObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Reassigns a meta object to a deleted object property. Warning: If the
     * previous test fails, this test may fail too!
     *
     * @throws Exception
     */
    @Test
    public void test04objectService11reassignMetaObjectDeletedObjectProperty() throws Exception {

        try {
            LOGGER.debug("[04.11] testing reassignMetaObjectDeletedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            final int expectedKategorienCount = dbEntitiesCount.get("SPH_KATEGORIE");
            Assert.assertTrue("SPH_KATEGORIE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final ObjectAttribute hauptkategorieAttribute = spielhalleObject.getAttributeByFieldName("hauptkategorie");
                Assert.assertNotNull("attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not null",
                        hauptkategorieAttribute);
                Assert.assertNull("value of attribute 'hauptkategorie' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' IS null",
                        hauptkategorieAttribute.getValue());

                final List<MetaObject> kategorieArrayElements
                        = getArrayElements(spielhalleObject, "kategorien");
                Assert.assertTrue("kategorie Array Elements available in meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "'",
                        !spielhallen.isEmpty());

                // don't use the meta object from the array: it contains referencing attributes, etc!
                final MetaObject newKategorie = connector.getMetaObject(user, kategorieArrayElements.get(0).getID(), kategorieArrayElements.get(0).getMetaClass().getID(), user.getDomain());
                Assert.assertNotNull("katagorie meta object (id:" + kategorieArrayElements.get(0).getID() + ") for meta class '" + kategorieArrayElements.get(0).getMetaClass().getTableName() + "' (id:" + kategorieArrayElements.get(0).getMetaClass().getID() + ") retrieved from server",
                        newKategorie);

                hauptkategorieAttribute.setValue(newKategorie);
                hauptkategorieAttribute.setChanged(true);

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedMetaObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedMetaObject);
                Assert.assertNotNull("reassigned hauptkategorie attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedMetaObject.getAttributeByFieldName("name"));
                Assert.assertNotNull("reassigned hauptkategorie of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedMetaObject.getAttributeByFieldName("hauptkategorie").getValue());
                Assert.assertEquals("reassigned hauptkategorie of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") set to '" + newKategorie.getName() + "'",
                        newKategorie.getName(),
                        ((MetaObject) updatedMetaObject.getAttributeByFieldName("hauptkategorie").getValue()).getName());

                this.compareMetaObjects(spielhalleObject, updatedMetaObject, true, false, true);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("reassignMetaObjectDeletedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test passed! "
                    + expectedSpielhallenCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("reassignMetaObjectDeletedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during reassignMetaObjectDeletedObjectProperty(SPH_SPIELHALLE/SPH_KATEGORIE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test updating a property of a meta object residing in a n-m array.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService12updateMetaObjectNtoMArrayProperty() throws Exception {
        try {
            LOGGER.debug("[04.12] testing updateMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");

            final int expectedCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_SPIELHALLE_KATEGORIEN");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !spielhallen.isEmpty());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {
                i++;

                final MetaObject originalSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertNotNull("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        originalSpielhalleObject);
                Assert.assertNotNull("kategorien attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("kategorien array dummy object of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien").getValue());
                Assert.assertFalse("array attribute 'kategorien' of  meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not empty",
                        getArrayElements(originalSpielhalleObject, "kategorien").isEmpty());
                // safely compare recursively, because nothing has changed!
                this.compareMetaObjects(spielhalleObject, originalSpielhalleObject, false, false, false);

                final ObjectAttribute kategorienAttribute = spielhalleObject.getAttributeByFieldName("kategorien");
                final MetaObject dummyKategorieArrayObject = (MetaObject) kategorienAttribute.getValue();
                final ObjectAttribute[] dummyKategorieArrayObjectAttributes = dummyKategorieArrayObject.getAttribs();
                // select the last intermediate arry heler object from the respective dummy arrayField
                final int kategorieObjectIndex = dummyKategorieArrayObjectAttributes.length - 1;
                final ObjectAttribute dummyKategorieArrayObjectAttribute = dummyKategorieArrayObjectAttributes[kategorieObjectIndex];
                final MetaObject intermediateKategorieArrayObject = (MetaObject) dummyKategorieArrayObjectAttribute.getValue();

                ObjectAttribute kategorieAttribute = null;
                for (final ObjectAttribute oa : intermediateKategorieArrayObject.getAttribs()) {
                    if (oa.referencesObject()) {
                        kategorieAttribute = oa;
                    }
                }

                Assert.assertNotNull("intermediate Kategorie Array Object Attribute[" + kategorieObjectIndex + "] of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") does exist",
                        kategorieAttribute);

                final MetaObject kategorieObject = (MetaObject) kategorieAttribute.getValue();
                final ObjectAttribute kategorieNameAttribute = kategorieObject.getAttributeByFieldName("name");
                final String oldKategorieName = kategorieNameAttribute.getValue().toString();
                final String updatedKategorieName = oldKategorieName + " (array updated)";

                kategorienAttribute.setChanged(true);
                dummyKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                dummyKategorieArrayObjectAttribute.setChanged(true);
                intermediateKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                kategorieAttribute.setChanged(true);
                kategorieObject.setStatus(MetaObject.MODIFIED);
                kategorieNameAttribute.setChanged(true);
                kategorieNameAttribute.setValue(updatedKategorieName);

                Assert.assertEquals("kategorie[" + kategorieObjectIndex + "] of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        getArrayElements(spielhalleObject, "kategorien").get(kategorieObjectIndex).getAttributeByFieldName("name").getValue().toString());

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                final MetaObject updatedKategorieObject = connector.getMetaObject(user, kategorieObject.getID(), kategorieObject.getMetaClass().getID(), user.getDomain());

                Assert.assertEquals("name of kategorie (" + updatedKategorieObject.getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        getArrayElements(updatedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getAttributeByFieldName("name").getValue().toString());
                Assert.assertNotNull("updated meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated kategorien attribute of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("updated kategorien of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue());
                Assert.assertEquals("name of kategorie[" + kategorieObjectIndex + "]  of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") changed from '" + oldKategorieName + "' to '" + updatedKategorieName + "'",
                        updatedKategorieName,
                        getArrayElements(updatedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getAttributeByFieldName("name").getValue().toString());

                this.compareMetaObjects((MetaObject) spielhalleObject.getAttributeByFieldName("kategorien").getValue(), (MetaObject) updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue(),
                        false, false, true);

                // revert changes!
                kategorieNameAttribute.setValue(oldKategorieName);
                response = connector.updateMetaObject(user, kategorieObject, user.getDomain());
                Assert.assertEquals("kategorie object (" + kategorieObject.getName() + ") for meta class '" + kategorieObject.getMetaClass().getTableName() + "' (id:" + kategorieObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject revertedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                Assert.assertEquals("name of kategorie[" + kategorieObjectIndex + "] of meta object #" + i + "/" + expectedCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") reverted from '" + updatedKategorieName + "' to '" + oldKategorieName + "'",
                        oldKategorieName,
                        getArrayElements(revertedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getAttributeByFieldName("name").getValue().toString());

                this.compareMetaObjects(originalSpielhalleObject, revertedSpielhalleObject, false, false, false);
            }

            final int actualCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedCount, actualCount);

            final int actualKategorienCount = countDbEntities("SPH_SPIELHALLE_KATEGORIEN", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    expectedKategorienCount, actualKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("updateMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test passed! "
                    + expectedCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test replacing a property of a meta object residing in a n-m array.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService13replaceMetaObjectNtoMArrayProperty() throws Exception {
        try {
            LOGGER.debug("[04.13] testing replaceMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN)");
            // needed for DB Triggers
            Thread.sleep(100);

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_SPIELHALLE_KATEGORIEN");
            final int expectedKategorieCount = dbEntitiesCount.get("SPH_KATEGORIE");

            final List<MetaObject> originalSpielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    originalSpielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    originalSpielhallen.size());

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    spielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    spielhallen.size());

            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    originalSpielhallen.size(),
                    spielhallen.size());

            final List<MetaObject> kategorien = this.getAllMetaObjects("SPH_KATEGORIE");
            Assert.assertFalse("SPH_KATEGORIE meta objects available",
                    kategorien.isEmpty());
            Assert.assertEquals(expectedKategorieCount + " SPH_KATEGORIE meta objects in database",
                    expectedKategorieCount,
                    kategorien.size());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {

                // don't fetch a new spielhalle object after kaegorien[] of another spielhalle object has changed
                // Beware of SPIELHALLE/BETREIBER/SPIELHALLEN[]/SPIELHALLE/KATEGORIEN[]/KATEGORIE object structure!
                //final MetaObject originalBetreiberObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                final MetaObject originalSpielhalleObject = originalSpielhallen.get(i);
                i++;

                Assert.assertNotNull("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        originalSpielhalleObject);
                Assert.assertNotNull("kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("kategorien array dummy object of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien").getValue());
                Assert.assertFalse("array attribute 'kategorien' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not empty",
                        getArrayElements(originalSpielhalleObject, "kategorien").isEmpty());

                // safely compare recursively, because originalBetreiberObject retrieved before changes were made
                this.compareMetaObjects(spielhalleObject, originalSpielhalleObject, false, false, false);

                final ObjectAttribute kategorienAttribute = spielhalleObject.getAttributeByFieldName("kategorien");
                // get the n-m dummy container object
                final MetaObject dummyKategorieArrayObject = (MetaObject) kategorienAttribute.getValue();
                // get all array entries as attributes
                final ObjectAttribute[] dummyKategorieArrayObjectAttributes = dummyKategorieArrayObject.getAttribs();
                // select the last intermediate array helper object from the respective dummy arrayField
                final int kategorieObjectIndex = dummyKategorieArrayObjectAttributes.length - 1;
                final ObjectAttribute dummyKategorieArrayObjectAttribute = dummyKategorieArrayObjectAttributes[kategorieObjectIndex];
                final MetaObject intermediateKategorieArrayObject = (MetaObject) dummyKategorieArrayObjectAttribute.getValue();

                // find the real kategorie object attribute in the intermediate array helper object's attributes
                ObjectAttribute kategorieAttribute = null;
                for (final ObjectAttribute oa : intermediateKategorieArrayObject.getAttribs()) {
                    if (oa.referencesObject()) {
                        kategorieAttribute = oa;
                    }
                }

                Assert.assertNotNull("intermediate Kategorie Array Object Attribute[" + kategorieObjectIndex + "] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") does exist",
                        kategorieAttribute);
                // get the real kategorie object from the attribute
                final MetaObject oldKategorieObject = (MetaObject) kategorieAttribute.getValue();
                // select random new kategorie object
                MetaObject newKategorieObject = oldKategorieObject;
                while (oldKategorieObject.getID() == newKategorieObject.getID()) {
                    newKategorieObject = kategorien.get(new Random().nextInt(kategorien.size()));
                }

                // important: change chain
                kategorienAttribute.setChanged(true);
                dummyKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                dummyKategorieArrayObjectAttribute.setChanged(true);
                intermediateKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                kategorieAttribute.setChanged(true);
                // actually assign a new kategorie without changing the dummy / intermediate objects
                kategorieAttribute.setValue(newKategorieObject);

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);
                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("updated kategorien of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue());

                Assert.assertEquals("kategorie[" + kategorieObjectIndex + "]  of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") replaced from '" + oldKategorieObject.getName() + "' to '" + newKategorieObject.getName() + "'",
                        getArrayElements(updatedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getId(),
                        getArrayElements(updatedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getId());

                // compare only the kategorien array 
                this.compareMetaObjects((MetaObject) spielhalleObject.getAttributeByFieldName("kategorien").getValue(), (MetaObject) updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue(),
                        false, false, true);

                // revert the changes! 
                kategorienAttribute.setChanged(true);
                dummyKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                dummyKategorieArrayObjectAttribute.setChanged(true);
                intermediateKategorieArrayObject.setStatus(MetaObject.MODIFIED);
                kategorieAttribute.setChanged(true);
                // recycle old dummy stuff!
                kategorieAttribute.setValue(oldKategorieObject);

                response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("reverted meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully retrieved from server",
                        1, response);
                final MetaObject revertedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                Assert.assertNotNull("reverted meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        revertedSpielhalleObject);
                Assert.assertNotNull("reverted kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        revertedSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("reverted kategorien of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        revertedSpielhalleObject.getAttributeByFieldName("kategorien").getValue());

                Assert.assertEquals("kategorie[" + kategorieObjectIndex + "]  of reverted meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") replaced from '" + oldKategorieObject.getName() + "' to '" + newKategorieObject.getName() + "'",
                        getArrayElements(revertedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getId(),
                        getArrayElements(revertedSpielhalleObject, "kategorien").get(kategorieObjectIndex).getId());
            }

            final List<MetaObject> revertedSpielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    revertedSpielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    revertedSpielhallen.size());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    originalSpielhallen.size(),
                    revertedSpielhallen.size());

            i = 0;
            for (final MetaObject originalSpielhalleObject : originalSpielhallen) {
                final MetaObject revertedSpielhalle = revertedSpielhallen.get(i);
                i++;

                // safely compare recursively, because originalBetreiberObject retrieved before changes were made
                // and revertedSpielhalle object retrieved after *all* changes were reverted!
                this.compareMetaObjects(originalSpielhalleObject, revertedSpielhalle, false, false, false);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_SPIELHALLE_KATEGORIEN", 3);
            Assert.assertEquals(expectedKategorienCount + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    expectedKategorienCount, actualKategorienCount);

            final int actualKategorieCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorieCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorieCount, actualKategorieCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("replaceMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test passed! "
                    + expectedSpielhallenCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("replaceMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during replaceMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test adding a property of a meta object residing in a n-m array.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService14addMetaObjectNtoMArrayProperty() throws Exception {
        try {
            LOGGER.debug("[04.14] testing addMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN)");
            // needed for DB Triggers
            Thread.sleep(100);

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_SPIELHALLE_KATEGORIEN");
            int expectedUpdatedKategorienCount = expectedKategorienCount;
            final int expectedKategorieCount = dbEntitiesCount.get("SPH_KATEGORIE");

            final List<MetaObject> originalSpielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    originalSpielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    originalSpielhallen.size());

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    spielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    spielhallen.size());

            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    originalSpielhallen.size(),
                    spielhallen.size());

            final List<MetaObject> kategorien = this.getAllMetaObjects("SPH_KATEGORIE");
            Assert.assertFalse("SPH_KATEGORIE meta objects available",
                    kategorien.isEmpty());
            Assert.assertEquals(expectedKategorieCount + " SPH_KATEGORIE meta objects in database",
                    expectedKategorieCount,
                    kategorien.size());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {

                // don't fetch a new spielhalle object after kaegorien[] of another spielhalle object has changed
                // Beware of SPIELHALLE/BETREIBER/SPIELHALLEN[]/SPIELHALLE/KATEGORIEN[]/KATEGORIE object structure!
                //final MetaObject originalBetreiberObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                final MetaObject originalSpielhalleObject = originalSpielhallen.get(i);
                i++;

                Assert.assertNotNull("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        originalSpielhalleObject);
                Assert.assertNotNull("kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("kategorien array dummy object of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien").getValue());
                Assert.assertFalse("array attribute 'kategorien' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not empty",
                        getArrayElements(originalSpielhalleObject, "kategorien").isEmpty());

                // safely compare recursively, because originalBetreiberObject retrieved before changes were made
                this.compareMetaObjects(spielhalleObject, originalSpielhalleObject, false, false, false);

                // select random new kategorie object
                final MetaObject newKategorieObject = kategorien.get(new Random().nextInt(kategorien.size()));

                // delegate adding attribute to helper method
                final int newKategorieObjectIndex = addArrayElement(spielhalleObject, "kategorien", newKategorieObject);

                // compare size after addAttribute
                final int newKategorienSize = getArrayElements(spielhalleObject, "kategorien").size();

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);
                expectedUpdatedKategorienCount++;

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("updated kategorien of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue());

                final int updatedKategorienSize = getArrayElements(updatedSpielhalleObject, "kategorien").size();
                Assert.assertEquals("server updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        newKategorienSize, updatedKategorienSize);
                Assert.assertEquals("server updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        updatedKategorienSize, (newKategorieObjectIndex + 1));

                Assert.assertEquals("kategorie[" + newKategorieObjectIndex + "]  of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") added as '" + newKategorieObject.getName() + "'",
                        getArrayElements(updatedSpielhalleObject, "kategorien").get(newKategorieObjectIndex).getName(),
                        newKategorieObject.getName());

                // compare only the kategorien array 
                this.compareMetaObjects((MetaObject) spielhalleObject.getAttributeByFieldName("kategorien").getValue(), (MetaObject) updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue(),
                        false, true, true);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_SPIELHALLE_KATEGORIEN", 3);
            Assert.assertEquals((expectedKategorienCount + i) + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    (expectedKategorienCount + i), actualKategorienCount);
            Assert.assertEquals(expectedUpdatedKategorienCount + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    expectedUpdatedKategorienCount, actualKategorienCount);

            final int actualKategorieCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorieCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorieCount, actualKategorieCount);

            // update for next test!
            dbEntitiesCount.put("SPH_SPIELHALLE_KATEGORIEN", expectedUpdatedKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("addMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test passed! "
                    + expectedSpielhallenCount + " meta objects updated, "
                    + i + " array entries added");

        } catch (AssertionError ae) {
            LOGGER.error("addMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during addMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @Test
    public void test04objectService15removeMetaObjectNtoMArrayProperty() throws Exception {
        try {
            LOGGER.debug("[04.15] testing removeMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN)");
            // needed for DB Triggers
            Thread.sleep(100);

            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            final int expectedKategorienCount = dbEntitiesCount.get("SPH_SPIELHALLE_KATEGORIEN");
            int expectedUpdatedKategorienCount = expectedKategorienCount;
            final int expectedKategorieCount = dbEntitiesCount.get("SPH_KATEGORIE");

            final List<MetaObject> originalSpielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    originalSpielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    originalSpielhallen.size());

            final List<MetaObject> spielhallen = this.getAllMetaObjects("SPH_SPIELHALLE");
            Assert.assertFalse("SPH_SPIELHALLE meta objects available",
                    spielhallen.isEmpty());
            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    expectedSpielhallenCount,
                    spielhallen.size());

            Assert.assertEquals(expectedSpielhallenCount + " SPH_SPIELHALLE meta objects in database",
                    originalSpielhallen.size(),
                    spielhallen.size());

            final List<MetaObject> kategorien = this.getAllMetaObjects("SPH_KATEGORIE");
            Assert.assertFalse("SPH_KATEGORIE meta objects available",
                    kategorien.isEmpty());
            Assert.assertEquals(expectedKategorieCount + " SPH_KATEGORIE meta objects in database",
                    expectedKategorieCount,
                    kategorien.size());

            int i = 0;
            for (final MetaObject spielhalleObject : spielhallen) {

                // don't fetch a new spielhalle object after kaegorien[] of another spielhalle object has changed
                // Beware of SPIELHALLE/BETREIBER/SPIELHALLEN[]/SPIELHALLE/KATEGORIEN[]/KATEGORIE object structure!
                //final MetaObject originalBetreiberObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                final MetaObject originalSpielhalleObject = originalSpielhallen.get(i);
                i++;

                Assert.assertNotNull("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        originalSpielhalleObject);
                Assert.assertNotNull("kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("kategorien array dummy object of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        originalSpielhalleObject.getAttributeByFieldName("kategorien").getValue());
                final List<MetaObject> arrayElements = getArrayElements(originalSpielhalleObject, "kategorien");
                Assert.assertFalse("array attribute 'kategorien' of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' is not empty",
                        arrayElements.isEmpty());

                // safely compare recursively, because originalBetreiberObject retrieved before changes were made
                this.compareMetaObjects(spielhalleObject, originalSpielhalleObject, false, false, false);

                final int arrayElementIndex = arrayElements.size() - 1;
                final int oldKategorieSize = arrayElements.size();
                final MetaObject arrayElement = arrayElements.get(arrayElementIndex);

                final MetaObject removedArrayElement = removeArrayElement(arrayElement, "kategorien", arrayElementIndex);

                Assert.assertEquals("array element kategorien[" + arrayElementIndex + "] of  meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' removed (id:" + arrayElement.getId() + ")",
                        arrayElement.getId(),
                        removedArrayElement.getId());
                compareMetaObjects(arrayElement, removedArrayElement, false, false, false);

                // compare size after removeAttribute
                final int newKategorienSize = getArrayElements(spielhalleObject, "kategorien").size();
                Assert.assertEquals("updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        oldKategorieSize - 1, newKategorienSize);
                Assert.assertEquals("updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        newKategorienSize, newKategorienSize);

                int response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                expectedUpdatedKategorienCount--;

                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());
                Assert.assertNotNull("updated meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") retrieved from server",
                        updatedSpielhalleObject);
                Assert.assertNotNull("updated kategorien attribute of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien"));
                Assert.assertNotNull("updated kategorien of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") is not null",
                        updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue());

                final int updatedKategorienSize = getArrayElements(updatedSpielhalleObject, "kategorien").size();
                Assert.assertEquals("server updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        newKategorienSize, updatedKategorienSize);
                Assert.assertEquals("server updated kategorien[] of meta object #" + i + "/" + expectedSpielhallenCount + " (id:" + spielhalleObject.getID() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") size is " + newKategorienSize,
                        updatedKategorienSize, arrayElementIndex);

                // compare only the kategorien array 
                this.compareMetaObjects((MetaObject) spielhalleObject.getAttributeByFieldName("kategorien").getValue(), (MetaObject) updatedSpielhalleObject.getAttributeByFieldName("kategorien").getValue(),
                        false, false, true);
            }

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            final int actualKategorienCount = countDbEntities("SPH_SPIELHALLE_KATEGORIEN", 3);
            Assert.assertEquals((expectedKategorienCount - i) + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    (expectedKategorienCount - i), actualKategorienCount);
            Assert.assertEquals(expectedUpdatedKategorienCount + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    expectedUpdatedKategorienCount, actualKategorienCount);

            final int actualKategorieCount = countDbEntities("SPH_KATEGORIE", 3);
            Assert.assertEquals(expectedKategorieCount + " 'SPH_KATEGORIE' entities in Integration Base",
                    expectedKategorieCount, actualKategorieCount);

            // update for next test!
            dbEntitiesCount.put("SPH_SPIELHALLE_KATEGORIEN", expectedUpdatedKategorienCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("removeMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test passed! "
                    + expectedSpielhallenCount + " meta objects updated, "
                    + i + " array entries removed");

        } catch (AssertionError ae) {
            LOGGER.error("removeMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during removeMetaObjectNtoMArrayProperty(SPH_SPIELHALLE/SPH_SPIELHALLE_KATEGORIEN): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Test updating a property of a meta object residing in a n-m array.
     *
     * @throws Exception
     */
    @Test
    public void test04objectService16updateMetaObject1toNArrayProperty() throws Exception {
        try {
            LOGGER.debug("[04.16] testing updateMetaObject1toNArrayProperty(SPH_BETRIEBER/SPH_SPIELHALLE)");
            // needed for DB Triggers
            Thread.sleep(100);

            final List<MetaObject> betreiberList = this.getAllMetaObjects("SPH_BETREIBER");
            final List<MetaObject> originalBetreiberList = new ArrayList<MetaObject>(betreiberList.size());

            final int expectedBetreiberCount = dbEntitiesCount.get("SPH_BETREIBER");
            Assert.assertTrue("SPH_BETREIBER meta objects available",
                    !betreiberList.isEmpty());
            final int expectedSpielhallenCount = dbEntitiesCount.get("SPH_SPIELHALLE");
            Assert.assertTrue("SPH_SPIELHALLE meta objects available",
                    !betreiberList.isEmpty());

            int i = 0;
            for (final MetaObject betreiberObject : betreiberList) {
                i++;

                final MetaObject originalBetreiberObject = connector.getMetaObject(user, betreiberObject.getID(), betreiberObject.getMetaClass().getID(), user.getDomain());
                originalBetreiberList.add(originalBetreiberObject);

                Assert.assertNotNull("meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") retrieved from server",
                        originalBetreiberObject);
                Assert.assertNotNull("spielhallen attribute of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") is not null",
                        originalBetreiberObject.getAttributeByFieldName("spielhallen"));
                Assert.assertNotNull("spielhallen array dummy object of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") is not null",
                        originalBetreiberObject.getAttributeByFieldName("spielhallen").getValue());
                Assert.assertFalse("array attribute 'spielhallen' of  meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' is not empty",
                        getArrayElements(originalBetreiberObject, "spielhallen").isEmpty());
                // safely compare recursively, because nothing has changed!
                this.compareMetaObjects(betreiberObject, originalBetreiberObject, false, false, false);

                final ObjectAttribute spielhallenAttribute = betreiberObject.getAttributeByFieldName("spielhallen");
                final MetaObject dummySpielhalleArrayObject = (MetaObject) spielhallenAttribute.getValue();
                final ObjectAttribute[] dummySpielhalleArrayObjectAttributes = dummySpielhalleArrayObject.getAttribs();

                Assert.assertEquals("array attribute 'spielhallen' of  meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' contains " + dummySpielhalleArrayObjectAttributes.length + " elements",
                        dummySpielhalleArrayObjectAttributes.length,
                        getArrayElements(betreiberObject, "spielhallen").size());

                // select the last intermediate array heler object from the respective dummy arrayField
                final int spielhalleObjectIndex = dummySpielhalleArrayObjectAttributes.length - 1;
                final ObjectAttribute dummySpielhalleArrayObjectAttribute = dummySpielhalleArrayObjectAttributes[spielhalleObjectIndex];
                final MetaObject spielhalleObject = (MetaObject) dummySpielhalleArrayObjectAttribute.getValue();

                final ObjectAttribute spielhalleNameAttribute = spielhalleObject.getAttributeByFieldName("name");
                final String oldSpielhalleName = spielhalleNameAttribute.getValue().toString();
                final String updatedSpielhalleName = oldSpielhalleName + " (betreiber array updated)";

                spielhallenAttribute.setChanged(true);
                dummySpielhalleArrayObject.setStatus(MetaObject.MODIFIED);
                dummySpielhalleArrayObjectAttribute.setChanged(true);
                spielhalleObject.setStatus(MetaObject.MODIFIED);
                spielhalleNameAttribute.setChanged(true);
                spielhalleNameAttribute.setValue(updatedSpielhalleName);

                Assert.assertEquals("spielhalle[" + spielhalleObjectIndex + "] of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") changed from '" + oldSpielhalleName + "' to '" + updatedSpielhalleName + "'",
                        updatedSpielhalleName,
                        getArrayElements(betreiberObject, "spielhallen").get(spielhalleObjectIndex).getAttributeByFieldName("name").getValue().toString());

                int response = connector.updateMetaObject(user, betreiberObject, user.getDomain());
                Assert.assertEquals("meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject updatedBetreiberObject = connector.getMetaObject(user, betreiberObject.getID(), betreiberObject.getMetaClass().getID(), user.getDomain());
                final MetaObject updatedSpielhalleObject = connector.getMetaObject(user, spielhalleObject.getID(), spielhalleObject.getMetaClass().getID(), user.getDomain());

                Assert.assertEquals("name of spielhalle (" + updatedSpielhalleObject.getID() + ") changed from '" + oldSpielhalleName + "' to '" + updatedSpielhalleName + "'",
                        updatedSpielhalleName,
                        getArrayElements(updatedSpielhalleObject, "spielhallen").get(spielhalleObjectIndex).getAttributeByFieldName("name").getValue().toString());
                Assert.assertNotNull("updated meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") retrieved from server",
                        updatedBetreiberObject);
                Assert.assertNotNull("updated spielhallen attribute of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") is not null",
                        updatedBetreiberObject.getAttributeByFieldName("spielhallen"));
                Assert.assertNotNull("updated spielhallen of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") is not null",
                        updatedBetreiberObject.getAttributeByFieldName("spielhallen").getValue());
                Assert.assertEquals("name of spielhalle[" + spielhalleObjectIndex + "]  of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") changed from '" + oldSpielhalleName + "' to '" + updatedSpielhalleName + "'",
                        updatedSpielhalleName,
                        getArrayElements(updatedBetreiberObject, "spielhallen").get(spielhalleObjectIndex).getAttributeByFieldName("name").getValue().toString());

                this.compareMetaObjects((MetaObject) betreiberObject.getAttributeByFieldName("spielhallen").getValue(), (MetaObject) updatedSpielhalleObject.getAttributeByFieldName("spielhallen").getValue(),
                        true, false, true);

                // revert changes!
                spielhalleNameAttribute.setValue(oldSpielhalleName);
                response = connector.updateMetaObject(user, spielhalleObject, user.getDomain());
                Assert.assertEquals("spielhalle object (" + spielhalleObject.getName() + ") for meta class '" + spielhalleObject.getMetaClass().getTableName() + "' (id:" + spielhalleObject.getMetaClass().getID() + ") successfully updated from server",
                        1, response);

                final MetaObject revertedBetreiberObject = connector.getMetaObject(user, betreiberObject.getID(), betreiberObject.getMetaClass().getID(), user.getDomain());
                Assert.assertEquals("name of spielhalle[" + spielhalleObjectIndex + "] of meta object #" + i + "/" + expectedBetreiberCount + " (id:" + betreiberObject.getID() + ") for meta class '" + betreiberObject.getMetaClass().getTableName() + "' (id:" + betreiberObject.getMetaClass().getID() + ") reverted from '" + updatedSpielhalleName + "' to '" + oldSpielhalleName + "'",
                        oldSpielhalleName,
                        getArrayElements(revertedBetreiberObject, "spielhallen").get(spielhalleObjectIndex).getAttributeByFieldName("name").getValue().toString());

            }

            final List<MetaObject> revertedBetreiberList = this.getAllMetaObjects("SPH_BETREIBER");
            Assert.assertEquals("reverted " + originalBetreiberList.size() + " spielhallen",
                    originalBetreiberList.size(),
                    revertedBetreiberList.size());

            // compare after all updates have been reverted!
            int j = 0;
            for (final MetaObject originalBetreiber : originalBetreiberList) {
                this.compareMetaObjects(originalBetreiber, revertedBetreiberList.get(j), false, false, false);
                j++;
            }

            final int actualCount = countDbEntities("SPH_SPIELHALLE", 3);
            Assert.assertEquals(expectedBetreiberCount + " 'SPH_SPIELHALLE' entities in Integration Base",
                    expectedBetreiberCount, actualCount);

            final int actualSpielhallenCount = countDbEntities("SPH_SPIELHALLE_KATEGORIEN", 3);
            Assert.assertEquals(expectedSpielhallenCount + " 'SPH_SPIELHALLE_KATEGORIE' entities in Integration Base",
                    expectedSpielhallenCount, actualSpielhallenCount);

            // needed for DB Triggers
            Thread.sleep(100);
            LOGGER.info("updateMetaObject1toNArrayProperty(SPH_BETRIEBER/SPH_SPIELHALLE) test passed! "
                    + expectedBetreiberCount + " meta objects updated");

        } catch (AssertionError ae) {
            LOGGER.error("updateMetaObject1toNArrayProperty(SPH_BETRIEBER/SPH_SPIELHALLE) test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateMetaObject1toNArrayProperty(SPH_BETRIEBER/SPH_SPIELHALLE): " + ex.getMessage(), ex);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HELPER METHODS ----------------------------------------------------------">
    /**
     *
     * @param tableName
     * @param retries
     * @return
     * @throws IOError
     * @throws java.lang.ClassNotFoundException
     * @throws SQLException
     */
    protected static int countDbEntities(final String tableName, int retries) throws SQLException, ClassNotFoundException {
        if (retries > 0) {
            retries--;
            try {
                return RESTfulInterfaceTest.countDbEntities(jdbcConnection, tableName);
            } catch (final SQLException sqlException) {
                LOGGER.warn("database connection (" + tableName + ") closed unexpectedly"
                        + "(closed:" + jdbcConnection.isClosed() + "), trying to reconnect (#" + retries + ")", sqlException);
                jdbcConnection = createJdbcConnection();
                return countDbEntities(tableName, retries);
            }
        } else {
            throw new SQLException("could not establish connection to integration base, giving up!");
        }
    }

    /**
     * Helper Method for adding array elements
     *
     * @param parentObject
     * @param arrayFieldName
     * @param arrayElementObject
     * @return
     */
    protected int addArrayElement(final MetaObject parentObject,
            final String arrayFieldName,
            final MetaObject arrayElementObject) {

        final ObjectAttribute arrayField = parentObject.getAttributeByFieldName(arrayFieldName);
        Assert.assertNotNull("'" + arrayFieldName + "' attribute available in meta object '" + parentObject.getName() + "'",
                arrayField);
        Assert.assertTrue("'" + arrayFieldName + "' attribute is array",
                arrayField.isArray());
        // TODO: create new dummy container object if value is null
        // See 
        Assert.assertNotNull("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is not null",
                arrayField.getValue());
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is MetaObject",
                MetaObject.class.isAssignableFrom(arrayField.getValue().getClass()));

        final MetaObject arrayContainerDummyObject = (MetaObject) arrayField.getValue();
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is array Container Dummy Object",
                arrayContainerDummyObject.isDummy());

        // 1-n array
        if (arrayField.getMai().isVirtual() && arrayField.getMai().getForeignKeyClassId() < 0) {
            Assert.assertTrue("FIXME: implement operation", false);
            return -1;

        } else {

            final int oldArraySize = getArrayElements(parentObject, arrayFieldName).size();
            final int newArrayElementObjectIndex = arrayContainerDummyObject.getAttribs().length;
            Assert.assertEquals(arrayFieldName + "[] of meta object '" + parentObject + "' (" + parentObject.getID() + ") size is " + oldArraySize,
                    newArrayElementObjectIndex, oldArraySize);

            final int intermediateArrayElementClassClassId = arrayField.getMai().getForeignKeyClassId();
            Assert.assertTrue("array Helper Object ClassId  of meta object '" + parentObject + "' (" + parentObject.getID() + ") is not negative",
                    intermediateArrayElementClassClassId >= 0);

            final MetaClass intermediateArrayElementClass
                    = MetaClassCache.getInstance().getMetaClass(parentObject.getDomain(),
                            intermediateArrayElementClassClassId);
            Assert.assertNotNull("intermediate Array Element Class (" + arrayField.getMai().getForeignKeyClassId() + ") for '" + arrayFieldName + "' array found",
                    intermediateArrayElementClass);

            final MetaObject intermediateArrayElementObject = intermediateArrayElementClass.getEmptyInstance();
            Assert.assertNotNull("intermediate Array Element Object (" + intermediateArrayElementClass.getName() + ") for '" + arrayFieldName + "' array created",
                    intermediateArrayElementClass);

            intermediateArrayElementObject.setStatus(MetaObject.NEW);

            final ObjectAttribute arrayContainerDummyObjectAttribute = new ObjectAttribute(
                    arrayField.getMai().getId()
                    + "." // NOI18N
                    + newArrayElementObjectIndex,
                    arrayField.getMai(),
                    intermediateArrayElementObject.getID(),
                    intermediateArrayElementObject,
                    intermediateArrayElementClass.getAttributePolicy());

            Assert.assertTrue("dummay array object '" + arrayFieldName + "' is array: ",
                    arrayContainerDummyObjectAttribute.isArray());
            Assert.assertFalse("dummay array object '" + arrayFieldName + "' is n-m array: "
                    + "arrayContainerDummyAttribute.isVirtualOneToManyAttribute() = false",
                    arrayContainerDummyObjectAttribute.isVirtualOneToManyAttribute());

            // copied from Persistance Manager. Should be centralized. See #171
            arrayContainerDummyObjectAttribute.setOptional(arrayField.getMai().isOptional());
            arrayContainerDummyObjectAttribute.setVisible(arrayField.getMai().isVisible());
            intermediateArrayElementObject.setReferencingObjectAttribute(arrayContainerDummyObjectAttribute);
            arrayContainerDummyObjectAttribute.setParentObject(arrayContainerDummyObject);
            arrayContainerDummyObjectAttribute.setClassKey(
                    arrayField.getMai().getForeignKeyClassId() + "@" + intermediateArrayElementClass.getDomain()); // NOI18N

            final ObjectAttribute[] intermediateArrayElementObjectAttributes = intermediateArrayElementObject.getAttribs();
            ObjectAttribute arrayElementAttribute = null;
            ObjectAttribute arrayReferenceAttribute = null;

            for (final ObjectAttribute intermediateArrayElementObjectAttribute : intermediateArrayElementObjectAttributes) {
                Assert.assertNotNull("class key of intermediateArrayElementObjectAttribute '" + intermediateArrayElementObjectAttribute.getName() + "' is not null",
                        intermediateArrayElementObjectAttribute.getClassKey());

                if (intermediateArrayElementObjectAttribute.referencesObject()) // arrayElement
                {
                    arrayElementAttribute = intermediateArrayElementObjectAttribute;
                } else if (!intermediateArrayElementObjectAttribute.isPrimaryKey()) {
                    arrayReferenceAttribute = intermediateArrayElementObjectAttribute;
                }
            }

            Assert.assertNotNull("actual array entry attribute of new array intermediate meta object of meta class '" + intermediateArrayElementClass.getName() + "'  not null",
                    arrayElementAttribute);
            Assert.assertNotNull("back reference array attribute of new array intermediate meta object of meta class '" + intermediateArrayElementClass.getName() + "'  not null",
                    arrayReferenceAttribute);

            arrayElementAttribute.setValue(arrayElementObject);
            arrayElementAttribute.setChanged(true);

            arrayReferenceAttribute.setValue(parentObject.getID());
            arrayReferenceAttribute.setChanged(true);

            arrayContainerDummyObject.addAttribute(arrayContainerDummyObjectAttribute);
            arrayContainerDummyObject.setStatus(MetaObject.MODIFIED);

            // compare size after addAttribute
            final int newArraySize = getArrayElements(parentObject, arrayFieldName).size();
            Assert.assertEquals("updated '" + arrayFieldName + "[]' of meta object '" + parentObject + "' (" + parentObject.getID() + ")  size is " + newArraySize,
                    newArraySize, (oldArraySize + 1));

            arrayField.setChanged(true);
            parentObject.setChanged(true);

            return newArrayElementObjectIndex;

        }

    }

    protected MetaObject removeArrayElement(final MetaObject parentObject,
            final String arrayFieldName,
            final int arrayElementIndex) {

        final ObjectAttribute arrayField = parentObject.getAttributeByFieldName(arrayFieldName);
        Assert.assertNotNull("'" + arrayFieldName + "' attribute available in meta object '" + parentObject.getName() + "'",
                arrayField);
        Assert.assertTrue("'" + arrayFieldName + "' attribute is array",
                arrayField.isArray());

        Assert.assertNotNull("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is not null",
                arrayField.getValue());
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is MetaObject",
                MetaObject.class.isAssignableFrom(arrayField.getValue().getClass()));

        final MetaObject arrayContainerDummyObject = (MetaObject) arrayField.getValue();
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is array Container Dummy Object",
                arrayContainerDummyObject.isDummy());

        // 1-n array
        if (arrayField.getMai().isVirtual() && arrayField.getMai().getForeignKeyClassId() < 0) {
            Assert.assertTrue("FIXME: implement operation", false);
            return null;

        } else {

            final int oldArraySize = getArrayElements(parentObject, arrayFieldName).size();
            final ObjectAttribute[] arrayContainerDummyObjectAttributes = arrayContainerDummyObject.getAttribs();
            Assert.assertEquals(arrayFieldName + "[] of meta object '" + parentObject + "' (" + parentObject.getID() + ") size is " + oldArraySize,
                    arrayContainerDummyObjectAttributes.length, oldArraySize);
            Assert.assertTrue(arrayFieldName + "[] of meta object '" + parentObject + "' (" + parentObject.getID() + ") size is > " + arrayElementIndex,
                    oldArraySize > arrayElementIndex && arrayElementIndex >= 0);

            final MetaObject arrayElement = getArrayElements(parentObject, arrayFieldName).get(arrayElementIndex);
            final ObjectAttribute arrayContainerDummyObjectAttribute = arrayContainerDummyObjectAttributes[arrayElementIndex];
            arrayContainerDummyObject.removeAttribute(arrayContainerDummyObjectAttribute);
            arrayContainerDummyObject.setChanged(true);

            final int newArraySize = getArrayElements(parentObject, arrayFieldName).size();
            Assert.assertEquals(arrayFieldName + "[] of meta object '" + parentObject + "' (" + parentObject.getID() + ") size is " + newArraySize,
                    arrayContainerDummyObject.getAttribs().length, newArraySize);

            return arrayElement;
        }
    }

    /**
     *
     * @param parentObject
     * @param arrayFieldName
     * @return
     * @throws AssertionError
     */
    protected List<MetaObject> getArrayElements(final MetaObject parentObject,
            final String arrayFieldName) throws AssertionError {

        final ObjectAttribute arrayField = parentObject.getAttributeByFieldName(arrayFieldName);
        Assert.assertNotNull("'" + arrayFieldName + "' attribute available in meta object '" + parentObject.getName() + "'",
                arrayField);
        // WARNING: isArrayis not set on 1-n array attributes!
        //Assert.assertTrue("'" + arrayFieldName + "' attribute is array",
        //        arrayField.isArray());

        if (arrayField.isArray()) {
            Assert.assertTrue("dummay array object '" + arrayFieldName + "' is n-m array: "
                    + "arrayField.getMai().getForeignKeyClassId() >= 0 = true",
                    arrayField.getMai().getForeignKeyClassId() >= 0);
        } else {
            Assert.assertTrue("dummay array object '" + arrayFieldName + "' is 1-n array: "
                    + "arrayField.getMai().getForeignKeyClassId() < 0 = true",
                    arrayField.getMai().getForeignKeyClassId() < 0);
        }

        Assert.assertNotNull("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is not null",
                arrayField.getValue());
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is MetaObject",
                MetaObject.class.isAssignableFrom(arrayField.getValue().getClass()));

        final MetaObject arrayContainerDummyObject = (MetaObject) arrayField.getValue();
        Assert.assertTrue("value of '" + arrayFieldName + "' attribute in meta object '" + parentObject.getName() + "' is array Container Dummy Object",
                arrayContainerDummyObject.isDummy());

        final ObjectAttribute[] arrayContainerDummyAttributes = arrayContainerDummyObject.getAttribs();
        final List<MetaObject> arrayElementObjects = new ArrayList<MetaObject>();
        int i = 0;

        for (final ObjectAttribute arrayContainerDummyAttribute : arrayContainerDummyAttributes) {
            i++;
            Assert.assertNotNull("value of '" + arrayFieldName + "' attribute's intermediate Array Element Object #" + i + " in meta object '" + parentObject.getName() + "' is not null",
                    arrayContainerDummyAttribute.getValue());
            Assert.assertTrue("value of '" + arrayFieldName + "' attribute's intermediate Array Element Object #" + i + " in meta object '" + parentObject.getName() + "' is MetaObject",
                    MetaObject.class.isAssignableFrom(arrayContainerDummyAttribute.getValue().getClass()));
            final MetaObject arrayElementObject = (MetaObject) arrayContainerDummyAttribute.getValue();

            // n-m: process intermediate objects
            if (arrayContainerDummyAttribute.isArray()) {
                Assert.assertFalse("dummay array object '" + arrayFieldName + "' is n-m array: "
                        + "arrayContainerDummyAttribute.isVirtualOneToManyAttribute() = false",
                        arrayContainerDummyAttribute.isVirtualOneToManyAttribute());
                Assert.assertFalse("dummay array object '" + arrayFieldName + "' is n-m array: "
                        + "arrayField.getMai().isVirtual() = false",
                        arrayField.getMai().isVirtual());
                Assert.assertFalse("dummay array object '" + arrayFieldName + "' is n-m array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = false",
                        arrayField.getMai().getForeignKeyClassId() < 0);
                Assert.assertEquals("intermediate Array Element Object #" + i + " is instance of class " + arrayField.getMai().getForeignKeyClassId(),
                        arrayField.getMai().getForeignKeyClassId(),
                        arrayElementObject.getMetaClass().getID());

                final ObjectAttribute[] intermediateArrayElementObjectAttributes = arrayElementObject.getAttribs();
                for (final ObjectAttribute intermediateArrayElementObjectAttribute : intermediateArrayElementObjectAttributes) {
                    if (intermediateArrayElementObjectAttribute.referencesObject()) {
                        Assert.assertNotNull("value of '" + arrayFieldName + "' attribute's intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is not null",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue("value of '" + arrayFieldName + "' attribute's intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is MetaObject",
                                MetaObject.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));
                        arrayElementObjects.add((MetaObject) intermediateArrayElementObjectAttribute.getValue());
                    } else if (!intermediateArrayElementObjectAttribute.isPrimaryKey()) {
                        Assert.assertEquals("array attribute's " + arrayFieldName + "' ArrayKeyFieldName matches intermediate Array Element Object Attribute FieldName in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is MetaObject",
                                arrayContainerDummyAttribute.getMai().getArrayKeyFieldName().toLowerCase(),
                                intermediateArrayElementObjectAttribute.getMai().getFieldName().toLowerCase());
                        Assert.assertNotNull("array attribute's " + arrayFieldName + "' ArrayKeyField '" + intermediateArrayElementObjectAttribute.getMai().getFieldName() + "' value is not null in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is MetaObject",
                                intermediateArrayElementObjectAttribute.getValue());
                        Assert.assertTrue("array attribute's " + arrayFieldName + "' ArrayKeyField '" + intermediateArrayElementObjectAttribute.getMai().getFieldName() + "' value is Integer in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is MetaObject",
                                Integer.class.isAssignableFrom(intermediateArrayElementObjectAttribute.getValue().getClass()));
                        Assert.assertEquals("array attribute's " + arrayFieldName + "' ArrayKeyField '" + intermediateArrayElementObjectAttribute.getMai().getFieldName() + "' value matches parent object id '" + parentObject.getId() + "' in intermediate Array Element Object #" + i + "'s attribute '" + intermediateArrayElementObjectAttribute.getName() + "' in meta object '" + parentObject.getName() + "' is MetaObject",
                                parentObject.getID(),
                                ((Integer) intermediateArrayElementObjectAttribute.getValue()).intValue());

                    }
                }
            } else {
                Assert.assertTrue("dummay array object '" + arrayFieldName + "' is 1-n array: "
                        + "arrayContainerDummyAttribute.isVirtualOneToManyAttribute() = true",
                        arrayContainerDummyAttribute.isVirtualOneToManyAttribute());
                Assert.assertTrue("dummay array object '" + arrayFieldName + "' is 1-n array: "
                        + "arrayField.getMai().isVirtual() = true",
                        arrayField.getMai().isVirtual());
                Assert.assertTrue("dummay array object '" + arrayFieldName + "' is 1-n array: "
                        + "arrayField.getMai().getForeignKeyClassId() < 0 = true",
                        arrayField.getMai().getForeignKeyClassId() < 0);
                arrayElementObjects.add(arrayElementObject);
            }
        }

        Assert.assertEquals(arrayContainerDummyAttributes.length + " elementes in array attribute '" + arrayFieldName + "' in meta object '" + parentObject.getName() + "'",
                arrayContainerDummyAttributes.length, arrayElementObjects.size());

        return arrayElementObjects;
    }

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

        objectHierarchy.add(expectedMetaObject.getClassKey());

        Assert.assertEquals("expected MetaObject [" + name + "].getClassID() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getClassID(),
                actualMetaObject.getClassID());

        Assert.assertEquals("expected MetaObject [" + name + "].getClassKey() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.getClassKey(),
                actualMetaObject.getClassKey());

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

            Assert.assertEquals("expected MetaObject [" + name + "].getStatus() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getStatus(),
                    actualMetaObject.getStatus());

            Assert.assertEquals("expected MetaObject [" + name + "].getStatusDebugString() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getStatusDebugString(),
                    actualMetaObject.getStatusDebugString());
        }

        Assert.assertEquals("expected MetaObject [" + name + "].hasObjectWritePermission(user) matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.hasObjectWritePermission(user),
                actualMetaObject.hasObjectWritePermission(user));

        RESTfulInterfaceTest.compareMetaClasses(expectedMetaObject.getMetaClass(), actualMetaObject.getMetaClass());

        if (expectedMetaObject.getReferencingObjectAttribute() == null) {
            // setReferencingObjectAttribute not neccessariliy called by client but by server.
            if (!compareNew && !compareChanged) {
                Assert.assertNull("expected MetaObject [" + name + "].getReferencingObjectAttribute() is null",
                        actualMetaObject.getReferencingObjectAttribute());
            }
        } else {
            Assert.assertNotNull("actualMetaObject MetaObject [" + name + "].getReferencingObjectAttribute() is not null",
                    actualMetaObject.getReferencingObjectAttribute());

            // always limit recursion in referencing oa
            this.compareObjectAttributes(
                    expectedMetaObject.getReferencingObjectAttribute(),
                    actualMetaObject.getReferencingObjectAttribute(),
                    true,
                    objectHierarchy,
                    compareNew,
                    compareChanged);
        }

        Assert.assertEquals("expected MetaObject [" + name + "].toString() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                expectedMetaObject.toString(),
                actualMetaObject.toString());

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

        // move property string comparision to the end because it fails too often :(
        if (!compareNew && !compareChanged) {
            //            if (LOGGER.isDebugEnabled()) {
            //                LOGGER.debug(expectedMetaObject.getPropertyString());
            //                LOGGER.debug(actualMetaObject.getPropertyString());
            //            }

            Assert.assertEquals("expected MetaObject [" + name + "].getPropertyString() matches actual MetaObject (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.getPropertyString(),
                    actualMetaObject.getPropertyString());

            Assert.assertTrue("expected MetaObject [" + name + "].propertyEquals(actualMetaObject) (" + this.getHierarchyPath(objectHierarchy) + ")",
                    expectedMetaObject.propertyEquals(actualMetaObject));
        }
    }

    /**
     * Compares ObjectAttributes. If recursive is true and the object value is a
     * parentObject, compareMetaObjects is invoked and compareNew and
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
//        Assert.assertEquals("expected kategorienAttribute[" + name + "].getJavaType()  matches actual ObjectAttribute  (" + this.getHierarchyPath(objectHierarchy) + ")",
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
            Assert.assertNotNull("actual objectAttribute[" + name + "] value (" + expectedObjectAttributeValue + ") is not null (" + this.getHierarchyPath(objectHierarchy) + ")",
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

                // if recursion shall be limited check if an object of the
                // same type exists already in the hierarchy (parent object)
                if (!limitRecursion || !objectHierarchy.contains(((MetaObject) expectedObjectAttributeValue).getClassKey())) {
                    // recursively compare meta objects
                    this.compareMetaObjects(
                            (MetaObject) expectedObjectAttributeValue,
                            (MetaObject) actualObjectAttributeValue,
                            limitRecursion,
                            new ArrayList<String>(objectHierarchy),
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

    protected static Connection createJdbcConnection() throws ClassNotFoundException, SQLException {
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
        final Connection connection = DriverManager.getConnection(
                integrationBaseUrl,
                PROPERTIES.getProperty("integrationbase.username", "postgres"),
                PROPERTIES.getProperty("integrationbase.password", "postgres"));
        connection.setReadOnly(true);

        return connection;
    }

    // </editor-fold>
}
