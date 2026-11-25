/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache.ApacheHttpClient;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests, that the JerseyClientCache can recreate its pool, if the server does not respond anymore, e.g. because a
 * firewall has silently dropped the connections.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JerseyClientCacheTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final int MAX_RECREATE_DURATION = 1000;
    private static final int MAX_CLOSE_DURATION = 2000;
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
     * The clients of the cache use the default read timeout of 10 minutes. Recreating the pool must neither block until
     * the old connections are closed, nor must closing the old connections wait for the read timeout.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testRecreatePoolDoesNotBlock() throws Exception {
        final JerseyClientCache cache = JerseyClientCache.getInstance(server.getBaseUrl(), null);
        final Client oldClient = cache.getJerseyHttpClient("ok");

        assertEquals("ok", oldClient.resource(server.getBaseUrl() + "ok").get(String.class));
        assertEquals(1, getConnectionManager(oldClient).getConnectionsInPool());

        final long start = System.currentTimeMillis();
        cache.recreatePool();
        final long duration = System.currentTimeMillis() - start;

        assertTrue("recreating the pool took " + duration + " ms", duration < MAX_RECREATE_DURATION);
        assertNotSame(oldClient, cache.getJerseyHttpClient("ok"));

        // the old connection is closed asynchronously
        final long deadline = System.currentTimeMillis() + MAX_CLOSE_DURATION;

        while ((getConnectionManager(oldClient).getConnectionsInPool() > 0)
                    && (System.currentTimeMillis() < deadline)) {
            Thread.sleep(50);
        }

        assertEquals(0, getConnectionManager(oldClient).getConnectionsInPool());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   client  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static MultiThreadedHttpConnectionManager getConnectionManager(final Client client) {
        return (MultiThreadedHttpConnectionManager)((ApacheHttpClient)client).getClientHandler().getHttpClient()
                    .getHttpConnectionManager();
    }
}
