/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketTimeoutException;

import static org.junit.Assert.*;

/**
 * Tests, that the connections of the SafeCloseHttpConnectionManager can be closed without delay, if the peer does not
 * respond anymore, e.g. because a firewall has silently dropped the connection.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SafeCloseHttpConnectionManagerTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final int READ_TIMEOUT = 2000;
    private static final int MAX_CLOSE_DURATION = 1000;
    private static final int TEST_TIMEOUT = 20000;

    //~ Instance fields --------------------------------------------------------

    private SilentTlsServer server;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
        // the JDK only waits for the close_notify response of the peer, if TLS 1.3 is used
        Assume.assumeTrue(SilentTlsServer.isTls13Supported());
        server = new SilentTlsServer();
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Verifies the precondition of the other tests: with the default connection manager, closing a connection to a
     * silent peer blocks until the read timeout expires. If this test fails, the JDK does not wait for the close_notify
     * response anymore and the SafeCloseHttpConnectionManager might be obsolete.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testDefaultManagerBlocksOnClose() {
        final ApacheHttpClient client = createClient(false);

        assertEquals("ok", client.resource(server.getBaseUrl() + "ok").get(String.class));
        assertEquals(1, getConnectionManager(client).getConnectionsInPool());

        final long duration = closeIdleConnections(client);

        assertTrue("closing took only " + duration + " ms", duration >= (READ_TIMEOUT - 200));
    }

    /**
     * DOCUMENT ME!
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testCloseIdleConnectionDoesNotBlock() {
        final ApacheHttpClient client = createClient(true);

        assertEquals("ok", client.resource(server.getBaseUrl() + "ok").get(String.class));
        assertEquals(1, getConnectionManager(client).getConnectionsInPool());

        final long duration = closeIdleConnections(client);

        assertTrue("closing took " + duration + " ms", duration < MAX_CLOSE_DURATION);
        assertEquals(0, getConnectionManager(client).getConnectionsInPool());
    }

    /**
     * After the read timeout, HttpClient closes the connection. Closing should not block for another read timeout.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testTimedOutRequestFailsAfterReadTimeout() {
        final ApacheHttpClient client = createClient(true);
        final long start = System.currentTimeMillis();

        try {
            client.resource(server.getBaseUrl() + SilentTlsServer.HANG_PATH.substring(1)).get(String.class);
            fail("the request should fail");
        } catch (final ClientHandlerException e) {
            final long duration = System.currentTimeMillis() - start;

            assertTrue("unexpected cause: " + e.getCause(), e.getCause() instanceof SocketTimeoutException);
            assertTrue("the request failed after " + duration + " ms",
                duration < (READ_TIMEOUT + MAX_CLOSE_DURATION));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   safeClose  true, if the SafeCloseHttpConnectionManager should be used
     *
     * @return  DOCUMENT ME!
     */
    private static ApacheHttpClient createClient(final boolean safeClose) {
        final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, READ_TIMEOUT);

        final ApacheHttpClient client = ApacheHttpClient.create(config);

        if (safeClose) {
            final HttpClient httpClient = client.getClientHandler().getHttpClient();
            final SafeCloseHttpConnectionManager connectionManager = new SafeCloseHttpConnectionManager();

            connectionManager.setParams(httpClient.getHttpConnectionManager().getParams());
            httpClient.setHttpConnectionManager(connectionManager);
        }

        return client;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   client  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static MultiThreadedHttpConnectionManager getConnectionManager(final ApacheHttpClient client) {
        return (MultiThreadedHttpConnectionManager)client.getClientHandler().getHttpClient()
                    .getHttpConnectionManager();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   client  DOCUMENT ME!
     *
     * @return  the duration in milliseconds
     */
    private static long closeIdleConnections(final ApacheHttpClient client) {
        final long start = System.currentTimeMillis();
        getConnectionManager(client).closeIdleConnections(0);

        return System.currentTimeMillis() - start;
    }
}
