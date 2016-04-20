package de.cismet.cids.integrationtests;

import Sirius.server.newuser.User;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

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
public class IntegrationBaseTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(IntegrationBaseTest.class);

    /**
     * The static cids Reference Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initcidsRefContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static GenericContainer postgresContainer = null;

    /**
     * This ClassRule is executed only once before any test run (@Test method)
     * starts. It checks whether the cids testing enviroment is enabled or not.
     * If the testing enviroment is enabled, it creates a new managed
     * GenericContainer.
     *
     * @return GenericContainer or dummy ClassRule
     */
    @ClassRule
    public static TestRule initcidsRefContainer() throws Throwable {
        LOGGER.info("activating cids Integration Tests: " + !TestEnvironment.isIntegrationTestsDisabled());

        // check if integration tests are enabled in current maven profile
        if (TestEnvironment.isIntegrationTestsDisabled()) {
            // return Dummy ClassRule that skips the test
            return new ExternalResource() {
                @Override
                protected void before() throws Throwable {
                    // Important: this will skip the test *before* any docker image is started!
                    Assume.assumeTrue(false);
                }
            };
        } else {

            try {
                // create new PostgreSQLContainer
                postgresContainer = new GenericContainer("cismet/cids-integration-base:latest")
                        .withExposedPorts(5434)
                        .withClasspathResourceMapping("cids-integration-base/import/cids_reference.sql",
                                "/import/cids-integration-base/cids_reference.sql", BindMode.READ_ONLY);

                // Important: return the container instance. Otherwise start/stop 
                // of the container is not called!
                return postgresContainer;
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
                throw t;
            }
        }
    }

    protected static User user = null;
    protected static Properties properties = null;

    @BeforeClass
    public static void beforeClass() throws Exception {

        properties = TestEnvironment.getProperties();
        Assert.assertNotNull("TestEnvironment created", properties);

        // check container creation succeeded 
        Assert.assertNotNull("postgresContainer sucessfully created", postgresContainer);

        // check if docker image started
        Assert.assertTrue("postgresContainer is running", postgresContainer.isRunning());

//        try {
//            final String callserverUrl = TestEnvironment.getCallserverUrl(postgresContainer.getServiceHost("docker_cidsref_1", 9986),
//                    postgresContainer.getServicePort("docker_cidsref_1", 9986));
//
//            
//            
//            
//            
//            LOGGER.info("connection to cids reference docker legacy server: " + callserverUrl);
//
//            connector = new RESTfulSerialInterfaceConnector(callserverUrl);
//
//            user = connector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
//                    properties.getProperty("usergroup", "Administratoren"),
//                    properties.getProperty("userDomain", "CIDS_REF"),
//                    properties.getProperty("username", "admin"),
//                    properties.getProperty("password", "cismet"));
//
//            LOGGER.info("sucessfully authenticated cids user: " + user.toString());
//
//        } catch (Exception e) {
//
//            LOGGER.error(e.getMessage(), e);
//            throw e;
//        }
    }

    @Test
    public void testConnection() throws Exception {
        LOGGER.info(postgresContainer.getContainerIpAddress());
        LOGGER.info(postgresContainer.getMappedPort(5432));
    }

}
