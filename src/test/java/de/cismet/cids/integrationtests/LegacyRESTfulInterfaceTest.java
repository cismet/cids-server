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
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
    @Ignore
    @UseDataProvider("getMetaClassIds")
    public void test03classService03getInstance(final Integer classId) throws Exception {
        LOGGER.debug("[03.03] testing getInstance(" + classId + ")");
        try {
            final MetaClass metaClass = MetaClassCache.getInstance().getMetaClass(user.getDomain(), classId);
            Assert.assertNotNull("meta class '" + classId + "' from meta class cache not null", metaClass);

            // FIXME: ObjectFactory().getInstance() fails with NPE for Classes SPH_SPIELHALLE and SPH_Betreiber
            final Iterator iter = Collections.synchronizedCollection(metaClass.getMemberAttributeInfos().values()).iterator();
            final MetaObject metaObjectFromService = connector.getInstance(user, metaClass);
            Assert.assertNotNull("new meta object of meta class '" + classId + "' from service not null",
                    metaObjectFromService);

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

    protected void compareMetaObjects(
            final MetaObject expectedMetaObject,
            final MetaObject actualMetaObject) throws AssertionError {

        final int pk = expectedMetaObject.getId();
        final String name = expectedMetaObject.getAttribute("name") != null
                ? expectedMetaObject.getAttribute("name").toString()
                : String.valueOf(pk);

        Assert.assertEquals("expected MetaObject" + name + "].getClassID() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getClassID(),
                actualMetaObject.getClassID());

        Assert.assertEquals("expected MetaObject" + name + "].getClassKey() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getClassKey(),
                actualMetaObject.getClassKey());

        Assert.assertEquals("expected MetaObject" + name + "].getComplexEditor() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getComplexEditor(),
                actualMetaObject.getComplexEditor());

        Assert.assertEquals("expected MetaObject" + name + "].getDescription() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getDescription(),
                actualMetaObject.getDescription());

        Assert.assertEquals("expected MetaObject" + name + "].getDomain() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getDomain(),
                actualMetaObject.getDomain());

        Assert.assertEquals("expected MetaObject" + name + "].getEditor() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getEditor(),
                actualMetaObject.getEditor());

        Assert.assertEquals("expected MetaObject" + name + "].getGroup() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getGroup(),
                actualMetaObject.getGroup());

        Assert.assertEquals("expected MetaObject" + name + "].getId() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getId(),
                actualMetaObject.getId());

        Assert.assertEquals("expected MetaObject" + name + "].getID() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getID(),
                actualMetaObject.getID());

        Assert.assertEquals("expected MetaObject" + name + "].getKey() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getKey(),
                actualMetaObject.getKey());

        Assert.assertEquals("expected MetaObject" + name + "].getName() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getName(),
                actualMetaObject.getName());

        Assert.assertEquals("expected MetaObject" + name + "].getPropertyString() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getPropertyString(),
                actualMetaObject.getPropertyString());

        Assert.assertEquals("expected MetaObject" + name + "].getRenderer() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getRenderer(),
                actualMetaObject.getRenderer());

        Assert.assertEquals("expected MetaObject" + name + "].getSimpleEditor() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getSimpleEditor(),
                actualMetaObject.getSimpleEditor());

        Assert.assertEquals("expected MetaObject" + name + "].getStatus() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getStatus(),
                actualMetaObject.getStatus());

        Assert.assertEquals("expected MetaObject" + name + "].getStatusDebugString() matches actual MetaObject (" + pk + ")",
                expectedMetaObject.getStatusDebugString(),
                actualMetaObject.getStatusDebugString());

        final ObjectAttribute[] objectAttributeFromLegacyServer = expectedMetaObject.getAttribs();
        final ObjectAttribute[] objectAttributeFromRestServer = actualMetaObject.getAttribs();

        Assert.assertEquals("expected MetaObject" + name + "].getAttribs() size from rest server matches MetaObject from rest server (" + pk + ")",
                objectAttributeFromLegacyServer.length,
                objectAttributeFromRestServer.length);

        for (int i = 0; i < objectAttributeFromLegacyServer.length; i++) {
            this.compareObjectAttributes(
                    objectAttributeFromLegacyServer[i],
                    objectAttributeFromRestServer[i],
                    pk);
        }
    }

    protected void compareObjectAttributes(
            final ObjectAttribute expectedObjectAttribute,
            final ObjectAttribute actualObjectAttribute,
            final int pk) throws AssertionError {

        final String name = expectedObjectAttribute.getName();

        Assert.assertEquals("expected objectAttribute[" + name + "].getClassID()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getClassID(),
                actualObjectAttribute.getClassID());

        Assert.assertEquals("expected objectAttribute[" + name + "].getClassKey()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getClassKey(),
                actualObjectAttribute.getClassKey());

        Assert.assertEquals("expected objectAttribute[" + name + "].getDescription()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getDescription(),
                actualObjectAttribute.getDescription());

        Assert.assertEquals("expected objectAttribute[" + name + "].getID()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getID(),
                actualObjectAttribute.getID());

        Assert.assertEquals("expected objectAttribute[" + name + "].getKey()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getKey(),
                actualObjectAttribute.getKey());

        Assert.assertEquals("expected objectAttribute[" + name + "].getJavaType()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getJavaType(),
                actualObjectAttribute.getJavaType());

        Assert.assertEquals("expected objectAttribute[" + name + "].getPermissions().toString()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getPermissions().getPolicy().toString(),
                actualObjectAttribute.getPermissions().getPolicy().toString());

        Assert.assertEquals("expected objectAttribute[" + name + "].getTypeId()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.getTypeId(),
                actualObjectAttribute.getTypeId());

        Assert.assertEquals("expected objectAttribute[" + name + "].isArray()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isArray(),
                actualObjectAttribute.isArray());

        Assert.assertEquals("expected objectAttribute[" + name + "].isChanged()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isChanged(),
                actualObjectAttribute.isChanged());

        Assert.assertEquals("expected objectAttribute[" + name + "].isOptional()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isOptional(),
                actualObjectAttribute.isOptional());

        Assert.assertEquals("expected objectAttribute[" + name + "].isPrimaryKey()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isPrimaryKey(),
                actualObjectAttribute.isPrimaryKey());

        Assert.assertEquals("expected objectAttribute[" + name + "].isSubstitute()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isSubstitute(),
                actualObjectAttribute.isSubstitute());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVisible()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isVisible(),
                actualObjectAttribute.isVisible());

        Assert.assertEquals("expected objectAttribute[" + name + "].isStringCreateable()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isStringCreateable(),
                actualObjectAttribute.isStringCreateable());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVirtualOneToManyAttribute()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isVirtualOneToManyAttribute(),
                actualObjectAttribute.isVirtualOneToManyAttribute());

        Assert.assertEquals("expected objectAttribute[" + name + "].isVisible()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.isVisible(),
                actualObjectAttribute.isVisible());

        Assert.assertEquals("expected objectAttribute[" + name + "].referencesObject()  matches actual ObjectAttribute  (classId: " + pk + ")",
                expectedObjectAttribute.referencesObject(),
                actualObjectAttribute.referencesObject());

        final MemberAttributeInfo expectedMemberAttributeInfo = expectedObjectAttribute.getMai();
        final MemberAttributeInfo actualMemberAttributeInfo = actualObjectAttribute.getMai();

        if (expectedMemberAttributeInfo != null) {
            Assert.assertNotNull("expected objectAttribute[" + name + "] has MemberAttributeInfo (classId: " + pk + ")",
                    actualMemberAttributeInfo);

            this.compareMemberAttributeInfos(
                    expectedMemberAttributeInfo,
                    actualMemberAttributeInfo,
                    pk);
        } else {
            Assert.assertNull("expected objectAttribute[" + name + "] has no MemberAttributeInfo (classId: " + pk + ")",
                    actualMemberAttributeInfo);
        }

        final Object expectedObjectAttributeValue = expectedObjectAttribute.getValue();
        final Object actualObjectAttributeValue = actualObjectAttribute.getValue();

        if (expectedObjectAttributeValue != null) {
            Assert.assertNotNull("actual objectAttribute[" + name + "] value is not null (classId: " + pk + ")",
                    actualObjectAttributeValue);

            final Class expectedObjectAttributeValueClass = expectedObjectAttributeValue.getClass();
            final Class actualObjectAttributeValueClass = actualObjectAttributeValue.getClass();

            Assert.assertEquals("actual objectAttribute[" + name + "] value from rest server is a "
                    + expectedObjectAttributeValueClass.getSimpleName() + " (classId: " + pk + ")",
                    expectedObjectAttributeValueClass,
                    actualObjectAttributeValueClass);

            if (expectedObjectAttribute.referencesObject()) {
                Assert.assertTrue("expected objectAttribute[" + name + "] value is a MetaObject (classId: " + pk + ")",
                        MetaObject.class.isAssignableFrom(expectedObjectAttributeValueClass));
                Assert.assertTrue("actual objectAttribute[" + name + "] value is a MetaObject (classId: " + pk + ")",
                        MetaObject.class.isAssignableFrom(actualObjectAttributeValueClass));

                this.compareMetaObjects(
                        (MetaObject) expectedObjectAttributeValue,
                        (MetaObject) actualObjectAttributeValue);

            } else if (expectedObjectAttributeValueClass.isPrimitive()) {
                Assert.assertEquals("actual objectAttribute[" + name + "] value matches (classId: " + pk + ")",
                        expectedObjectAttributeValue,
                        actualObjectAttributeValue);

                // disable value comparision for non-primitives due to unkown behaviour of object.equals
            }
        } else {
            Assert.assertNull("expected objectAttribute[" + name + "] value from rest server is null (classId: " + pk + ")",
                    actualObjectAttributeValue);
        }
    }

    protected void compareMemberAttributeInfos(
            final MemberAttributeInfo expectedMemberAttributeInfo,
            final MemberAttributeInfo actualMemberAttributeInfo,
            final int pk) {

        final String name = expectedMemberAttributeInfo.getName();

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getArrayKeyFieldName matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getArrayKeyFieldName(),
                actualMemberAttributeInfo.getArrayKeyFieldName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getClassId() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getClassId(),
                actualMemberAttributeInfo.getClassId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getComplexEditor() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getComplexEditor(),
                actualMemberAttributeInfo.getComplexEditor());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getDefaultValue() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getDefaultValue(),
                actualMemberAttributeInfo.getDefaultValue());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getEditor() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getEditor(),
                actualMemberAttributeInfo.getEditor());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getFieldName() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getFieldName(),
                actualMemberAttributeInfo.getFieldName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getForeignKeyClassId() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getForeignKeyClassId(),
                actualMemberAttributeInfo.getForeignKeyClassId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getFromString() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getFromString(),
                actualMemberAttributeInfo.getFromString());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getId() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getId(),
                actualMemberAttributeInfo.getId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getJavaclassname() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getJavaclassname(),
                actualMemberAttributeInfo.getJavaclassname());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getKey() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getKey(),
                actualMemberAttributeInfo.getKey());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getName() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getName(),
                actualMemberAttributeInfo.getName());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getPosition() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getPosition(),
                actualMemberAttributeInfo.getPosition());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getRenderer() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getRenderer(),
                actualMemberAttributeInfo.getRenderer());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getToString() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getToString(),
                actualMemberAttributeInfo.getToString());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].getTypeId() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.getTypeId(),
                actualMemberAttributeInfo.getTypeId());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isArray() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.isArray(),
                actualMemberAttributeInfo.isArray());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isExtensionAttribute() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.isExtensionAttribute(),
                actualMemberAttributeInfo.isExtensionAttribute());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isForeignKey() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.isForeignKey(),
                actualMemberAttributeInfo.isForeignKey());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isIndexed() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.isIndexed(),
                actualMemberAttributeInfo.isIndexed());

        Assert.assertEquals("actual memberAttributeInfo[" + name + "].isOptional() matches actual MemberAttributeInfo (" + pk + ")",
                expectedMemberAttributeInfo.isOptional(),
                actualMemberAttributeInfo.isOptional());
    }
}
