package de.cismet.cids.integrationtests;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

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
public class PostgreSQLTest {

    protected final static Logger LOGGER = Logger.getLogger(PostgreSQLTest.class);

//    static {
//        final Properties log4jProperties = new Properties();
//        log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
//        log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
//        log4jProperties.put("log4j.appender.Remote.port", "4445");
//        log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
//        log4jProperties.put("log4j.rootLogger", "ALL,Remote");
//        org.apache.log4j.PropertyConfigurator.configure(log4jProperties);
//    }
    @Rule
    public PostgreSQLContainer postgres = new PostgreSQLContainer();

    public PostgreSQLTest() {
        LOGGER.debug("initializing PostgreSQLContainer");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        final Properties log4jProperties = new Properties();
        log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
        log4jProperties.put("log4j.appender.Remote.port", "4445");
        log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
        log4jProperties.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(log4jProperties);

        LOGGER.debug("activating PostgreSQLTest: " + TestEnvironment.isIntegrationTestsEnabled());
        Assume.assumeTrue(!TestEnvironment.isIntegrationTestsDisabled());

        LOGGER.debug("PostgreSQLTest activated");

    }

    @Test
    public void testConnection() throws Exception {

        LOGGER.info("testConnection");
        try {
            Class.forName(postgres.getDriverClassName());

        } catch (ClassNotFoundException e) {
            LOGGER.error("JDBC Driver '" + postgres.getDriverClassName()
                    + "' Driver not available", e);
            throw e;
        }

        try {
            Connection connection = postgres.createConnection("");

            final Statement statement = connection.createStatement();
            LOGGER.debug("Testing PostgreSQL connection with statement: " + postgres.getTestQueryString());
            final boolean result = statement.execute(postgres.getTestQueryString());

            Assert.assertTrue(result);
            LOGGER.info("PostgreSQL connection test successfully completed");

        } catch (SQLException e) {

            LOGGER.error(e.getMessage(), e);
            throw e;

        }
    }
}
