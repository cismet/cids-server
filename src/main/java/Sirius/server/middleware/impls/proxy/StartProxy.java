/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.Server;
import Sirius.server.ServerExit;
import Sirius.server.ServerExitError;
import Sirius.server.ServerStatus;
import Sirius.server.ServerType;
import Sirius.server.Shutdown;
import Sirius.server.property.ServerProperties;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import org.openide.util.Lookup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Collection;
import java.util.MissingResourceException;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ServerSecurityManager;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cids.server.ws.rest.RESTfulService;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class StartProxy {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(StartProxy.class);

    private static StartProxy instance;

    //~ Instance fields --------------------------------------------------------

    private final transient CallServerService callServer;
    private final transient String siriusRegistryIP;
    private final transient Server serverInfo;
    private final transient ServerStatus status;
    private final transient ServerProperties serverProperties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StartProxy object.
     *
     * @param   configFile  DOCUMENT ME!
     *
     * @throws  ServerExitError        DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private StartProxy(final String configFile) throws ServerExitError {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating new StartProxy: " + configFile); // NOI18N
        }

        // initialise server properties
        serverProperties = initServerProperties(configFile);

        // init log4j
        final String fileName = serverProperties.getLog4jPropertyFile();
        if ((fileName != null) && !fileName.isEmpty()) {
            try {
                try(final InputStream configStream = new FileInputStream(fileName)) {
                    final ConfigurationSource source = new ConfigurationSource(configStream);
                    final LoggerContext context = (LoggerContext)LogManager.getContext(false);
                    context.start(new XmlConfiguration(context, source)); // Apply new configuration
                }
            } catch (final Exception e) {
                LOG.warn("could not initialise Log4J", e);                // NOI18N
            }
        }

        if (ServerProperties.START_MODE__PROXY.equalsIgnoreCase(serverProperties.getStartMode())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("<CS> INFO: starting RESTful pass-through proxy");
            }

            siriusRegistryIP = null;
            serverInfo = null;
            status = null;

            if (!serverProperties.isRestEnabled()) {
                throw new IllegalStateException("if the startmode is proxy then REST must be enabled"); // NOI18N
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("<CS> INFO: pass-through url: " + serverProperties.getServerProxyURL());
            }

            String serverName = "Proxy";
            try {
                serverName = serverProperties.getServerName();
            } catch (final Exception ex) {
                LOG.warn("missing serverName in serverProperties", ex);
            }
            callServer = new RESTfulSerialInterfaceConnector(serverProperties.getServerProxyURL(),
                    serverName,
                    serverProperties.isCompressionEnabled());
            RESTfulService.up(serverProperties);
        } else {
            // init server registry ip
            siriusRegistryIP = initServerRegistryIP(serverProperties);

            // TODO: why sout???
            System.out.println("<CS> INFO: siriusRegistryIP:: " + siriusRegistryIP); // NOI18N
            System.out.println("<CS> INFO: configFile:: " + configFile);             // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info("<CS> INFO: siriusRegistryIP:: " + siriusRegistryIP);       // NOI18N
                LOG.info("<CS> INFO: configFile:: " + configFile);                   // NOI18N
            }

            // create a securitymanager if it is not registered yet
            try {
                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new ServerSecurityManager());
                }
            } catch (final Exception e) {
                final String message = "could not create security manager"; // NOI18N
                LOG.fatal(message, e);
                throw new ServerExitError(message, e);
            }

            // init server
            serverInfo = initServer(serverProperties);

            // init RMI registry
            final Registry rmiRegistry = initRegistry(Integer.valueOf(serverInfo.getRMIPort()));

            // create and bind callserver instance
            callServer = createAndBindProxy(serverProperties);
            status = new ServerStatus();

            // bring up the RESTful Service after initialisation if rest is enabled
            if (serverProperties.isRestEnabled()) {
                try {
                    RESTfulService.up(serverProperties);
                } catch (final ServerExitError e) {
                    LOG.error("could not bring up RESTful interface", e); // NOI18N
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<CS> RMIRegistry does exist...");                                           // NOI18N
                final String[] list;
                try {
                    list = rmiRegistry.list();
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < list.length; i++) {
                        sb.append('\t').append(list[i]);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(" Info <CS> Already registered with RMIRegistry: " + sb.toString()); // NOI18N
                    }
                } catch (final Exception ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("cannot list registered services", ex);                              // NOI18N
                    }
                }
            }
        }
        final Collection<? extends ProxyStartupHook> startupHooks = Lookup.getDefault()
                    .lookupAll(ProxyStartupHook.class);
        for (final ProxyStartupHook hook : startupHooks) {
            try {
                hook.proxyStarted();
            } catch (Exception ex) {
                LOG.error("error durin ServerStartupHook", ex);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   configFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private ServerProperties initServerProperties(final String configFile) throws ServerExitError {
        try {
            return new ServerProperties(configFile);
        } catch (final FileNotFoundException ex) {
            final String message = "given configFile does not exist: " + configFile; // NOI18N
            LOG.fatal(message, ex);
            throw new ServerExitError(message, ex);
        } catch (final IOException ex) {
            final String message = "error while reading config: " + configFile;      // NOI18N
            LOG.fatal(message, ex);
            throw new ServerExitError(message, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   properties  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private String initServerRegistryIP(final ServerProperties properties) throws ServerExitError {
        try {
            final String[] ips = properties.getRegistryIps();
            if (ips.length == 0) {
                final String message = "registry IPs not set in config file, server exit"; // NOI18N
                LOG.fatal(message);
                throw new ServerExitError(message);
            } else {
                final String ip = ips[0];
                if (LOG.isInfoEnabled()) {
                    LOG.info("using registry ip: " + ip);                                  // NOI18N
                }

                return ip;
            }
        } catch (final MissingResourceException mre) {
            final String message = "<CS> FATAL: value for key '" + mre.getMessage() + "' is missing"; // NOI18N
            // TODO: why serr???
            System.err.println(message);
            LOG.fatal(message, mre);
            throw new ServerExitError(mre);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   properties  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private Server initServer(final ServerProperties properties) throws ServerExitError {
        try {
            String rmiPort;
            try {
                rmiPort = properties.getRMIRegistryPort();
            } catch (final MissingResourceException mre) {
                final String warning = "<CS> WARN: value for key " + mre.getMessage() + " is missing"; // NOI18N
                // TODO: why serr???
                System.err.println(warning);
                LOG.warn(warning, mre);

                // defaulting to standard port 1099
                final String message = "<CS> INFO: set default RMI port: 1099"; // NOI18N
                System.out.println(message);
                if (LOG.isInfoEnabled()) {
                    LOG.info(message);
                }
                rmiPort = "1099";                                               // NOI18N
            }

            return new Server(
                    ServerType.CALLSERVER,
                    properties.getServerName(),
                    InetAddress.getLocalHost().getHostAddress(),
                    rmiPort,
                    String.valueOf(properties.getServerPort()));
        } catch (final UnknownHostException e) {
            final String message = "SEVERE: could not find host address for localhost"; // NOI18N
            LOG.fatal(message, e);
            throw new ServerExitError(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   port  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private Registry initRegistry(final int port) throws ServerExitError {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("<CS> DEBUG: getRMIRegistry on port " + port); // NOI18N
            }

            final String message = "<CS> INFO: create RMIRegistry on port " + port; // NOI18N
            System.out.println(message);
            LOG.info(message);
            return LocateRegistry.createRegistry(port);
        } catch (final RemoteException e) {
            final String info = "INFO: cannot create registry on port: " + port;    // NOI18N
            // no registry present, create new registry on rmiPort
            System.out.println(e.getMessage() + " \n" + info);                                    // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(info, e);
            }
            try {
                return LocateRegistry.getRegistry(port);
            } catch (final RemoteException ex) {
                final String fatal = "<CS>SEVERE: no RMIRegistry on port " + port + " available"; // NOI18N
                LOG.fatal(fatal, ex);
                throw new ServerExitError(fatal, ex);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   properties  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    private ProxyImpl createAndBindProxy(final ServerProperties properties) throws ServerExitError {
        try {
            final ProxyImpl proxy = new ProxyImpl(properties);
            final String message = "<CS> INFO: Proxy/Callserver/Broker listening for RESTful requests on port: "
                        + properties.getRestPort();
            if (LOG.isInfoEnabled()) {
                LOG.info(message);
            }
            System.out.println(message);
            return proxy;
        } catch (final RemoteException ex) {
            final String fatal = "cannot create callserver implementation"; // NOI18N
            LOG.fatal(fatal, ex);
            throw new ServerExitError(fatal, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static synchronized StartProxy getInstance() throws IllegalStateException {
        if (instance == null) {
            throw new IllegalStateException("startproxy not up yet"); // NOI18N
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static synchronized StartProxy getServerInstance() throws IllegalStateException {
        return getInstance();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   configFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static synchronized StartProxy getInstance(final String configFile) throws ServerExitError {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance for configfile: " + configFile + " :: instance already present? " + instance); // NOI18N
        }
        if (instance == null) {
            instance = new StartProxy(configFile);
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerStatus getStatus() {
        return status;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Server getInfo() {
        return serverInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CallServerService getCallServer() {
        return callServer;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  ServerExit       Throwable DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public synchronized void shutdown() throws ServerExit, ServerExitError {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shutdown proxy: " + this); // NOI18N
        }

        try {
            final Shutdown shutdown = Shutdown.createShutdown(this);
            shutdown.shutdown();

            if (callServer instanceof ProxyImpl) {
                final ProxyImpl proxyimpl = (ProxyImpl)callServer;
                proxyimpl.unregisterAsObserver(siriusRegistryIP); // NOI18N
                proxyimpl.getNameServer()
                        .unregisterServer(
                            serverInfo.getType(),
                            serverInfo.getName(),
                            serverInfo.getIP(),
                            serverInfo.getServerPort());

                RESTfulService.down();

                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("unbind callserver");                                          // NOI18N
                    }
                    Naming.unbind(serverInfo.getBindString());                                   // NOI18N
                } catch (final NotBoundException e) {
                    LOG.warn("callserver not available (anymore), probably already unbound", e); // NOI18N
                }
            } else {
                RESTfulService.down();
            }

            final String message = "Server shutdown success"; // NOI18N
            if (LOG.isInfoEnabled()) {
                LOG.info(message);
            }
            // TODO: a throwable is thrown to indicate success?! This is bad code style. Refactor
            throw new ServerExit(message);
        } catch (final Exception e) {
            final String message = "Server shutdown failure, integrity no longer guaranteed"; // NOI18N
            LOG.fatal(message, e);
            throw new ServerExitError(message, e);
        } finally {
            instance = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static void main(final String[] args) throws ServerExitError {
        if (args == null) {
            throw new ServerExitError("no cli params");     // NOI18N
        } else if (args.length < 1) {
            throw new ServerExitError("too few arguments"); // NOI18N
        }

        StartProxy.getInstance(args[0]);
    }
}
