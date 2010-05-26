/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExitError;
import Sirius.server.middleware.impls.proxy.StartProxy;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.log4j.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
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
//    private final transient SelectorThread selector;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulService object.
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private RESTfulService(final int port) throws ServerExitError {
        final StartProxy proxy = StartProxy.getInstance();
        this.port = port;

//        final URI baseURI = URI.create("http://" + proxy.getServer().getIP() + ":" + port + "/"); // NOI18N
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "de.cismet.cids.server.ws.rest"); // NOI18N

        final ServletHolder servlet = new ServletHolder(ServletContainer.class);
        servlet.setInitParameters(initParams);

        server = new Server(port);
        for (final Connector c : server.getConnectors()) {
            c.setHeaderBufferSize(HEADER_BUFFER_SIZE);
        }

        final Context context = new Context(server, "/", Context.SESSIONS); // NOI18N
        context.addServlet(servlet, "/*");                                  // NOI18N

        try {
//            final ServletAdapter adapter = new ServletAdapter();
//            adapter.addInitParameter("com.sun.jersey.config.property.packages", "de.cismet.cids.server.ws.rest"); // NOI18N
//            adapter.setServletInstance(ServletContainer.class.newInstance());
//            adapter.setResourcesContextPath(baseURI.getRawPath());
//
//            selector = new SelectorThread();
//            selector.setMaxHttpHeaderSize(HEADER_SIZE);
//            selector.setMaxPostSize(HEADER_SIZE);
//            selector.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
//            selector.setPort(port);
//            selector.setAdapter(adapter);
//
//            selector.listen();
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
     * @param   port  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static synchronized void up(final int port) throws ServerExitError {
        if (!isUp()) {
            instance = new RESTfulService(port);
            if (LOG.isInfoEnabled()) {
                LOG.info("RESTfulService started @ port: " + port); // NOI18N
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
