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
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;


import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;


import org.apache.log4j.Logger;


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

    //~ Instance fields --------------------------------------------------------

    private final transient int port;
//    private final transient Server server;
    private final transient SelectorThread selector;

    private static final int HEADER_SIZE = 16 * 1024;

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

        final URI baseURI = URI.create("http://" + proxy.getServer().getIP() + ":" + port + "/");            // NOI18N

//        final ServletHolder servlet = new ServletHolder(ServletContainer.class);
//        servlet.setInitParameters(initParams);
//
//        server = new Server(port);
//        final Context context = new Context(server, "/", Context.SESSIONS);
//        context.addServlet(servlet, "/*");

        try {
            final ServletAdapter adapter = new ServletAdapter();
            adapter.addInitParameter("com.sun.jersey.config.property.packages", "de.cismet.cids.server.ws.rest"); // NOI18N
            adapter.setServletInstance(ServletContainer.class.newInstance());
            adapter.setResourcesContextPath(baseURI.getRawPath());

            selector = new SelectorThread();
            selector.setMaxHttpHeaderSize(HEADER_SIZE);
            selector.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
            selector.setPort(port);
            selector.setAdapter(adapter);

            selector.listen();
//            server.start();
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
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static synchronized void down() {
        if (isUp()) {
            try {
//                instance.server.stop();
                instance.selector.stopEndpoint();
            } catch (final Exception ex) {
//                LOG.warn("could not stop jetty", ex);
                LOG.warn("could not stop grizzly", ex);
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
