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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    private static String serverResources = null;
    private static boolean noConfiguration = false;

    //~ Instance fields --------------------------------------------------------

    private final transient Map<String, CircularObjectPool<Client>> clientCache;
    private final transient Proxy proxy;
    private final String rootResource;
    private int poolSize = DEFAULT_POOL_SIZE;

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

                if ((configuration != null) && (configuration.getDefaultPoolSize() != null)) {
                    poolSize = configuration.getDefaultPoolSize();
                }

                if (configuration != null) {
                    for (final RestHttpClientConfiguration.PathConfiguration pathConfig : configuration.getConfig()) {
                        final String path = pathConfig.getPath();
                        final CircularObjectPool<Client> pool = createClientsForPath(path, pathConfig.getPoolSize());

                        clientCache.put(path, pool);
                    }
                } else {
                    noConfiguration = true;
                }
            } catch (Exception e) {
                LOG.error("Cannot read configuration", e);
                noConfiguration = true;
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

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

        final Client[] clientArray = new Client[poolsize];

        for (int i = 0; i < poolsize; ++i) {
            clientArray[i] = ApacheHttpClient.create(clientConfig);
        }

        return new CircularObjectPool<>(Collections.unmodifiableList(Arrays.asList(clientArray)));
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
