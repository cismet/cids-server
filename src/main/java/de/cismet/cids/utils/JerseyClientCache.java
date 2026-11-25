/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.ApacheHttpClientHandler;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.netutil.Proxy;

import de.cismet.tools.collections.CircularObjectPool;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JerseyClientCache {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(JerseyClientCache.class);
    private static final int DEFAULT_POOL_SIZE = (int)Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0);
    private static final int TIMEOUT = 10000;
    // without a read timeout, a request hangs forever, if the server does not respond
    private static final int DEFAULT_READ_TIMEOUT = 10 * 60 * 1000;
    // additional delay, before the connections of replaced clients will be closed
    private static final int SHUTDOWN_DELAY_MARGIN = 60 * 1000;
    // pooled connections, which were not used for this time, will be closed. Otherwise, a firewall can silently drop
    // such a connection and the next request on it hangs until the read timeout expires
    private static final int DEFAULT_IDLE_CONNECTION_TIMEOUT = 60 * 1000;
    private static final int MIN_IDLE_CHECK_INTERVAL = 1000;
    private static final int MAX_IDLE_CHECK_INTERVAL = 30 * 1000;
    // the configuration of the client. The server uses the server resources instead
    private static final String CLIENT_CONFIGURATION = "de/cismet/cids/client/http_pool_config.json";
    private static String serverResources = null;
    private static boolean noConfiguration = false;

    //~ Instance fields --------------------------------------------------------

    private final transient Map<String, CircularObjectPool<Client>> clientCache;
    private final transient Proxy proxy;
    private final String rootResource;
    private final transient Map<String, List<Client>> clientLists = new HashMap<>();
    private final transient Map<String, Integer> pathReadTimeouts = new HashMap<>();
    private int poolSize = DEFAULT_POOL_SIZE;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
    private long lastRecached = 0;

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

                @Override
                public Thread newThread(final Runnable r) {
                    final Thread t = new Thread(r, "JerseyClientCache-connections");
                    // the scheduled tasks should not prevent the jvm from exiting
                    t.setDaemon(true);

                    return t;
                }
            });

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JerseyClientCache object.
     *
     * @param  resource  DOCUMENT ME!
     * @param  proxy     DOCUMENT ME!
     */
    private JerseyClientCache(final String resource, final Proxy proxy) {
        clientCache = new HashMap<>();
        this.proxy = proxy;
        this.rootResource = resource;

        if (!noConfiguration && (serverResources != null)) {
            try {
                final RestHttpClientConfiguration configuration = ServerResourcesLoader.getInstance()
                            .loadJson(GeneralServerResources.CONFIG_REST_HTTP_CLIENT_JSON.getValue(),
                                RestHttpClientConfiguration.class);

                if (configuration != null) {
                    applyConfiguration(configuration);
                } else {
                    noConfiguration = true;
                }
            } catch (Exception e) {
                LOG.error("Cannot read configuration", e);
                noConfiguration = true;
            }
        } else if (serverResources == null) {
            // the client has no server resources, so the configuration is read from the classpath. A missing
            // configuration should not set noConfiguration, because the server resources can be set later
            final RestHttpClientConfiguration configuration = loadClientConfiguration();

            if (configuration != null) {
                try {
                    applyConfiguration(configuration);
                } catch (Exception e) {
                    LOG.error("Cannot apply the client configuration " + CLIENT_CONFIGURATION, e);
                }
            }
        }

        if (idleConnectionTimeout > 0) {
            final long checkInterval = Math.max(
                    MIN_IDLE_CHECK_INTERVAL,
                    Math.min(MAX_IDLE_CHECK_INTERVAL, idleConnectionTimeout / 2));

            exec.scheduleWithFixedDelay(new Runnable() {

                    @Override
                    public void run() {
                        closeIdleConnections();
                    }
                }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Applies the given configuration to this cache.
     *
     * @param  configuration  the configuration to apply
     */
    private void applyConfiguration(final RestHttpClientConfiguration configuration) {
        if (configuration.getDefaultPoolSize() != null) {
            poolSize = configuration.getDefaultPoolSize();
        }

        if (configuration.getDefaultReadTimeout() != null) {
            readTimeout = configuration.getDefaultReadTimeout();
        }

        if (configuration.getIdleConnectionTimeout() != null) {
            idleConnectionTimeout = configuration.getIdleConnectionTimeout();
        }

        if (configuration.getConfig() != null) {
            for (final RestHttpClientConfiguration.PathConfiguration pathConfig : configuration.getConfig()) {
                final String path = pathConfig.getPath();

                if (pathConfig.getReadTimeout() != null) {
                    pathReadTimeouts.put(path, pathConfig.getReadTimeout());
                }

                final int pathPoolSize = (pathConfig.getPoolSize() != null) ? pathConfig.getPoolSize() : poolSize;

                clientCache.put(path, createClientsForPath(path, pathPoolSize));
            }
        }
    }

    /**
     * Loads the configuration of the client from the classpath.
     *
     * @return  the configuration or null, if there is no valid configuration
     */
    private static RestHttpClientConfiguration loadClientConfiguration() {
        try(final InputStream is = JerseyClientCache.class.getClassLoader().getResourceAsStream(
                            CLIENT_CONFIGURATION)) {
            if (is == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No client configuration found: " + CLIENT_CONFIGURATION);
                }

                return null;
            }

            return new ObjectMapper().readValue(is, RestHttpClientConfiguration.class);
        } catch (final Exception e) {
            LOG.error("Cannot read the client configuration " + CLIENT_CONFIGURATION, e);

            return null;
        }
    }

    /**
     * Closes all pooled connections of the current clients, which were not used for longer than the idle connection
     * timeout. Connections, which are in use, will not be closed. The connections are closed without holding the lock
     * of this cache, so that requests are not blocked, if closing a connection takes longer.
     */
    private void closeIdleConnections() {
        final List<Client> clients = new ArrayList<>();

        synchronized (this) {
            for (final List<Client> pathClients : clientLists.values()) {
                clients.addAll(pathClients);
            }
        }

        try {
            for (final Client client : clients) {
                if (client instanceof ApacheHttpClient) {
                    ((ApacheHttpClient)client).getClientHandler()
                            .getHttpClient()
                            .getHttpConnectionManager()
                            .closeIdleConnections(idleConnectionTimeout);
                }
            }
        } catch (final Exception e) {
            // an exception would cancel the periodic execution
            LOG.warn("Error while closing idle connections", e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void recreatePool() {
        if ((System.currentTimeMillis() - lastRecached) > 10000) {
            for (final String path : clientCache.keySet()) {
                final List<Client> oldClients = clientLists.get(path);

                clientCache.put(path, createClientsForPath(path, poolSize));
                closeClients(path, oldClients);
            }

            lastRecached = System.currentTimeMillis();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  serverResourcesPath  DOCUMENT ME!
     * @param  proxy                DOCUMENT ME!
     */
    public static synchronized void setServerResources(final String serverResourcesPath, final Proxy proxy) {
        serverResources = serverResourcesPath;
        ServerResourcesLoader.getInstance().setResourcesBasePath(serverResources);

        try {
            final RestHttpClientConfiguration configuration = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.CONFIG_REST_HTTP_CLIENT_JSON.getValue(),
                            RestHttpClientConfiguration.class);

            if ((configuration != null) && (proxy == LazyInitialiser.current_proxy)) {
                if (configuration.getRootResources() != null) {
                    for (final String rootResources : configuration.getRootResources()) {
                        getInstance(rootResources, proxy);
                    }
                }
            } else {
                noConfiguration = true;
            }
        } catch (Exception e) {
            LOG.error("Cannot read configuration", e);
            noConfiguration = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   resource  DOCUMENT ME!
     * @param   proxy     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized JerseyClientCache getInstance(final String resource, final Proxy proxy) {
        return LazyInitialiser.getInstance(resource, proxy);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   path      DOCUMENT ME!
     * @param   poolsize  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CircularObjectPool<Client> createClientsForPath(final String path, final int poolsize) {
        // remove leading '/' if present
        final String resource;
        if (path == null) {
            resource = rootResource;
        } else if ('/' == path.charAt(0)) {
            resource = rootResource + path.substring(1, path.length() - 1);
        } else {
            resource = rootResource + path;
        }

        LOG.info("create jersey http clients for (resource/path): " + resource);

        final DefaultApacheHttpClientConfig clientConfig = new DefaultApacheHttpClientConfig();
        if ((proxy != null) && proxy.isEnabledFor(resource)) {
            clientConfig.getProperties()
                    .put(
                        ApacheHttpClientConfig.PROPERTY_PROXY_URI,
                        "http://"
                        + proxy.getHost()
                        + ":"
                        + proxy.getPort());
            if (LOG.isDebugEnabled()) {
                LOG.debug("proxy set: " + proxy);
            }

            if ((proxy.getUsername() != null) && (proxy.getPassword() != null)) {
                clientConfig.getState()
                        .setProxyCredentials(
                            null,
                            proxy.getHost(),
                            proxy.getPort(),
                            proxy.getUsername(),
                            proxy.getPassword(),
                            proxy.getDomain(),
                            "");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("proxy credentials set: " + proxy);
                }
            }
        }

        clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, TIMEOUT);
        clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, getReadTimeout(path));

        final Client[] clientArray = new Client[poolsize];

        for (int i = 0; i < poolsize; ++i) {
            final ApacheHttpClient client = ApacheHttpClient.create(clientConfig);
            final HttpClient httpClient = client.getClientHandler().getHttpClient();
            // closing a dropped tls connection should not block until the read timeout expires
            final SafeCloseHttpConnectionManager connectionManager = new SafeCloseHttpConnectionManager();

            connectionManager.setParams(httpClient.getHttpConnectionManager().getParams());
            httpClient.setHttpConnectionManager(connectionManager);
            clientArray[i] = client;
        }

        final List<Client> clients = Collections.unmodifiableList(Arrays.asList(clientArray));
        clientLists.put(path, clients);

        return new CircularObjectPool<>(clients);
    }

    /**
     * Closes the connections of the given clients, without aborting running requests. The unused connections will be
     * closed immediately. All other connections will be closed, after the read timeout of the path has expired, because
     * all requests, which use these clients, should be finished or failed at this time. If the read timeout is
     * disabled, only the unused connections will be closed. The connections are closed asynchronously, so that the
     * caller, which usually holds the lock of this cache, is not blocked, if closing a connection takes longer.
     *
     * @param  path     the path of the clients
     * @param  clients  the clients to close. Can be null
     */
    private void closeClients(final String path, final List<Client> clients) {
        if (clients == null) {
            return;
        }

        final List<HttpConnectionManager> connectionManagers = new ArrayList<>();

        for (final Client client : clients) {
            if (client instanceof ApacheHttpClient) {
                connectionManagers.add(((ApacheHttpClient)client).getClientHandler().getHttpClient()
                            .getHttpConnectionManager());
            }
        }

        if (connectionManagers.isEmpty()) {
            return;
        }

        exec.execute(new Runnable() {

                @Override
                public void run() {
                    for (final HttpConnectionManager connectionManager : connectionManagers) {
                        try {
                            // close only the unused connections, so that running requests will not be aborted
                            connectionManager.closeIdleConnections(0);
                        } catch (final Exception e) {
                            LOG.warn("Error while closing the connections of the replaced jersey http clients", e);
                        }
                    }
                }
            });

        final int timeout = getReadTimeout(path);

        if (timeout > 0) {
            exec.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("close the connections of the replaced jersey http clients for path: " + path);
                        }

                        for (final HttpConnectionManager connectionManager : connectionManagers) {
                            if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
                                ((MultiThreadedHttpConnectionManager)connectionManager).shutdown();
                            }
                        }
                    }
                }, timeout + SHUTDOWN_DELAY_MARGIN, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Determines the read timeout for the given path. A path specific timeout from the configuration takes precedence
     * over the default read timeout. A value of 0 disables the timeout.
     *
     * @param   path  DOCUMENT ME!
     *
     * @return  the read timeout in milliseconds
     */
    private Integer getReadTimeout(final String path) {
        final Integer pathReadTimeout = pathReadTimeouts.get(path);

        return (pathReadTimeout != null) ? pathReadTimeout : readTimeout;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   path  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized Client getJerseyHttpClient(final String path) {
        if (!clientCache.containsKey(path)) {
            clientCache.put(path, createClientsForPath(path, poolSize));
        }

        return clientCache.get(path).next();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final Map<String, JerseyClientCache> INSTANCES = new HashMap<>();
        private static transient Proxy current_proxy = null;
        private static boolean proxySet = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   resource  DOCUMENT ME!
         * @param   proxy     DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private static JerseyClientCache getInstance(final String resource, final Proxy proxy) {
            if (!proxySet) {
                current_proxy = proxy;
                proxySet = true;
            } else if ((current_proxy != proxy)
                        || (((current_proxy != null) || (proxy != null)) && !current_proxy.equals(proxy))) {
                // the proxy has changed
                INSTANCES.clear();
                current_proxy = proxy;
            }

            if (!INSTANCES.containsKey(resource)) {
                INSTANCES.put(resource, new JerseyClientCache(resource, proxy));
            }

            return INSTANCES.get(resource);
        }
    }
}
