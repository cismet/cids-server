package de.cismet.cids.integrationtests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    public static boolean pingHost(String host, int port, int timeout) {
        try {
            final Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.println("GET /callserver/binary HTTP/1.1");
            pw.flush();
            Logger.getLogger(TestEnvironment.class).debug(socket.getLocalSocketAddress());
            //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final InputStream is = socket.getInputStream();
            while (socket.getInputStream().read() != -1) {
                //Logger.getLogger(TestEnvironment.class).debug(t);
            }
            is.close();
            return true;
        } catch (Throwable t) {
            Logger.getLogger(TestEnvironment.class).error("ping " + host + ":" 
                    + port + "fails after " + timeout + "ms: " + t.getMessage(), t);
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
