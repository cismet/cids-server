package de.cismet.cids.integrationtests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.DockerComposeContainer;

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

    public static final String INTEGRATIONBASE_CONTAINER = "cids_integrationtests_integrationbase_1";
    public static final String SERVER_CONTAINER = "cids_integrationtests_server_1";
    public static final String REST_SERVER_CONTAINER = "cids_integrationtests_server_rest_1";

    public final static ExternalResource SKIP_INTEGRATION_TESTS = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            // Important: this will skip the test *before* any docker image is started!
            Assume.assumeTrue(false);
        }
    };

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
                        final InputStream propertyFile = TestEnvironment.class.getResourceAsStream("cidsIntegrationtests.properties");
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
    
    public static boolean pingHostWithGet(final String host, final int port, final String path, int retries) {
        final String connectionUrl = "http://" + host + ":" + port + path;
        Logger.getLogger(TestEnvironment.class).debug("ping service with GET at url " + connectionUrl);
        final HttpMethod method = new GetMethod(connectionUrl);
        
        return pingHost(method, host, port, path, retries);
    }
    
    public static boolean pingHostWithPost(final String host, final int port, final String path, int retries) {
        final String connectionUrl = "http://" + host + ":" + port + path;
        Logger.getLogger(TestEnvironment.class).debug("ping service with POST at url " + connectionUrl);
        final HttpMethod method = new PostMethod(connectionUrl);
        
        return pingHost(method, host, port, path, retries);
    }

    protected static boolean pingHost(final HttpMethod method, final String host, final int port, final String path, int retries) {
        try {
            
            final HttpClient client = new HttpClient();
            final HttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(retries, true);
            final HttpMethodParams params = new HttpMethodParams();
            params.setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
            method.setParams(params);

            int statusCode = client.executeMethod(method);
            while (statusCode != 200 && retries > 0) {
                Logger.getLogger(TestEnvironment.class).warn("ping " + host + ":"
                        + port + " return status code: " + statusCode + ", retrying " + retries + "x");
                statusCode = client.executeMethod(method);
                retries--;
            }

            method.releaseConnection();
            return statusCode == 200;

        } catch (Throwable t) {
            Logger.getLogger(TestEnvironment.class).error("ping " + host + ":"
                    + port + " fails after " + retries + " retries: " + t.getMessage(), t);
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static DockerComposeContainer createDefaultDockerEnvironment() throws Exception {
        final String composeFile = System.getProperty("user.home")
                + File.separator
                + "docker-volumes" + File.separator
                + "cids-integrationtests" + File.separator
                + "docker-compose.yml";

        final File file = new File(composeFile);
        if (!file.exists()) {
            final String message = "Docker Compose File for cids Integration Tests not found at default location: "
                    + composeFile;
            throw new FileNotFoundException(message);
        }

        DockerComposeContainer dockerEnvironment = new DockerComposeContainer(file)
                .withExposedService(INTEGRATIONBASE_CONTAINER,
                        Integer.parseInt(getProperties().getProperty("integrationbase.port", "5434")))
                .withExposedService(SERVER_CONTAINER,
                        Integer.parseInt(getProperties().getProperty("broker.port", "9986")))
                .withExposedService(REST_SERVER_CONTAINER,
                        Integer.parseInt(getProperties().getProperty("restservice.port", "8890")));

        return dockerEnvironment;
    }
}
