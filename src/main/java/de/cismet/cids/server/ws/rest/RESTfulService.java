/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExitError;
import Sirius.server.property.ServerProperties;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.log4j.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class RESTfulService {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RESTfulService.class);

    private static RESTfulService instance;

    private static final int HEADER_BUFFER_SIZE = 512 * 1024; // = 512kb

    //~ Instance fields --------------------------------------------------------

    private final transient int port;
    private final transient Server server;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulService object.
     *
     * @param   properties  port DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private RESTfulService(final ServerProperties properties) throws ServerExitError {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "de.cismet.cids.server.ws.rest"); // NOI18N

        final ServletHolder servlet = new ServletHolder(ServletContainer.class);
        servlet.setInitParameters(initParams);

        this.port = properties.getRestPort();
        server = new Server();
        server.addConnector(getConnector(properties));

        final Context context = new Context(server, "/", Context.SESSIONS); // NOI18N
        context.addServlet(servlet, "/*");                                  // NOI18N

        try {
            server.start();
        } catch (final Exception ex) {
            final String message = "could not create jetty web container on port: " + port; // NOI18N
            LOG.error(message, ex);
            throw new ServerExitError(message, ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   properties  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private Connector getConnector(final ServerProperties properties) throws ServerExitError {
        final Connector connector;
        // we are still in test mode so we stay in debug mode
        if (true) {                                                                   // properties.isRestDebug()) {
            LOG.warn("server REST interface is in debug mode, no security applied!"); // NOI18N
            connector = new SocketConnector();
        } else {
            try {
                final SslSocketConnector ssl = new SslSocketConnector();
                ssl.setMaxIdleTime(30000);
                ssl.setKeystore(properties.getRestServerKeystore());
                ssl.setPassword(properties.getRestServerKeystorePW());
                ssl.setKeyPassword(properties.getRestServerKeystoreKeyPW());

                final boolean clientAuth = properties.isRestClientAuth();
                if (clientAuth) {
                    ssl.setTruststore(properties.getRestClientKeystore());
                    ssl.setTrustPassword(properties.getRestClientKeystorePW());
                }
                ssl.setWantClientAuth(clientAuth);
                ssl.setNeedClientAuth(clientAuth);

                connector = ssl;
            } catch (final Exception e) {
                final String message = "cannot initialise ssl connector"; // NOI18N
                LOG.error(message, e);
                throw new ServerExitError(message, e);
            }
        }

        connector.setPort(port);
        connector.setHeaderBufferSize(HEADER_BUFFER_SIZE);

        return connector;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   properties  port DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static synchronized void up(final ServerProperties properties) throws ServerExitError {
        if (!isUp()) {
            instance = new RESTfulService(properties);
            if (LOG.isInfoEnabled()) {
                LOG.info("RESTfulService started @ port: " + properties.getRestPort()); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static synchronized void down() {
        if (isUp()) {
            try {
                instance.server.stop();
//                instance.selector.stopEndpoint();
                if (LOG.isInfoEnabled()) {
                    LOG.info("RESTfulService stopped @ port: " + getPort()); // NOI18N
                }
            } catch (final Exception ex) {
//                LOG.warn("could not stop jetty", ex);
                LOG.warn("could not stop grizzly", ex);                      // NOI18N
            }
            instance = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized boolean isUp() {
        return instance != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized int getPort() {
        if (isUp()) {
            return instance.port;
        } else {
            return -1;
        }
    }
}
