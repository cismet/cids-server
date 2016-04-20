package de.cismet.cids.integrationtests;

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * Simple PostgreSQLTest to check whether docker is properly installed and the
 * docker postgres image can be started and queried.
 *
 * Attention: Testcontainers is currently lacking windows support, see
 * https://github.com/testcontainers/testcontainers-java/issues/85 On windows
 * use the fork https://github.com/v-schulz/testcontainers-java
 * (1.0.1-SNAPSHOT)!
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public class UserServiceTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(UserServiceTest.class);

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

    protected RESTfulSerialInterfaceConnector connector = null;
    protected User user = null;
    protected static Properties properties = null;

    public UserServiceTest() throws Exception {
        LOGGER.debug("UserServiceTest(): initializing new UserServiceTest instance");

        try {
            final String brokerUrl = TestEnvironment.getCallserverUrl(
                    dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))));

            if (!TestEnvironment.pingHost(
                    dockerEnvironment.getServiceHost(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    "/callserver/binary/getUserGroupNames",
                    12)) {
                throw new Exception(brokerUrl + "did not answer after 12 retries");
            }

            LOGGER.info("connecting to cids reference docker legacy server: " + brokerUrl);
            connector = new RESTfulSerialInterfaceConnector(brokerUrl);

            user = connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.debug("beforeClass(): cids Integration Tests activated, loading properties");
        Assert.assertNotNull("TestEnvironment created", properties);

        // check container creation succeeded 
        Assert.assertNotNull("cidsRefContainer sucessfully created", dockerEnvironment);
    }

    @Test
    public void changePasswordSuccess() throws Exception {
        LOGGER.debug("testing changePasswordSuccess");
        final String newPassword = "DADhejPYEDtym:8hej54umVEB0hag25y";
        final String oldPassword = properties.getProperty("password", "cismet");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertTrue("password of user changed",
                connector.changePassword(user, oldPassword, newPassword));

        Assert.assertTrue("password of user changed",
                connector.changePassword(user, newPassword, oldPassword));

        LOGGER.info("changePasswordSuccess passed!");
    }

    @Test
    public void changePasswordError() throws Exception {
        LOGGER.debug("testing changePasswordError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertFalse(connector.changePassword(user, "wrong_password", "wrong_password"));
        LOGGER.info("changePasswordError passed!");
    }

    @Test(expected = UserException.class)
    public void getUserPasswordError() throws Exception {

        LOGGER.debug("testing getUserErrorPassword");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {

            connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
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

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {
            connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
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

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        final String domains[] = connector.getDomains();

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

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Vector userGroupNames = connector.getUserGroupNames();
        Assert.assertTrue("user groups available on server", userGroupNames.size() > 0);

        userGroupNames = connector.getUserGroupNames(
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

    @Test(expected = RemoteException.class)
    public void getUserGroupNamesError() throws Exception {
        LOGGER.debug("testing getUserGroupNamesError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {
            final Vector userGroupNames = connector.getUserGroupNames(
                    "does-not-exist",
                    "does-not-exist");
        } catch (RemoteException ex) {
            LOGGER.debug(ex.getClass(), ex);
            LOGGER.info("getUserGroupNamesError test passed!");
            throw ex;
        }
    }
}
