/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;

import Sirius.util.image.Image;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.ws.SSLConfig;

import de.cismet.cidsx.server.search.builtin.legacy.LightweightMetaObjectsByQuerySearch;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.netutil.Proxy;

import de.cismet.tools.Converter;

import static de.cismet.cids.server.ws.rest.RESTfulSerialInterface.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
// TODO: refine exception handling
public final class RESTfulSerialInterfaceConnector implements CallServerService {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RESTfulSerialInterfaceConnector.class);
    private static final String MULTITHREADEDHTTPCONNECTION_IGNORE_EXCEPTION =
        "Interrupted while waiting in MultiThreadedHttpConnectionManager";
    private static final int TIMEOUT = 10000;

    //~ Instance fields --------------------------------------------------------

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;

    private final transient Proxy proxy;
    private final boolean compressionEnabled;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     */
    @Deprecated
    public RESTfulSerialInterfaceConnector(final String rootResource) {
        this(rootResource, null, null, false);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource        DOCUMENT ME!
     * @param  compressionEnabled  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource, final boolean compressionEnabled) {
        this(rootResource, null, null, compressionEnabled);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         config proxyURL DOCUMENT ME!
     */
    @Deprecated
    public RESTfulSerialInterfaceConnector(final String rootResource, final Proxy proxy) {
        this(rootResource, proxy, null, false);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
     */
    @Deprecated
    public RESTfulSerialInterfaceConnector(final String rootResource, final SSLConfig sslConfig) {
        this(rootResource, null, sslConfig, false);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource        DOCUMENT ME!
     * @param  proxy               DOCUMENT ME!
     * @param  compressionEnabled  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource,
            final Proxy proxy,
            final boolean compressionEnabled) {
        this(rootResource, proxy, null, compressionEnabled);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource        DOCUMENT ME!
     * @param  sslConfig           DOCUMENT ME!
     * @param  compressionEnabled  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource,
            final SSLConfig sslConfig,
            final boolean compressionEnabled) {
        this(rootResource, null, sslConfig, compressionEnabled);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
     */
    @Deprecated
    public RESTfulSerialInterfaceConnector(final String rootResource,
            final Proxy proxy,
            final SSLConfig sslConfig) {
        this(rootResource, proxy, sslConfig, false);
    }
    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource        DOCUMENT ME!
     * @param  proxy               proxyConfig proxyURL DOCUMENT ME!
     * @param  sslConfig           DOCUMENT ME!
     * @param  compressionEnabled  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource,
            final Proxy proxy,
            final SSLConfig sslConfig,
            final boolean compressionEnabled) {
        if (sslConfig == null) {
            LOG.warn("cannot initialise ssl because sslConfig is null"); // NOI18N
        } else {
            initSSL(sslConfig);
        }

        // add training '/' to the root resource if not present
        if ('/' == rootResource.charAt(rootResource.length() - 1)) {
            this.rootResource = rootResource;
        } else {
            this.rootResource = rootResource + "/"; // NOI18N
        }

        if (proxy == null) {
            this.proxy = new Proxy();
        } else {
            this.proxy = proxy;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("using proxy: " + proxy); // NOI18N
        }

        clientCache = new HashMap<String, Client>();
        this.compressionEnabled = compressionEnabled;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sslConfig  DOCUMENT ME!
     *
     * @throws  SSLInitializationException  IllegalStateException SSLInititializationException IllegalStateException
     *                                      DOCUMENT ME!
     */
    private void initSSL(final SSLConfig sslConfig) {
        if (LOG.isInfoEnabled()) {
            LOG.info("initialising ssl connection: " + sslConfig); // NOI18N
        }

        try {
            // server certificate for trustmanager
            // should never be null because otherwise the client cannot be sure to be communicating with the correct
            // server
            final TrustManagerFactory tmf;
            if (sslConfig.getServerKeystore() == null) {
                tmf = null;
                LOG.info("no server certificates provided by SSLConfig"); // NOI18N
            } else {
                tmf = TrustManagerFactory.getInstance(SSLConfig.TMF_SUNX509);
                tmf.init(sslConfig.getServerKeystore());
            }

            // client certificate and key for key manager
            // if server does not require client authentication there is no need to provide a client keystore
            final KeyManagerFactory kmf;
            if (sslConfig.getClientKeystore() == null) {
                kmf = null;
            } else {
                kmf = KeyManagerFactory.getInstance(SSLConfig.TMF_SUNX509);
                kmf.init(sslConfig.getClientKeystore(), sslConfig.getClientKeyPW());
            }

            // init context
            final SSLContext context = SSLContext.getInstance(SSLConfig.CONTEXT_TYPE_TLS);

            // Use the CidsTrustManager to validate the default certificates and the cismet certificate
            final CidsTrustManager trustManager;
            X509TrustManager cidsManager = null;
            TrustManager[] trustManagerArray = null;

            if ((tmf != null) && (tmf.getTrustManagers() != null) && (tmf.getTrustManagers().length == 1)) {
                if (tmf.getTrustManagers()[0] instanceof X509TrustManager) {
                    cidsManager = (X509TrustManager)tmf.getTrustManagers()[0];
                }
            }

            try {
                trustManager = new CidsTrustManager(cidsManager);
                trustManagerArray = new TrustManager[] { trustManager };
            } catch (Exception e) {
                LOG.error("Cannot create CidsTrustManager.", e);
                trustManagerArray = (tmf == null) ? null : tmf.getTrustManagers();
            }

            context.init(
                (kmf == null) ? null : kmf.getKeyManagers(),
                trustManagerArray,
                null);

            SSLContext.setDefault(context);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new SSLHostnameVerifier());
        } catch (final NoSuchAlgorithmException e) {
            throw new SSLInitializationException("system does not support SSL", e);            // NOI18N       (kmf ==
                                                                                               // null) ? null :
                                                                                               // kmf.getKeyManagers(),
        } catch (final KeyStoreException e) {
            throw new SSLInitializationException("system does not support java keystores", e); // NOI18N
        } catch (final KeyManagementException e) {
            throw new SSLInitializationException("ssl context init properly initialised", e);  // NOI18N
        } catch (final UnrecoverableKeyException e) {
            throw new SSLInitializationException("cannot get key from keystore", e);           // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRootResource() {
        return rootResource;
    }

    /**
     * Creates a {@link WebResource.Builder} from the given path. Equal to <code>createWebResourceBuilder(path,
     * null)</code>.
     *
     * @param   path  the path relative to the root resource
     *
     * @return  a <code>WebResource.Builder</code> ready to perform an operation (GET, POST, PUT...)
     *
     * @see     #createWebResourceBuilder(java.lang.String, java.util.Map)
     */
    public WebResource.Builder createWebResourceBuilder(final String path) {
        return createWebResourceBuilder(path, null);
    }

    /**
     * Creates a {@link WebResource.Builder} from the given path and the given params. The given path will be appended
     * to the root path of this connector, thus shall denote a path relative to the root resource. The given {@link Map}
     * of queryParams will be appended to the query.
     *
     * @param   path         the path relative to the root resource
     * @param   queryParams  parameters of the query, may be null or empty.
     *
     * @return  a <code>WebResource.Builder</code> ready to perform an operation (GET, POST, PUT...)
     */
    public WebResource.Builder createWebResourceBuilder(final String path, final Map<String, String> queryParams) {
        // remove leading '/' if present
        final String resource;
        if (path == null) {
            resource = rootResource;
        } else if ('/' == path.charAt(0)) {
            resource = rootResource + path.substring(1, path.length() - 1);
        } else {
            resource = rootResource + path;
        }

        // create new client and webresource from the given resource
        if (!clientCache.containsKey(path)) {
            final DefaultApacheHttpClientConfig clientConfig = new DefaultApacheHttpClientConfig();
            if (proxy.isEnabled()) {
                if ((proxy.getHost() != null) && (proxy.getPort() > 0)) {
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
            }

            clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, TIMEOUT);
            clientCache.put(path, ApacheHttpClient.create(clientConfig));
        }

        final Client c = clientCache.get(path);
        final UriBuilder uriBuilder = UriBuilder.fromPath(resource);

        // add all query params that are present
        if (queryParams != null) {
            for (final Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        final WebResource wr = c.resource(uriBuilder.build());

        // this is the binary interface so we accept the octet stream type only
        return wr.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>        DOCUMENT ME!
     * @param   path       DOCUMENT ME!
     * @param   queryData  DOCUMENT ME!
     * @param   type       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponsePOST(final String path, final Map queryData, final Class<T> type) throws IOException,
        ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path);

        return getResponsePOST(builder, type, queryData);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>        DOCUMENT ME!
     * @param   builder    DOCUMENT ME!
     * @param   type       DOCUMENT ME!
     * @param   queryData  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  IllegalStateException   DOCUMENT ME!
     */
    private <T> T getResponsePOST(final WebResource.Builder builder,
            final Class<T> type,
            final Map queryData) throws IOException, ClassNotFoundException {
        if ((builder == null) || (type == null)) {
            throw new IllegalStateException("neither builder nor type may be null"); // NOI18N
        }

        try {
            final byte[] bytes = builder.post(byte[].class, queryData);
//            System.out.println(bytes.length);
            if (isCompressionEnabled()) {
                return Converter.deserialiseFromGzip(bytes, type);
            } else {
                return Converter.deserialiseFromBase64(bytes, type);
            }
        } catch (final Exception ex) {
            if ((ex.getCause() != null) && (ex.getCause() instanceof IllegalThreadStateException)) {
                if (ex.getCause().getMessage().equals(MULTITHREADEDHTTPCONNECTION_IGNORE_EXCEPTION)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            "ignoring \""
                                    + MULTITHREADEDHTTPCONNECTION_IGNORE_EXCEPTION
                                    + "\" IllegalThreadStateException",
                            ex);
                    }
                } else {
                    LOG.warn("Error while querying request", ex);
                }
                return null;
            } else {
                throw ex;
            }
        }
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user, final String domainName) throws RemoteException {
        return getRoots(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   domainName  DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final String domainName, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domainName, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));
            return getResponsePOST("getRootsByDomain", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node[] getRoots(final User user) throws RemoteException {
        return getRoots(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getRoots", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        return getChildren(node, usr, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   usr      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getChildren(final Node node, final User usr, final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_NODE,
                        Converter.serialiseToString(node, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getChildren", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return addNode(node, parent, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   parent   DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node addNode(final Node node, final Link parent, final User user, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_NODE,
                        Converter.serialiseToString(node, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_LINK_PARENT, Converter.serialiseToString(parent, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("addNode", queryParams, Node.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return deleteNode(node, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_NODE,
                        Converter.serialiseToString(node, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("deleteNode", queryParams, boolean.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return addLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_NODE_FROM,
                        Converter.serialiseToString(from, isCompressionEnabled()))
                        .append(PARAM_NODE_TO, Converter.serialiseToString(to, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("addLink", queryParams, boolean.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return deleteLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_NODE_FROM,
                        Converter.serialiseToString(from, isCompressionEnabled()))
                        .append(PARAM_NODE_TO, Converter.serialiseToString(to, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("deleteLink", queryParams, boolean.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public String[] getDomains() throws RemoteException {
        return getDomains(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String[] getDomains(final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams =
                new AppendableMultivaluedMapImpl().append(
                    PARAM_CONNECTIONCONTEXT,
                    Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getDomains", queryParams, String[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param   exception  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UniformInterfaceException  DOCUMENT ME!
     */
    private RemoteException createRemoteException(final Exception exception) throws UniformInterfaceException {
        try {
            throw exception;
        } catch (final UniformInterfaceException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("exception during request, remapping", ex);
            }

            final ClientResponse response = ex.getResponse();
            if (response == null) {
                return new RemoteException("response is null", ex);
            } else {                    
                try {
                    return ServerExceptionMapper.fromResponse(
                        response,
                        RemoteException.class,
                        compressionEnabled);
                } catch (final Exception e) {
                    final String message = "exception during communication with server";
                    LOG.error(message, e);
                    return new RemoteException(message, ex);
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            return new RemoteException(message, ex);
        } catch (final ClassNotFoundException ex) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, ex);
            return new RemoteException(message, ex);
        } catch (final Exception ex) {
            final String message = "exception during communication with server";
            LOG.error(message, ex);
            return new RemoteException(message, ex);
        }
    }

    @Override
    @Deprecated
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        return getMetaObjectNode(usr, nodeID, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   nodeID   DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User usr,
            final int nodeID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_NODE_ID, Converter.serialiseToString(nodeID, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMetaObjectNodeByID", queryParams, Node.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        return getMetaObjectNode(usr, query, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   query    DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_QUERY, Converter.serialiseToString(query, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMetaObjectNodeByString", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                ConnectionContext.createDeprecated());
    }

    /**
     * Performs a Search for LightweightMetaObjects by Query.<br>
     * <strong>Note:</strong> This operation is delegated to the
     * {@link #customServerSearch(Sirius.server.newuser.User, de.cismet.cids.server.search.CidsServerSearch)} operation
     * and implemented by {@link LightweightMetaObjectsByQuerySearch}
     *
     * @param       classId               class id of LWMOs
     * @param       user                  user performing the request
     * @param       query                 query to search for LWMO. Has to select at lest the primary key (ID) of the
     *                                    Meta Object
     * @param       representationFields  must match fields in query
     * @param       context               DOCUMENT ME!
     *
     * @return      Array of LWMOs or empty array
     *
     * @throws      RemoteException  if any error occurs
     *
     * @deprecated  should be replaced by custom search
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        return this.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                null,
                context);
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                representationPattern,
                ConnectionContext.createDeprecated());
    }

    /**
     * Performs a Search for LightweightMetaObjects by Query.<br>
     * <strong>Note:</strong> This operation is delegated to the
     * {@link #customServerSearch(Sirius.server.newuser.User, de.cismet.cids.server.search.CidsServerSearch)} operation
     * and implemented by {@link LightweightMetaObjectsByQuerySearch}
     *
     * @param       classId                class id of LWMOs
     * @param       user                   user performing the request
     * @param       query                  query to search for LWMO. Has to select at lest the primary key (ID) of the
     *                                     Meta Object
     * @param       representationFields   must match fields in query
     * @param       representationPattern  string format pattern for toStrin Operation
     * @param       context                DOCUMENT ME!
     *
     * @return      Array of LWMOs or empty array
     *
     * @throws      RemoteException  if any error occurs
     *
     * @deprecated  should be replaced by custom search
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        LOG.warn("delegating getLightweightMetaObjectsByQuery for class + '"
                    + classId + "' with query '" + query + "' to legacy custom server search!");

        final LightweightMetaObjectsByQuerySearch lightweightMetaObjectsByQuerySearch =
            new LightweightMetaObjectsByQuerySearch();

        lightweightMetaObjectsByQuerySearch.setDomain(user.getDomain());
        lightweightMetaObjectsByQuerySearch.setClassId(classId);
        lightweightMetaObjectsByQuerySearch.setQuery(query);
        lightweightMetaObjectsByQuerySearch.setRepresentationFields(representationFields);
        lightweightMetaObjectsByQuerySearch.setRepresentationPattern(representationPattern);

        final Collection lwmoCollection = this.customServerSearch(user, lightweightMetaObjectsByQuerySearch, context);

        final LightweightMetaObject[] lightweightMetaObjects = (LightweightMetaObject[])lwmoCollection.toArray(
                new LightweightMetaObject[lwmoCollection.size()]);

        return lightweightMetaObjects;
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        return getMetaObject(usr, query, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr      DOCUMENT ME!
     * @param   query    DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject[] getMetaObject(final User usr, final String query, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_QUERY, Converter.serialiseToString(query, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMetaObjectByString", queryParams, MetaObject[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaObject[] getMetaObject(final User usr, final String query, final String domain) throws RemoteException {
        return getMetaObject(usr, query, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject[] getMetaObject(final User usr,
            final String query,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_QUERY, Converter.serialiseToString(query, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMetaObjectByStringAndDomain", queryParams, MetaObject[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr       DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     * @param   classID   DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        return getMetaObject(usr, objectID, classID, domain, ConnectionContext.createDeprecated());
    }

    @Override
    public MetaObject getMetaObject(final User usr,
            final int objectID,
            final int classID,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(usr, isCompressionEnabled()))
                        .append(PARAM_OBJECT_ID, Converter.serialiseToString(objectID, isCompressionEnabled()))
                        .append(PARAM_CLASS_ID, Converter.serialiseToString(classID, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMetaObjectByID", queryParams, MetaObject.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return insertMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject insertMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_METAOBJECT, Converter.serialiseToString(metaObject, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("insertMetaObject", queryParams, MetaObject.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return updateMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int updateMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_METAOBJECT, Converter.serialiseToString(metaObject, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("updateMetaObject", queryParams, int.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        return deleteMetaObject(user, metaObject, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   context     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int deleteMetaObject(final User user,
            final MetaObject metaObject,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_METAOBJECT, Converter.serialiseToString(metaObject, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("deleteMetaObject", queryParams, int.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   query   DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int update(final User user, final String query, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query, isCompressionEnabled()));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()));
            }

            return getResponsePOST("update", queryParams, int.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return getInstance(user, c, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   c        DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass c, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_METACLASS, Converter.serialiseToString(c, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getInstance", queryParams, MetaObject.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        return getClassByTableName(user, tableName, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     * @param   context    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClassByTableName(final User user,
            final String tableName,
            final String domain,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_TABLE_NAME, Converter.serialiseToString(tableName, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getClassByTableName", queryParams, MetaClass.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaClass getClass(final User user, final int classID, final String domain) throws RemoteException {
        return getClass(user, classID, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClass(final User user, final int classID, final String domain, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CLASS_ID, Converter.serialiseToString(classID, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getClassByID", queryParams, MetaClass.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        return getClasses(user, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getClasses(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getClasses", queryParams, MetaClass[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        return getClassTreeNodes(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getClassTreeNodesByUser", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        return getClassTreeNodes(user, domain, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String domain, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getClassTreeNodesByDomain", queryParams, Node[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user) throws RemoteException {
        return getMethods(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMethodsByUser", queryParams, MethodMap.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
        return getMethods(user, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(
                                PARAM_LOCAL_SERVER_NAME,
                                Converter.serialiseToString(localServerName, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getMethodsByDomain", queryParams, MethodMap.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                representationPattern,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     * @param   context                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_CLASS_ID,
                        Converter.serialiseToString(classId, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(
                                PARAM_REP_FIELDS,
                                Converter.serialiseToString(representationFields, isCompressionEnabled()))
                        .append(
                                PARAM_REP_PATTERN,
                                Converter.serialiseToString(representationPattern, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST(
                    "getAllLightweightMetaObjectsForClassByPattern", // NOI18N
                    queryParams,
                    LightweightMetaObject[].class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return getAllLightweightMetaObjectsForClass(
                classId,
                user,
                representationFields,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   context               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_CLASS_ID,
                        Converter.serialiseToString(classId, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(
                                PARAM_REP_FIELDS,
                                Converter.serialiseToString(representationFields, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST(
                    "getAllLightweightMetaObjectsForClass", // NOI18N
                    queryParams,
                    LightweightMetaObject[].class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Image[] getDefaultIcons(final String lsName) throws RemoteException {
        return getDefaultIcons(lsName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lsName             DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons(final String lsName, final ConnectionContext connectionContext)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams =
                new AppendableMultivaluedMapImpl().append(
                    PARAM_LS_NAME,
                    Converter.serialiseToString(lsName, isCompressionEnabled()));

            return getResponsePOST("getDefaultIconsByLSName", queryParams, Image[].class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Image[] getDefaultIcons() throws RemoteException {
        return getDefaultIcons(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons(final ConnectionContext connectionContext) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl();

            return getResponsePOST("getDefaultIcons", queryParams, Image[].class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        return changePassword(user, oldPassword, newPassword, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     * @param   context      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_OLD_PASSWORD, Converter.serialiseToString(oldPassword, isCompressionEnabled()))
                        .append(PARAM_NEW_PASSWORD, Converter.serialiseToString(newPassword, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("changePassword", queryParams, Boolean.class); // NOI18N
        } catch (final UniformInterfaceException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("exception during request, remapping", ex);
            }

            final ClientResponse response = ex.getResponse();
            if (response == null) {
                throw new RemoteException("response is null", ex);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.getStatus()) {   
                try {
                    throw ServerExceptionMapper.fromResponse(
                            response,
                            UserException.class,
                            compressionEnabled);
                } catch (final Exception e) {
                    final String message = "exception during communication with server";
                    LOG.error(message, e);
                    throw new RemoteException(message, e);
                }
            } else {
                throw createRemoteException(ex);
            }
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public User getUser(
            final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        return getUser(
                userGroupLsName,
                userGroupName,
                userLsName,
                userName,
                password,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public User getUser(
            final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password,
            final ConnectionContext context) throws RemoteException, UserException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USERGROUP_LS_NAME,
                        Converter.serialiseToString(userGroupLsName, isCompressionEnabled()))
                        .append(
                                PARAM_USERGROUP_NAME,
                                Converter.serialiseToString(userGroupName, isCompressionEnabled()))
                        .append(PARAM_USER_LS_NAME, Converter.serialiseToString(userLsName, isCompressionEnabled()))
                        .append(PARAM_USERNAME, Converter.serialiseToString(userName, isCompressionEnabled()))
                        .append(PARAM_PASSWORD, Converter.serialiseToString(password, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getUser", queryParams, User.class); // NOI18N
        } catch (final UniformInterfaceException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("exception during request, remapping", ex);
            }

            final ClientResponse response = ex.getResponse();
            if (response == null) {
                throw new RemoteException("response is null", ex);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.getStatus()) {
                try {
                    throw ServerExceptionMapper.fromResponse(
                        response,
                        UserException.class,
                        compressionEnabled);
                } catch (final Exception e) {
                    final String message = "exception during communication with server";
                    LOG.error(message, e);
                    throw new RemoteException(message, ex);
                }
            } else {
                throw createRemoteException(ex);
            }
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Vector getUserGroupNames() throws RemoteException {
        return getUserGroupNames(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams =
                new AppendableMultivaluedMapImpl().append(
                    PARAM_CONNECTIONCONTEXT,
                    Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getUserGroupNames", queryParams, Vector.class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        return getUserGroupNames(userName, lsHome, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     * @param   context   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USERNAME,
                        Converter.serialiseToString(userName, isCompressionEnabled()))
                        .append(PARAM_LS_HOME, Converter.serialiseToString(lsHome, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getUserGroupNamesByUser", queryParams, Vector.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        return getConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public String getConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_KEY, Converter.serialiseToString(key, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getConfigAttr", queryParams, String.class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        return hasConfigAttr(user, key, ConnectionContext.createDeprecated());
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_KEY, Converter.serialiseToString(key, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("hasConfigAttr", queryParams, boolean.class);
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        return customServerSearch(user, serverSearch, ConnectionContext.createDeprecated());
    }

    @Override
    public Collection customServerSearch(final User user,
            final CidsServerSearch serverSearch,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(
                                PARAM_CUSTOM_SERVER_SEARCH,
                                Converter.serialiseToString(serverSearch, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("customServerSearch", queryParams, Collection.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        return getHistory(classId, objectId, domain, user, elements, ConnectionContext.createDeprecated());
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements,
            final ConnectionContext context) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_CLASS_ID,
                        Converter.serialiseToString(classId, isCompressionEnabled()))
                        .append(PARAM_OBJECT_ID, Converter.serialiseToString(objectId, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(domain, isCompressionEnabled()))
                        .append(PARAM_USER, Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_ELEMENTS, Converter.serialiseToString(elements, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()));

            return getResponsePOST("getHistory", queryParams, HistoryObject[].class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    @Override
    @Deprecated
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return executeTask(user, taskname, taskdomain, body, ConnectionContext.createDeprecated(), params);
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ConnectionContext context,
            final ServerActionParameter... params) throws RemoteException {
        try {
            final AppendableMultivaluedMapImpl queryParams = new AppendableMultivaluedMapImpl().append(
                        PARAM_USER,
                        Converter.serialiseToString(user, isCompressionEnabled()))
                        .append(PARAM_TASKNAME, Converter.serialiseToString(taskname, isCompressionEnabled()))
                        .append(PARAM_DOMAIN, Converter.serialiseToString(taskdomain, isCompressionEnabled()))
                        .append(PARAM_CONNECTIONCONTEXT, Converter.serialiseToString(context, isCompressionEnabled()))
                        .append(PARAM_BODY, Converter.serialiseToString(body, isCompressionEnabled()))
                        .append(PARAM_PARAMELIPSE, Converter.serialiseToString(params, isCompressionEnabled()));

            return getResponsePOST("executeTask", queryParams, Object.class); // NOI18N
        } catch (final Exception ex) {
            throw createRemoteException(ex);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AppendableMultivaluedMapImpl extends MultivaluedMapImpl {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   key    DOCUMENT ME!
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public AppendableMultivaluedMapImpl append(final String key, final Object value) {
            if ((key != null) && (value != null)) {
                add(key, value);
            }
            return this;
        }
    }
}
