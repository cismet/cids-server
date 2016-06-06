package de.cismet.cids.integrationtests;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Simple PostgreSQLTest to check whether docker is properly installed and the
 * docker postgresContainer image can be started and queried.
 *
 * Attention: Testcontainers is currently lacking windows support, see
 * https://github.com/testcontainers/testcontainers-java/issues/85 On windows
 * use the fork https://github.com/v-schulz/testcontainers-java
 * (1.0.1-SNAPSHOT)!
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@Ignore
public class PostgreSQLTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(PostgreSQLTest.class);

    /**
     * The static Postgres Docker Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initPostgreSQLContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static PostgreSQLContainer postgresContainer = null;

    /**
     * This ClassRule is executed only once before any test run (@Test method)
     * starts. It checks whether the cids testing enviroment is enabled or not.
     * If the testing enviroment is enabled, it creates a new managed
     * PostgreSQLContainer.
     *
     * @return PostgreSQLContainer or dummy ClassRule
     */
    @ClassRule
    public static TestRule initPostgreSQLContainer() {
        LOGGER.info("activating cids Integration Tests: " + TestEnvironment.isIntegrationTestsEnabled());

        // check if integration tests are enabled in current maven profile
        if (!TestEnvironment.isIntegrationTestsEnabled()) {
            // return Dummy ClassRule that skips the test
            return TestEnvironment.SKIP_INTEGRATION_TESTS;
        } else {

            // create new PostgreSQLContainer
            postgresContainer = new PostgreSQLContainer();

            // Important: return the container instance. Otherwise start/stop 
            // of the container is not called!
            return postgresContainer;
        }
    }

    public PostgreSQLTest() {
        // constructor is *not called* if integration tests are *disabled*  
        // in current maven profile, see initPostgreSQLContainer()
        LOGGER.debug("initializing PostgreSQLContainer");
    }

    @Test
    public void testConnection() throws Exception {

        LOGGER.info("testConnection");

        // check conteiner creation succeeded 
        Assert.assertNotNull("postgresContainer sucessfully created", postgresContainer);

        // check if docker image started
        Assert.assertTrue("postgresContainer is running", postgresContainer.isRunning());

        LOGGER.debug("postgresContainer '" + postgresContainer.getContainerName()
                + "' (" + postgresContainer.getContainerId() + ") sucessfully created");

        Assert.assertNotNull("postgresContainer knows JDBC Driver Class Name",
                postgresContainer.getDriverClassName());

        try {
            Class.forName(postgresContainer.getDriverClassName());
        } catch (ClassNotFoundException e) {
            LOGGER.error("JDBC Driver '" + postgresContainer.getDriverClassName()
                    + "' Driver not available", e);
            throw e;
        }

        LOGGER.debug("connecting to postgres DB: " + postgresContainer.getJdbcUrl());

        try {
            Connection connection = postgresContainer.createConnection("");
            //Assert.assertTrue("connection successfully established", connection.isValid(15));

            final Statement statement = connection.createStatement();
            LOGGER.debug("Testing PostgreSQL connection with statement: " + postgresContainer.getTestQueryString());
            final boolean result = statement.execute(postgresContainer.getTestQueryString());

            Assert.assertTrue(result);
            LOGGER.info("PostgreSQL connection test successfully completed");

        } catch (AssertionError ae) {
            LOGGER.error(ae.getMessage(), ae);
            throw ae;
        } catch (SQLException e) {

            LOGGER.error(e.getMessage(), e);
            throw e;

        }
    }
}
