package de.cismet.cids.integrationtests;

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
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
        LOGGER.info("initCidsRefContainer(): activating cids Integration Tests: "
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
        LOGGER.debug("UserServiceTest(): initializing UserServiceTest instance");

        try {
            final String brokerUrl = TestEnvironment.getCallserverUrl(
                    dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))));

//            Unreliables.retryUntilTrue(30, TimeUnit.SECONDS, () -> {
//                //noinspection CodeBlock2Expr
//                return DOCKER_CLIENT_RATE_LIMITER.getWhenReady(() -> {
//                    InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
//                    return inspectionResponse.getState().isRunning();
//                });
//            });
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

        // check if docker image started
        //Assert.assertTrue("cidsRefContainer is running", dockerEnvironment.isRunning());
    }

    @Test
    public void changePasswordSuccess() throws Exception {
        LOGGER.debug("testing changePasswordSuccess");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        Assert.assertTrue("password of user changed",
                connector.changePassword(user,
                        properties.getProperty("password", "cismet"),
                        "DADhejPYEDtym:8hej54umVEB0hag25y"));
    }

    //@Test(expected = UserException.class)
    public void changePasswordError() throws Exception {
        LOGGER.debug("testing changePasswordError");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);
        Assert.assertNotNull("user authenticated", user);

        try {
            connector.changePassword(user, "wrong_password",
                    "DADhejPYEDtym:8hej54umVEB0hag25y");
        } catch (Exception ex) {
            LOGGER.debug(ex.getClass(), ex);
            throw ex;
        }

    }

    @Test(expected = UserException.class)
    @Ignore
    public void getUserErrorPassword() throws Exception {

        LOGGER.debug("testing getUserErrorPassword");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);

        connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                properties.getProperty("usergroup", "Administratoren"),
                properties.getProperty("userDomain", "CIDS_REF"),
                properties.getProperty("username", "admin"),
                "wrong_password");
    }

    @Test(expected = UserException.class)
    @Ignore
    public void getUserErrorDomain() throws Exception {

        LOGGER.debug("testing getUserErrorDomain");

        Assert.assertNotNull("cidsRefContainer connection successfully established", connector);

        connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                properties.getProperty("usergroup", "Administratoren"),
                "WRONG_DOMAIN",
                properties.getProperty("username", "admin"),
                properties.getProperty("password", "cismet"));
    }
}
