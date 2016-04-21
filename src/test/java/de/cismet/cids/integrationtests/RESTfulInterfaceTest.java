package de.cismet.cids.integrationtests;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static de.cismet.cids.dynamics.AbstractCidsBeanDeserialisationTest.ENTITIES_JSON_PACKAGE;
import de.cismet.cids.dynamics.CidsBean;
import static de.cismet.cids.integrationtests.TestEnvironment.REST_SERVER_CONTAINER;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cidsx.client.connector.RESTfulInterfaceConnector;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * CidsBean vs MetaObject Tests
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@RunWith(DataProviderRunner.class)
public class RESTfulInterfaceTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(RESTfulInterfaceTest.class);

    /**
     * The static cids Reference Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initcidsRefContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static DockerComposeContainer dockerEnvironment = null;

    /**
     * This ClassRule is executed only once before any test run (@Test method)
     * starts. It checks whether the cids testing enviroment is enabled or not.
     * If the testing enviroment is enabled, it creates a new managed
     * GenericContainer.
     *
     * @return GenericContainer or dummy ClassRule
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
                properties = TestEnvironment.getProperties();
                return dockerEnvironment;
            } catch (Exception ex) {
                LOGGER.fatal("could not initialize docker interation test environment, integration tests disabled!", ex);
                return TestEnvironment.SKIP_INTEGRATION_TESTS;
            }
        }
    }

    protected static RESTfulInterfaceConnector restConnector = null;
    protected static RESTfulSerialInterfaceConnector legacyConnector = null;
    protected static User user = null;
    protected static Properties properties = null;
    protected static boolean connectionFailed = false;
    protected final static ArrayList<String> CIDS_BEANS_JSON = new ArrayList<String>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.debug("beforeClass(): cids Integration Tests activated, loading properties");
        Assert.assertNotNull("TestEnvironment created", properties);

        // check container creation succeeded 
        Assert.assertNotNull("cidsRefContainer sucessfully created", dockerEnvironment);

        Assert.assertFalse("connecting to cids integrationtest docker containers could not be established",
                connectionFailed);

        try {

            final String brokerUrl = TestEnvironment.getCallserverUrl(
                    dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))));

            // check connection to legacy server (broker f.k.a. callserver)
            if (!TestEnvironment.pingHost(
                    dockerEnvironment.getServiceHost(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    "/callserver/binary/getUserGroupNames",
                    12)) {
                connectionFailed = true;
                throw new Exception(brokerUrl + " did not answer after 12 retries");
            }

            LOGGER.info("connecting to cids reference docker legacy server: " + brokerUrl);
            legacyConnector = new RESTfulSerialInterfaceConnector(brokerUrl);

            final String restServerUrl = "http://"
                    + dockerEnvironment.getServiceHost(REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890")))
                    + ":"
                    + dockerEnvironment.getServicePort(REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890")))
                    + "/";

            if (!TestEnvironment.pingHost(
                    dockerEnvironment.getServiceHost(
                            REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890"))),
                    dockerEnvironment.getServicePort(
                            REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890"))),
                    "/service/ping",
                    6)) {
                connectionFailed = true;
                throw new Exception(brokerUrl + " did not answer after 6 retries");
            }

            LOGGER.info("connecting to cids reference docker rest server: " + restServerUrl);
            restConnector = new RESTfulInterfaceConnector(restServerUrl);

            // load local reference cids beans
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            URL resources;
            resources = classLoader.getResource(ENTITIES_JSON_PACKAGE);

            final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
            while (scanner.hasNext()) {
                final String jsonFile = ENTITIES_JSON_PACKAGE + scanner.next();
                LOGGER.info("loading cids entity from json file " + jsonFile);
                try {

                    final String entity = IOUtils.toString(classLoader.getResourceAsStream(jsonFile), "UTF-8");
                    CIDS_BEANS_JSON.add(entity);

                } catch (Exception ex) {
                    LOGGER.error("could not load cids entities from url " + jsonFile, ex);
                    throw ex;
                }
            }

            LOGGER.info(CIDS_BEANS_JSON.size() + " CIDS_BEANS_JSON entities loaded");

            // authenticate user
            user = legacyConnector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                    properties.getProperty("usergroup", "Administratoren"),
                    properties.getProperty("userDomain", "CIDS_REF"),
                    properties.getProperty("username", "admin"),
                    properties.getProperty("password", "cismet"));

            LOGGER.info("sucessfully authenticated cids user: " + user.toString());

        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @DataProvider
    public final static String[] getCidsBeansJson() throws Exception {
        return CIDS_BEANS_JSON.toArray(new String[CIDS_BEANS_JSON.size()]);
    }

    @Test
    @UseDataProvider("getCidsBeans")
    public void getAndCompareMetaObjects(final String cidsBeanJson) throws Exception {
        LOGGER.debug("testing getAndCompareMetaObjects");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        try {
            final CidsBean cidsBeanFromJson = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObjectFromJson = cidsBeanFromJson.getMetaObject();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " from legacy server");
            final MetaObject metaObjectFromLegacyServer = legacyConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            final CidsBean cidsBeanFromLegacyServer = metaObjectFromLegacyServer.getBean();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " from rest server");
            final MetaObject metaObjectFromRestServer = restConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            final CidsBean cidsBeanFromRestServer = metaObjectFromRestServer.getBean();

            Assume.assumeTrue("cidsBeanFromJson.getCidsBeanInfo().getClassKey().equalsIgnoreCase(\"SPH_SPIELHALLE\")",
                    cidsBeanFromJson.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));
            Assume.assumeTrue("cidsBeanFromLegacyServer.getCidsBeanInfo().getClassKey().equalsIgnoreCase(\"SPH_SPIELHALLE\")",
                    cidsBeanFromLegacyServer.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));
            Assume.assumeTrue("cidsBeanFromRestServer.getCidsBeanInfo().getClassKey().equalsIgnoreCase(\"SPH_SPIELHALLE\")",
                    cidsBeanFromRestServer.getCidsBeanInfo().getClassKey().equalsIgnoreCase("SPH_SPIELHALLE"));

        } catch (AssertionError ae) {
            LOGGER.error("getAndCompareCidsBeans test failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("getAndCompareMetaObjects test passed");
    }

    @Test
    public void changePasswordSuccess() throws Exception {
        LOGGER.debug("testing changePasswordSuccess");
        final String newPassword = "DADhejPYEDtym:8hej54umVEB0hag25y";
        final String oldPassword = properties.getProperty("password", "cismet");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertTrue("password of user changed",
                legacyConnector.changePassword(user, oldPassword, newPassword));

        Assert.assertTrue("password of user changed",
                legacyConnector.changePassword(user, newPassword, oldPassword));

        LOGGER.info("changePasswordSuccess passed!");
    }

    @Test
    public void changePasswordError() throws Exception {
        LOGGER.debug("testing changePasswordError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertFalse(legacyConnector.changePassword(user, "wrong_password", "wrong_password"));
        LOGGER.info("changePasswordError passed!");
    }

    @Test(expected = UserException.class)
    public void getUserPasswordError() throws Exception {

        LOGGER.debug("testing getUserErrorPassword");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        try {

            legacyConnector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                    properties.getProperty("usergroup", "Administratoren"),
                    properties.getProperty("userDomain", "CIDS_REF"),
                    properties.getProperty("username", "admin"),
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
    public void getUserDomainError() throws Exception {

        LOGGER.debug("testing getUserErrorDomain");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        try {
            legacyConnector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                    properties.getProperty("usergroup", "Administratoren"),
                    "WRONG_DOMAIN",
                    properties.getProperty("username", "admin"),
                    properties.getProperty("password", "cismet"));
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
    public void getDomains() throws Exception {
        LOGGER.debug("testing getDomains");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        final String domains[] = legacyConnector.getDomains();

        Assert.assertTrue("one domain available", domains.length == 1);

        Assert.assertEquals("domain matches from properties",
                properties.getProperty("domain", "CIDS_REF"), domains[0]);

        Assert.assertEquals("domain matches from user",
                this.user.getDomain(), domains[0]);

        LOGGER.info("getDomains test passed!");
    }

    @Test
    public void getUserGroupNames() throws Exception {
        LOGGER.debug("testing getUserGroupNames");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        Vector userGroupNames = legacyConnector.getUserGroupNames();
        Assert.assertTrue("user groups available on server", userGroupNames.size() > 0);

        userGroupNames = legacyConnector.getUserGroupNames(
                properties.getProperty("username", "admin"),
                properties.getProperty("userDomain", "CIDS_REF"));

        Assert.assertTrue("user groups for user available on server", userGroupNames.size() > 0);

        Assert.assertEquals("usergroup matches from properties",
                properties.getProperty("usergroup", "Administratoren"),
                ((String[]) userGroupNames.get(0))[0]);

        Assert.assertEquals("usergroup matches from user",
                this.user.getUserGroup().getName(),
                ((String[]) userGroupNames.get(0))[0]);

        Assert.assertEquals("usergroup domain matches from properties",
                properties.getProperty("usergroupDomain", "CIDS_REF"),
                ((String[]) userGroupNames.get(0))[1]);

        Assert.assertEquals("usergroup domain matches from user",
                this.user.getUserGroup().getDomain(),
                ((String[]) userGroupNames.get(0))[1]);

        LOGGER.info("getUserGroupNames test passed!");

    }

    @Test
    public void getUserGroupNamesError() throws Exception {
        LOGGER.debug("testing getUserGroupNamesError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", legacyConnector);
        Assert.assertNotNull("user authenticated", user);

        Vector userGroupNames = legacyConnector.getUserGroupNames(
                "does-not-exist",
                "does-not-exist");

        Assert.assertTrue("no groups found for wrong user and domain",
                userGroupNames.isEmpty());

    }
}
