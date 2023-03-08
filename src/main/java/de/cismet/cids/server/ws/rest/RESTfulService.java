/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExitError;
import Sirius.server.middleware.impls.proxy.UserServiceImpl;
import Sirius.server.property.ServerProperties;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.log4j.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.lang.management.ManagementFactory;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import de.cismet.cids.server.connectioncontext.ConnectionContextBackend;
import de.cismet.cids.server.connectioncontext.ConnectionContextManagement;

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
    private static boolean threadNamingEnabled = false;

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
        final QueuedThreadPool btp = new QueuedThreadPool();
        System.out.println("<CS> INFO: min Jetty Threads set to:" + properties.getRestServerMinThreads());
        System.out.println("<CS> INFO: max Jetty Threads set to:" + properties.getRestServerMaxThreads());

        btp.setMinThreads(properties.getRestServerMinThreads());
        btp.setMaxThreads(properties.getRestServerMaxThreads());
        server = new Server(btp);
        threadNamingEnabled = properties.isRestThreadNamingEnabled();
        server.addConnector(getConnector(properties));

        final ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS); // NOI18N
        context.addServlet(servlet, "/*");                                                                            // NOI18N

        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName name = new ObjectName("de.cismet.cids.rest.broker:type=RestMBean");
            final RESTManagement restMB = new RESTManagement();
            mbs.registerMBean(restMB, name);
            ConnectionContextBackend.getInstance().registerMBean();

//            name = new ObjectName("Sirius.server.middleware.impls.proxy:type=UserServiceManagementMBean");
//            restMB = new RESTManagement();
//            mbs.registerMBean(restMB, name);
            UserServiceImpl.registerMBean();

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
        final ServerConnector connector;
        if (properties.isRestDebug()) {
            LOG.warn("server REST interface is in debug mode, no security applied!"); // NOI18N
            connector = new ServerConnector(server);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("server REST interface uses SSL connector");                 // NOI18N
            }

            try {
                final SslContextFactory.Server ssl = new SslContextFactory.Server();
//                ssl.setMaxIdleTime(30000);

                ssl.setKeyStorePath(properties.getRestServerKeystore());
                ssl.setKeyStorePassword(properties.getRestServerKeystorePW());
                ssl.setKeyManagerPassword(properties.getRestServerKeystoreKeyPW());

                final boolean clientAuth = properties.isRestClientAuth();
                if (clientAuth) {
                    ssl.setTrustStorePath(properties.getRestClientKeystore());
                    ssl.setTrustStorePassword(properties.getRestClientKeystorePW());
                }
                ssl.setWantClientAuth(clientAuth);
                ssl.setNeedClientAuth(clientAuth);

                connector = new ServerConnector(server, ssl);
            } catch (final Exception e) {
                final String message = "cannot initialise SSL connector"; // NOI18N
                LOG.error(message, e);
                throw new ServerExitError(message, e);
            }
        }
        connector.setPort(port);
        connector.setAcceptedReceiveBufferSize(HEADER_BUFFER_SIZE);

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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isThreadNamingEnabled() {
        return threadNamingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  threadNamingEnabled  DOCUMENT ME!
     */
    public static void setThreadNamingEnabled(final boolean threadNamingEnabled) {
        RESTfulService.threadNamingEnabled = threadNamingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  max  DOCUMENT ME!
     */
    public static void setMaxThreads(final int max) {
        if (instance.server.getThreadPool() instanceof QueuedThreadPool) {
            ((QueuedThreadPool)instance.server.getThreadPool()).setMaxThreads(max);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int getMaxThreads() {
        if (instance.server.getThreadPool() instanceof QueuedThreadPool) {
            return ((QueuedThreadPool)instance.server.getThreadPool()).getMaxThreads();
        } else {
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  min  DOCUMENT ME!
     */
    public static void setMinThreads(final int min) {
        if (instance.server.getThreadPool() instanceof QueuedThreadPool) {
            ((QueuedThreadPool)instance.server.getThreadPool()).setMinThreads(min);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int getMinThreads() {
        if (instance.server.getThreadPool() instanceof QueuedThreadPool) {
            return ((QueuedThreadPool)instance.server.getThreadPool()).getMinThreads();
        } else {
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getThreadingStatus() {
        final int threadCount = instance.server.getThreadPool().getThreads();
        final int idleTC = instance.server.getThreadPool().getIdleThreads();
        final HashMap<String, String> vals = new HashMap<String, String>();
        if (instance.server.getThreadPool() instanceof QueuedThreadPool) {
            vals.put("MaxThreads", ((QueuedThreadPool)instance.server.getThreadPool()).getMaxThreads() + "");
            vals.put("MinThreads", ((QueuedThreadPool)instance.server.getThreadPool()).getMinThreads() + "");
        }
        final boolean isLowOnThreads = instance.server.getThreadPool().isLowOnThreads();
        return idleTC + " of " + threadCount + " Threads are idle. AlertOnLowThreads:" + isLowOnThreads
                    + " Additional Info: " + vals;
    }
}
