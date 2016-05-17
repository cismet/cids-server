package de.cismet.cids.integrationtests;

import Sirius.server.middleware.impls.domainserver.OfflineMetaClassCacheService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static de.cismet.cids.integrationtests.TestEnvironment.INTEGRATIONBASE_CONTAINER;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
public class LegacyRESTfulInterfaceTest {

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

    @Test
    @UseDataProvider("getMetaClassTableNames")
    public void test00integrationBase00countDbEntities(final String tableName) throws Exception {
        LOGGER.debug("testing countDbEntities(" + tableName + ")");

        try {
            final int count = RESTfulInterfaceTest.countDbEntities(jdbcConnection, tableName);
            if (!tableName.equalsIgnoreCase("URL_BASE")) {
                Assert.assertTrue(tableName + " entities available in integration base", count > 0);
            }

            Assert.assertNull(tableName + " entities counted only once",
                    dbEntitiesCount.put(tableName, count));

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

    // USER SERVICE TESTS ------------------------------------------------------
    @Test
    public void test01userService00changePasswordSuccess() throws Exception {
        LOGGER.debug("testing changePasswordSuccess");
        final String newPassword = "DADhejPYEDtym:8hej54umVEB0hag25y";
        final String oldPassword = PROPERTIES.getProperty("password", "cismet");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertTrue("password of user changed",
                connector.changePassword(user, oldPassword, newPassword));

        Assert.assertTrue("password of user changed",
                connector.changePassword(user, newPassword, oldPassword));

        LOGGER.info("changePasswordSuccess passed!");
    }

    @Test
    public void test01userService01changePasswordError() throws Exception {
        LOGGER.debug("testing changePasswordError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertFalse(connector.changePassword(user, "wrong_password", "wrong_password"));
        LOGGER.info("changePasswordError passed!");
    }

    @Test(expected = UserException.class)
    public void test01userService02getUserPasswordError() throws Exception {

        LOGGER.debug("testing getUserErrorPassword");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {

            connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("username", "admin"),
                    "wrong_password");
        } catch (UserException ex) {
            LOGGER.debug(ex.getClass(), ex);
            LOGGER.info("getUserErrorPassword passed!");
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(ex.getClass(), ex);
            throw ex;
        }
    }

    @Test(expected = UserException.class)
    public void test01userService03getUserDomainError() throws Exception {

        LOGGER.debug("testing getUserErrorDomain");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {
            connector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    "WRONG_DOMAIN",
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("password", "cismet"));
        } catch (UserException ex) {
            LOGGER.debug(ex.getClass(), ex);
            LOGGER.info("getUserErrorDomain passed!");
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("getUserErrorDomain test failed", ex);
            throw ex;
        }
    }

    @Test
    public void test01userService04getDomains() throws Exception {
        LOGGER.debug("testing getDomains");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        final String domains[] = connector.getDomains();

        Assert.assertTrue("one domain available", domains.length == 1);

        Assert.assertEquals("domain matches from properties",
                PROPERTIES.getProperty("domain", "CIDS_REF"), domains[0]);

        Assert.assertEquals("domain matches from user",
                this.user.getDomain(), domains[0]);

        LOGGER.info("getDomains test passed!");
    }

    @Test
    public void test01userService04getUserGroupNames() throws Exception {
        LOGGER.debug("testing getUserGroupNames");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

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
                this.user.getUserGroup().getName(),
                ((String[]) userGroupNames.get(0))[0]);

        Assert.assertEquals("usergroup domain matches from properties",
                PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                ((String[]) userGroupNames.get(0))[1]);

        Assert.assertEquals("usergroup domain matches from user",
                this.user.getUserGroup().getDomain(),
                ((String[]) userGroupNames.get(0))[1]);

        LOGGER.info("getUserGroupNames test passed!");

    }

    @Test
    public void test01userService06getUserGroupNamesError() throws Exception {
        LOGGER.debug("testing getUserGroupNamesError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Vector userGroupNames = connector.getUserGroupNames(
                "does-not-exist",
                "does-not-exist");

        Assert.assertTrue("no groups found for wrong user and domain",
                userGroupNames.isEmpty());

    }
}
