package de.cismet.cids.integrationtests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.rules.ExternalResource;

/**
 * TestEnviroment Specification for providing access to system properties and
 * configurations that control the integration test behaviour.
 * 
 * Can be used as @ClassRule to skip tests if integration tests are disabled.
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public class TestEnvironment extends ExternalResource {

    public static final String INTEGRATION_TESTS_ENABLED = "de.cismet.cids.integrationtests.enabled";
    protected static volatile Properties properties = null;
    
    public static boolean isIntegrationTestsEnabled() {
        //System.out.println(System.getProperty(INTEGRATION_TESTS_ENABLED));
        return "true".equalsIgnoreCase(System.getProperty(INTEGRATION_TESTS_ENABLED));
    }

    public static boolean isIntegrationTestsDisabled() {
        //System.out.println(System.getProperty(INTEGRATION_TESTS_ENABLED));
        return !"true".equalsIgnoreCase(System.getProperty(INTEGRATION_TESTS_ENABLED));
    }
    

    @Override
    protected void before() throws Throwable {
        Logger.getLogger(TestEnvironment.class).info("activating cids Integration Tests: " + !TestEnvironment.isIntegrationTestsDisabled());
        Assume.assumeTrue(!TestEnvironment.isIntegrationTestsDisabled());
    }
    
    public static String getCallserverUrl(final String containerIpAddress, final int mappedPort) {
        final String callserverUrl = "http://" + containerIpAddress
                    + ":" + mappedPort + "/callserver/binary";
        
        return callserverUrl;
    }
    
    public static Properties getProperties() {
        Properties localProperties = TestEnvironment.properties;
        if (localProperties == null) {
            synchronized (TestEnvironment.class) {
                localProperties = TestEnvironment.properties;
                if (localProperties == null) {
                    
                    localProperties = new Properties();
                    try {
                        final InputStream propertyFile = TestEnvironment.class.getResourceAsStream("cidsRef.properties");
                        final InputStreamReader isr = new InputStreamReader(propertyFile);
                        final BufferedReader br = new BufferedReader(isr);
                        localProperties.load(br);
                    } catch (IOException ioex) {
                        Logger.getLogger(TestEnvironment.class).error("could not load properties file: " + ioex.getMessage(), ioex);
                    }
                }
            }
        }
        
        return localProperties;
    }
}
