/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */
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
import Sirius.server.newuser.UserGroup;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;

import Sirius.util.image.Image;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import de.cismet.cids.dynamics.CidsBean;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.CidsNode;
import de.cismet.cids.server.api.types.GenericCollectionResource;
import de.cismet.cids.server.api.types.legacy.CidsClassFactory;
import de.cismet.cids.server.api.types.legacy.CidsNodeFactory;
import de.cismet.cids.server.api.types.legacy.ClassNameCache;
import de.cismet.cids.server.api.types.legacy.UserFactory;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.ws.SSLConfig;

import de.cismet.netutil.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

/**
 * This is the common CallServerService implementation for interacting with the
 * cids Pure REST API and for translating between cids REST JSON Entities and
 * cids server Java Types.
 *
 * @author Pascal Dihé (pascal.dihe@cismet.de))
 * @version 0.1 2015/04/17
 */
public class RESTfulInterfaceConnector implements CallServerService {

    public final static String USERS_API = "users";
    public final static String CLASSES_API = "classes";
    public final static String ENTITIES_API = "";
    public final static String NODES_API = "nodes";

    private static final transient Logger LOG = Logger.getLogger(RESTfulInterfaceConnector.class);

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;
    private final transient ClassNameCache classKeyCache;
    private static final int TIMEOUT = 10000;
    private final transient Proxy proxy;

    /**
     * for caching username/password combinations (needed for basic auth)
     */
    private final transient Map<String, String> credentialsCache;

    //~ Constructors -----------------------------------------------------------
    // <editor-fold desc="~ Constructors" defaultstate="collapsed">
    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param rootResource DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource) {
        this(rootResource, null, null);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param rootResource DOCUMENT ME!
     * @param proxy config proxyURL DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource, final Proxy proxy) {
        this(rootResource, proxy, null);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param rootResource DOCUMENT ME!
     * @param sslConfig DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource, final SSLConfig sslConfig) {
        this(rootResource, null, sslConfig);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param rootResource DOCUMENT ME!
     * @param proxy proxyConfig proxyURL DOCUMENT ME!
     * @param sslConfig DOCUMENT ME!
     *
     */
    public RESTfulInterfaceConnector(final String rootResource,
            final Proxy proxy,
            final SSLConfig sslConfig) {

        if (sslConfig == null) {
            LOG.warn("cannot initialise ssl because sslConfig is null"); // NOI18N
        } else {
            initSSL(sslConfig);
        }

        // add trailing '/' to the root resource if not present
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

        LOG.info("connecting to root resource '" + this.rootResource + "' using proxy: " + this.proxy); // NOI18N
        clientCache = new HashMap<String, Client>();
        credentialsCache = new HashMap<String, String>();
        classKeyCache = new ClassNameCache();
    }

    // </editor-fold>
    //~ Methods ----------------------------------------------------------------
    // <editor-fold desc="~ Helper Methods" defaultstate="collapsed">
    /**
     * DOCUMENT ME!
     *
     * @param sslConfig DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    protected final void initSSL(final SSLConfig sslConfig) {
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
            TrustManager[] trustManagerArray;

            if ((tmf != null) && (tmf.getTrustManagers() != null) && (tmf.getTrustManagers().length == 1)) {
                if (tmf.getTrustManagers()[0] instanceof X509TrustManager) {
                    cidsManager = (X509TrustManager) tmf.getTrustManagers()[0];
                }
            }

            try {
                trustManager = new CidsTrustManager(cidsManager);
                trustManagerArray = new TrustManager[]{trustManager};
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
            throw new IllegalStateException("system does not support SSL", e);            // NOI18N
        } catch (final KeyStoreException e) {
            throw new IllegalStateException("system does not support java keystores", e); // NOI18N
        } catch (final KeyManagementException e) {
            throw new IllegalStateException("ssl context init properly initialised", e);  // NOI18N
        } catch (final UnrecoverableKeyException e) {
            throw new IllegalStateException("cannot get key from keystore", e);           // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected String getRootResource() {
        return rootResource;
    }

    /**
     * Creates a {@link WebResource.Builder} from the given path and the given
     * params. The given path will be appended to the root path of this
     * connector, thus shall denote a path relative to the root resource. The
     * given {@link Map} of queryParams will be appended to the query.
     *
     * @param path the path relative to the root resource
     *
     * @return a <code>WebResource</code> ready to perform an operation (GET,
     * POST, PUT...)
     */
    protected WebResource createWebResource(final String path) {
        // remove leading '/' if present
        final String resource;
        if (path == null || path.isEmpty()) {
            resource = rootResource;
        } else if ('/' == path.charAt(0)) {
            resource = rootResource + path.substring(1, path.length() - 1);
        } else {
            resource = rootResource + path;
        }

        // create new client and webresource from the given resource
        if (!clientCache.containsKey(path)) {
            LOG.info("adding new client for path '" + path + "' and resource '" + resource + "' to cache");
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
            clientConfig.getClasses().add(JacksonJsonProvider.class);
            clientCache.put(path, ApacheHttpClient.create(clientConfig));
        }

        final Client client = clientCache.get(path);
        final UriBuilder uriBuilder = UriBuilder.fromPath(resource);

        final WebResource webResource = client.resource(uriBuilder.build());
        return webResource;
    }

    protected WebResource.Builder createAuthorisationHeader(final WebResource webResource, final User user) throws RemoteException {
        final String basicAuthString = this.getBasicAuthString(user);
        final WebResource.Builder builder = webResource.header("Authorization", basicAuthString);
        return builder;
    }

    protected WebResource.Builder createMediaTypeHeaders(final WebResource.Builder builder) {
        return builder.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    protected WebResource.Builder createMediaTypeHeaders(final WebResource webResource) {
        return webResource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Helper Method for adding 'domain' and 'role' query parameters to an
     * existing query parameters map from a legacy cids user object.
     *
     * @param queryParams existing query parameters that should be extended by
     * this method
     * @param user legacy cids user object
     * @return query parameters that have been extended by this method
     */
    protected MultivaluedMap createUserParameters(final MultivaluedMap queryParams, final User user) {
        if (user.getDomain() != null) {
            queryParams.add("domain", user.getDomain());
        }

        if (user.getUserGroup() != null) {
            queryParams.add("role", user.getUserGroup());
        }

        return queryParams;
    }

    /**
     * Helper Method for creating 'domain' and 'role' query parameters from
     * legacy cids user object.
     *
     * @param user legacy cids user object
     * @return query parameters 'domain' and 'role'
     */
    protected MultivaluedMap createUserParameters(final User user) {
        return this.createUserParameters(new MultivaluedMapImpl(), user);
    }

    /**
     * Tries to lookup a basic auth string for a previously authenticated user
     * FIXME: better work with session ids, etc.
     *
     * @param user
     * @return
     * @throws RemoteException
     */
    protected String getBasicAuthString(final User user) throws RemoteException {

        final String message;
        if (user != null && user.getName() != null && user.getDomain() != null) {
            final String key = user.getName() + "@" + user.getDomain();
            final String basicAuthString = this.credentialsCache.get(key);
            if (basicAuthString != null) {
                return basicAuthString;
            } else {
                message = "User '" + user.getName()
                        + "' is not authenticated at domain '" + user.getDomain() + "', try to login first.";
            }
        } else {
            message = "cannot lookup crendentials for user, either username, password or domain is null: " + user;
        }

        LOG.error(message);
        throw new RemoteException(message);
    }

    /**
     * Stores the BasicAuthString for a specific user in the credentials map
     *
     * @param user legacy cids user
     * @param password password of the user
     * @throws java.rmi.RemoteException if the login fails
     */
    protected void putBasicAuthString(final User user, final String password) throws RemoteException {

        if (user != null && user.getName() != null && user.getDomain() != null && password != null) {
            final String key = user.getName() + "@" + user.getDomain();
            final String basicAuthString = "Basic "
                    + new String(Base64.encode(key + ":" + password));

            if (this.credentialsCache.containsKey(key)) {
                LOG.warn("user '" + user.getName() + "' is already logged-in at domain '" + user.getDomain() + ", overwriting credentials");
            }
            this.credentialsCache.put(key, basicAuthString);
        } else {
            final String message = "cannot login user, either, username, password or domain is null: " + user;
            LOG.error(message);
            throw new RemoteException(message);
        }
    }

    /**
     * Removes the BasicAuthString for a specific user in the credentials map,
     * e.g. when the login failed.
     *
     * @param user user object o remove from the credentials map
     */
    protected void removeBasicAuthString(final User user) {
        final String key = user.getName() + "@" + user.getDomain();
        if (!this.credentialsCache.containsKey(key)) {
            LOG.warn("user '" + user.getName() + "' is not authenticated at '" + user.getDomain() + ", cannot remove");
        } else {
            this.credentialsCache.remove(key);
        }
    }
    
    private String getClassNameForClassId(final User user, final String domain, final int classId) throws RemoteException {
        final String className;
        
        if (!this.classKeyCache.isDomainCached(domain)) {
            LOG.info("class key cache does not contain class ids for domain '" + domain
                    + "', need to fill the cache first!");
            this.getClasses(user, domain);
        }

        className = this.classKeyCache.getClassNameForClassId(domain, classId);
        if (className == null) {
            final String message = "could not find class with id '" + classId
                    + "' at domain '" + domain + "' for user '"
                    + user.getName() + "', class key map does not contain id.";
            LOG.error(message);
            //return null;
            throw new RemoteException(message);
        }
        
        return className;
    }
    
    

    // </editor-fold>
    
    // <editor-fold desc="NODES API" defaultstate="collapsed">
    /**
     * Gets legacy root nodes of a specific domain.
     * <br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>
     * <a href="http://localhost:8890/nodes?domain=SWITCHON&limit=100&offset=0&role=all">
     * http://localhost:8890/nodes?domain=SWITCHON&limit=100&offset=0&role=all
     * </a>
     * </code>
     *
     * @param user legacy user needed for authentication
     * @param domainName
     * @return array of legacy root nodes
     * @throws RemoteException if any server error occurs
     */
    @Override
    public Node[] getRoots(final User user, final String domainName) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    /**
     * Gets all legacy root nodes of all domains supported by the server.
     * <br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>
     * <a href="http://localhost:8890/nodes?limit=100&offset=0&role=all">
     * http://localhost:8890/nodes?limit=100&offset=0&role=all
     * </a>
     * </code>
     *
     * @param user legacy user needed for authentication
     * @return array of legacy root nodes
     * @throws RemoteException if any server error occurs
     */
    @Override
    public Node[] getRoots(final User user) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(NODES_API).queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        LOG.debug("getRoots for user '" + user + "': " + webResource.toString());

        try {
            final GenericCollectionResource<CidsNode> restCidsNodes
                    = builder.get(new GenericType<GenericCollectionResource<CidsNode>>() {
                    });

            if (restCidsNodes != null && restCidsNodes.get$collection() != null
                    && restCidsNodes.get$collection().size() > 0) {

                LOG.debug("found " + restCidsNodes.get$collection().size()
                        + " root nodes for user '" + user.getName()
                        + "'. performing conversion to cids legacy nodes.");

                final Node[] legacyNodes = new Node[restCidsNodes.get$collection().size()];
                int i = 0;
                for (CidsNode cidsNode : restCidsNodes.get$collection()) {
                    try {
                        final Node legacyNode
                                = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(cidsNode);
                        legacyNodes[i] = legacyNode;
                        i++;

                    } catch (Exception ex) {
                        final String message = "could not perform conversion from cids rest node '"
                                + cidsNode.getKey() + "' to cids legacy node: " + ex.getMessage();
                        LOG.error(ex);
                        throw new RemoteException(message, ex);
                    }
                }

                return legacyNodes;

            } else {
                LOG.error("could not find any cids nodes for user '" + user.getName() + "'");
                return null;
            }

        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could get cids nodes for user '" + user.getName() + "': "
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    /**
     * Gets the children of the specified node for the specified user. The
     * specified node has to contain either a valid node id or a dynamic
     * children statement. Depending on whether the node contains an id or a
     * dynamic children statement, the call is performed against the
     * "getChildrenById" or "getChildrenByQuery" REST Node API.
     * <br>
     * <br>
     * <strong>Example REST Call (without dynamic children):</strong><br>
     * <code>
     * <a href="http://localhost:8890/nodes/SWITCHON.7/children?limit=100&offset=0&role=all">
     * http://localhost:8890/nodes/SWITCHON.7/children?limit=100&offset=0&role=all
     * </a>
     * </code>
     * <br>
     * <br>
     * <strong>Example REST Call (with dynamic children):</strong><br>
     * <code>
     * curl --user username@SWITCHON:password -H "Content-Type: application/json" -X POST -d "SELECT -1 AS id,  taggroup.name,  cs_class.id AS class_id,  NULL AS object_id,  'N' AS node_type,  NULL AS url,  csdc.tags(taggroup.id) AS dynamic_children,  FALSE AS sql_sort FROM taggroup,  cs_class WHERE cs_class.name = 'taggroup' ORDER BY taggroup.name;" http://localhost:8890/nodes/SWITCHON/children?limit=100&offset=0&role=all
     * </code>
     *
     * @param node the legacy node that contains either a valid node id or a
     * dynamic children statements
     * @param user
     * @return child nodes as legacy node array
     * @throws RemoteException if any remote error occurs
     */
    @Override
    public Node[] getChildren(final Node node, final User user) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final GenericCollectionResource<CidsNode> restCidsNodes;
        try {
            if (node.getId() == -1) {
                if (node.getDynamicChildrenStatement() == null || node.getDynamicChildrenStatement().isEmpty()) {
                    final String message = "node '" + node.getName() + "' with id '" + node.getId()
                            + "' has no dynamic children statement, returning null!";
                    LOG.error(message);
                    return new Node[]{};
                }
                LOG.debug("node '" + node.getName() + "' with id '" + node.getId()
                        + "' has a dynamic children statement");

                final WebResource webResource = this.createWebResource(NODES_API)
                        .path(user.getDomain() + "/children")
                        .queryParams(queryParameters);
                WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
                builder = this.createMediaTypeHeaders(builder);
                LOG.debug("getDynamicChildren of node '" + node.getName()
                        + "' (" + node.getId() + ") for user '" + user + "': " + webResource.toString());
                restCidsNodes = builder.post(new GenericType<GenericCollectionResource<CidsNode>>() {
                }, node.getDynamicChildrenStatement());
            } else {
                final WebResource webResource = this.createWebResource(NODES_API)
                        .path(user.getDomain() + "." + String.valueOf(node.getId()) + "/children")
                        .queryParams(queryParameters);
                WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
                builder = this.createMediaTypeHeaders(builder);
                LOG.debug("getChildren of node '" + node.getName()
                        + "' (" + node.getId() + ") for user '" + user + "': " + webResource.toString());
                restCidsNodes = builder.get(new GenericType<GenericCollectionResource<CidsNode>>() {
                }); 
            }

            if (restCidsNodes != null && restCidsNodes.get$collection() != null
                    && restCidsNodes.get$collection().size() > 0) {

                LOG.debug("found " + restCidsNodes.get$collection().size()
                        + " child nodes of node '" + node.getName()
                        + "' (" + node.getId() + ") for user '" + user.getName()
                        + "'. performing conversion to cids legacy nodes.");

                final Node[] legacyNodes = new Node[restCidsNodes.get$collection().size()];
                int i = 0;
                for (CidsNode cidsNode : restCidsNodes.get$collection()) {
                    try {
                        final Node legacyNode
                                = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(cidsNode);
                        legacyNodes[i] = legacyNode;
                        i++;

                    } catch (Exception ex) {
                        final String message = "could not perform conversion from cids rest node '"
                                + cidsNode.getKey() + "' to cids legacy node: " + ex.getMessage();
                        LOG.error(ex);
                        throw new RemoteException(message, ex);
                    }
                }

                return legacyNodes;

            } else {
                LOG.error("could not find any child nodes of node '" + node.getName()
                        + "' (" + node.getId() + ") for user '" + user.getName() + "'");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could retrieve children of node '" + node.getName()
                    + "' (" + node.getId() + ") for user '" + user.getName()
                    + "' at domain '" + user.getDomain() + "'"
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    @Override
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        // TODO: Implement method in Nodes API or remove
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is not yet supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: Implement method in Nodes API or remove
     *
     * @param node TODO
     * @param user TODO
     * @return TODO
     * @throws RemoteException TODO
     */
    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        // TODO: Implement method in Nodes API or remove
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is not yet supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: Implement method in Nodes API or remove
     *
     * @param from TODO
     * @param to TODO
     * @param user TODO
     * @return TODO
     * @throws RemoteException TODO
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        // TODO: Implement method in Nodes API or remove
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is not yet supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: Implement method in Nodes API or remove
     *
     * @param from TODO
     * @param to TODO
     * @param user TODO
     * @return TODO
     * @throws RemoteException TODO
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        // TODO: Implement method in Nodes API or remove
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is not yet supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Gets a legacy node by its id. The method is not restricted to nodes of
     * type of MetaObjectNode!
     *
     * <br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>
     * <a href="http://localhost:8890/nodes/SWITCHON.7">
     * http://localhost:8890/nodes/SWITCHON.7
     * </a>
     * </code>
     *
     * @param user legacy user
     * @param nodeID id of the legacy node
     * @param domain domain name
     * @return legacy node object or null
     * @throws RemoteException
     */
    @Override
    public Node getMetaObjectNode(final User user, final int nodeID, final String domain) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(NODES_API)
                .path(user.getDomain() + "." + String.valueOf(nodeID))
                .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        LOG.debug("getMetaObjectNode by id '" + nodeID
                + "' for user '" + user + "' and domain '"
                + user.getDomain() + "': " + webResource.toString());

        try {
            final CidsNode restCidsNode = builder.get(CidsNode.class);

            if (restCidsNode != null) {

                LOG.debug("found node '" + restCidsNode.getName()
                        + "' (" + restCidsNode.getId() + ") for user '" + user.getName()
                        + "'. performing conversion to cids legacy nodes.");
                try {
                    final Node legacyNode
                            = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(restCidsNode);
                    return legacyNode;
                } catch (Exception ex) {
                    final String message = "could not perform conversion from cids rest node '"
                            + restCidsNode.getKey() + "' to cids legacy node: " + ex.getMessage();
                    LOG.error(ex);
                    throw new RemoteException(message, ex);
                }
            } else {
                LOG.error("could not find node with id '" + nodeID
                        + "' for user '" + user.getName()
                        + "' at domain '" + user.getDomain() + "'");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could retrieve node with id '" + nodeID
                    + "' for user '" + user.getName()
                    + "' at domain '" + user.getDomain() + "'"
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it should not be called and throws an UnsupportedOperationException 
     * when invoked by the client!</p>
     *
     * @param usr UnsupportedOperation
     * @param query UnsupportedOperation
     * @return UnsupportedOperation
     * @throws RemoteException UnsupportedOperation
     * @deprecated UnsupportedOperation
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it should not be called and throws an UnsupportedOperationException 
     * when invoked by the client!</p>
     *
     * @param user UnsupportedOperation
     * @param query UnsupportedOperation
     * @return UnsupportedOperation
     * @throws RemoteException UnsupportedOperation
     * @deprecated UnsupportedOperation
     */
    @Override
    public Node[] getMetaObjectNode(final User user, final Query query) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by Nodes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it returns an empty result!</p>
     *
     * @param user parameter is ignored
     * @return empty node array
     * @throws RemoteException is never thrown
     * @deprecated UnsupportedOperation
     */
    @Override
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by Nodes REST API!";
        LOG.warn(message);
        //throw new UnsupportedOperationException(message);
        return new Node[]{};
    }

    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it returns an empty result!</p>
     *
     * @param user parameter is ignored
     * @param domain parameter is ignored
     * @return empty node array
     * @throws RemoteException is never thrown
     * @deprecated UnsupportedOperation
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by Nodes REST API!";
        LOG.warn(message);
        //throw new UnsupportedOperationException(message);
        return new Node[]{};
    }

    //</editor-fold>
    // <editor-fold desc="ENTITIES API" defaultstate="collapsed">
    @Override
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
        
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final String query, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaObject getMetaObject(final User user, final int objectID, final int classId, final String domain)
            throws RemoteException {
        
        final String className = this.getClassNameForClassId(user, domain, classId);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(ENTITIES_API)
                .path(domain + "." + className + "/" + objectID)
                .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);

        LOG.debug("getMetaObject '" + objectID + "@" + classId + "@" + domain 
                + "' for user '" + user + "' :" + webResource.toString());
        
        try {
            final CidsBean cidsBean = builder.get(CidsBean.class);
            if (cidsBean != null) {
                    final MetaObject metaObject =  cidsBean.getMetaObject();
                    return metaObject;
            } else {
                LOG.error("could not find meta object  '" + objectID + "@" + classId + "@" + domain 
                + "' for user '" + user + "'");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could get meta object '" + objectID + "@" + classId + "@" + domain 
                + "' for user '" + user + "': "
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    @Override
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int insertMetaObject(final User user, final Query query, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int update(final User user, final String query, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        return c.getEmptyInstance();
    }

    // </editor-fold>
    // <editor-fold desc="ENTITY INFO (CLASSES) API" defaultstate="collapsed">
    /**
     * Gets a meta class by its legacy class id (int) for the user form the
     * specified domain. The connector performs fist a lookup for legacy meta
     * class ids (int) against REST meta class ids (strings) in a local class
     * key cache and then delegates the call to
     * {@link #getClassByTableName(Sirius.server.newuser.User, java.lang.String, java.lang.String)}.
     * <br>
     * <br>
     * <strong>Example REST Call (delegated):</strong><br>
     * <code>
     * <a href="http://localhost:8890/classes/SWITCHON.tag">
     * http://localhost:8890/classes/SWITCHON.tag
     * </a>
     * </code>
     *
     * @param user user performing the request
     * @param classId legacy id of the class
     * @param domain domain of the class
     * @return
     * @throws RemoteException
     */
    @Override
    public MetaClass getClass(final User user, final int classId, final String domain) throws RemoteException {
        LOG.debug("getClass '" + classId + "@" + domain + "' for user '" + user + "'");

        if (!this.classKeyCache.isDomainCached(domain)) {
            LOG.info("class key cache does not contain class ids for domain '" + domain
                    + "', need to fill teche first!");
            this.getClasses(user, domain);
        }

        final String className = this.classKeyCache.getClassNameForClassId(domain, classId);
        if (className == null) {
            final String message = "could not find class with id '" + classId
                    + "' at domain '" + domain + "' for user '"
                    + user.getName() + "', class key map does not contain id.";
            LOG.error(message);
            //return null;
            throw new RemoteException(message);
        }

        return this.getClassByTableName(user, className, domain);
    }

    /**
     * <p>
     * Gets a meta class by its name for the user form the specified domain. The
     * REST meta class id is constructed as <code>tableName.domain</code></p>
     * <strong>Example REST Call:</strong><br>
     * <code>
     * <a href="http://localhost:8890/classes/SWITCHON.tag">
     * http://localhost:8890/classes/SWITCHON.tag
     * </a>
     * </code>
     *
     * @param user user performing the request
     * @param tableName name of the class
     * @param domain domain of the class
     * @return
     * @throws RemoteException
     */
    @Override
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(CLASSES_API)
                .path(domain + "." + tableName)
                .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);

        LOG.debug("getClassByTableName '" + tableName + "@" + domain + "' for user '" + user + "': "
                + webResource.toString());

        try {

            final CidsClass cidsClass = builder.get(CidsClass.class);
            if (cidsClass != null) {
                try {
                    final MetaClass metaClass
                            = CidsClassFactory.getFactory().legacyCidsClassFromRestCidsClass(cidsClass);
                    return metaClass;
                } catch (Exception ex) {
                    final String message = "could not perform conversion from cids rest class '"
                            + cidsClass.getKey() + "' to cids legacy class: " + ex.getMessage();
                    LOG.error(ex);
                    throw new RemoteException(message, ex);
                }
            } else {
                LOG.error("could not find cids class '" + tableName + "' at domain '"
                        + domain + "' for user '" + user.getName() + "'");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could get cids class '" + tableName + "' at domain '"
                    + domain + "' for user '" + user.getName() + "': "
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    /**
     * <p>
     * Gets all meta classes for the user from the specified the domain.</p>
     * <strong>Example REST Call:</strong><br>
     * <code>
     * <a href="http://localhost:8890/classes?domain=SWITCHON">
     * http://localhost:8890/classes?domain=SWITCHON
     * </a>
     * </code>
     *
     * @param user legacy cids user performing the request
     * @param domain domain (~localserver)
     * @return Array with all meta classes
     * @throws RemoteException if any server error occurs
     */
    @Override
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(CLASSES_API).queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        LOG.debug("getClasses from domain '" + domain + "' for user '" + user + "': "
                + webResource.toString());

        try {
            final GenericCollectionResource<CidsClass> restCidsClasses
                    = builder.get(new GenericType<GenericCollectionResource<CidsClass>>() {
                    });

            if (restCidsClasses != null && restCidsClasses.get$collection() != null
                    && restCidsClasses.get$collection().size() > 0) {

                LOG.debug("found " + restCidsClasses.get$collection().size()
                        + " cids classes at domain '" + domain
                        + "' for user '" + user.getName() + "'. performing conversion to cids legacy class.");

                final MetaClass[] metaClasses = new MetaClass[restCidsClasses.get$collection().size()];
                int i = 0;
                for (CidsClass cidsClass : restCidsClasses.get$collection()) {
                    try {
                        final MetaClass metaClass
                                = CidsClassFactory.getFactory().legacyCidsClassFromRestCidsClass(cidsClass);
                        metaClasses[i] = metaClass;
                        i++;
                    } catch (Exception ex) {
                        final String message = "could not perform conversion from cids rest class '"
                                + cidsClass.getKey() + "' to cids legacy class: " + ex.getMessage();
                        LOG.error(ex);
                        throw new RemoteException(message, ex);
                    }
                }

                this.classKeyCache.fillCache(domain, metaClasses);
                return metaClasses;
            } else {
                LOG.error("could not find any cids classes at domain '"
                        + domain + "' for user '" + user.getName() + "'");
                return null;
            }

        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could get cids classes at domain '"
                    + domain + "' for user '" + user.getName() + "': "
                    + status.getReasonPhrase();

            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    // </editor-fold>
    // <editor-fold desc="METHODS (DEPRECATED)" defaultstate="collapsed">
    /**
     * <strong>UnsupportedOperation</strong>
     *
     * @param user UnsupportedOperation
     * @return UnsupportedOperation
     * @throws RemoteException UnsupportedOperation
     * @deprecated UnsupportedOperation
     */
    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>UnsupportedOperation</strong>
     *
     * @param user UnsupportedOperation
     * @param localServerName UnsupportedOperation
     * @return UnsupportedOperation
     * @throws RemoteException UnsupportedOperation
     */
    @Override
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    // </editor-fold>
    // <editor-fold desc="QUERIES (DEPRECATED?)" defaultstate="collapsed">
    @Override
    public boolean storeQuery(final User user, final QueryData data) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Info[] getQueryInfos(final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public QueryData getQuery(final int id, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean delete(final int id, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int addQuery(final User user,
            final String name,
            final String description,
            final String statement,
            final int resultType,
            final char isUpdate,
            final char isBatch,
            final char isRoot,
            final char isUnion) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public int addQuery(final User user, final String name, final String description, final String statement)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final String paramkey,
            final String description) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
    // <editor-fold desc="SEARCH API" defaultstate="collapsed">
    
    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it returns an empty result!</p>
     * 
     * @param user parameter is ignored
     * @return <strong>empty</strong> HashMap;
     * @throws RemoteException never thrown
     * @deprecated
     */
    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by cids REST API! -> returns empty result";
        LOG.warn(message);
        //throw new UnsupportedOperationException(message);
        return new HashMap(0);
    }

    /**
     * <strong>Unsupported Operation</strong>
     * <p>This operation is not supported anymore in the cids REST API,
     * it returns an empty result!</p>
     * 
     * @param user parameter is ignored
     * @param domain parameter is ignored
     * @return <strong>empty</strong> HashMap;
     * @throws RemoteException never thrown
     * @deprecated
     */
    @Override
    public HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is deprecated and not supported by cids REST API! -> returns empty result";
        LOG.warn(message);
        //throw new UnsupportedOperationException(message);
        return new HashMap(0);
    }

    @Override
    public SearchResult search(final User user, final String[] classIds, final SearchOption[] searchOptions)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
    // <editor-fold desc="INFRASTRUCTURE API (NEW)" defaultstate="collapsed">
    /**
     * TODO: To be implemented in cids REST API
     *
     * @return list with domain names
     * @throws RemoteException if any remote error occurs
     */
    @Override
    public String[] getDomains() throws RemoteException {
        // TODO: Implement method in Nodes API or remove
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                + "' is not yet supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    @Override
    public Image[] getDefaultIcons(final String lsName) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Image[] getDefaultIcons() throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
    // <editor-fold desc="USERS API" defaultstate="collapsed">
    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {

        LOG.debug("performing validation of user '" + userName + "' af domain '" + userLsName + "'");
        LOG.warn("userGroupLsName '" + userGroupLsName + "' and userGroupName '" + userGroupName + "' are ignored since they are not supported by RESTful service interface (yet).");

        final User cidsUser = new User(-1, userName, userLsName);

        this.putBasicAuthString(cidsUser, password);

        final WebResource webResource = this.createWebResource(USERS_API);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, cidsUser);
        builder = this.createMediaTypeHeaders(builder);

        try {
            final de.cismet.cids.server.api.types.User restUser = builder.get(de.cismet.cids.server.api.types.User.class);
            return UserFactory.getFactory().cidsUserFromRestUser(restUser);

        } catch (UniformInterfaceException ue) {

            this.removeBasicAuthString(cidsUser);

            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "login of user '" + userName + "' at domain '" + userLsName
                    + "' failed: " + status.toString();
            LOG.error(message, ue);

            if (status.getStatusCode() < 500) {
                throw new UserException(message, true, true, false, false);
            } else {
                throw new RemoteException(message, ue);
            }
        }
    }

    @Override
    public Vector getUserGroupNames() throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.

//        final MultivaluedMap queryParams = new MultivaluedMapImpl();
//            queryParams.add("domain", domain);
//            queryParams.add("role", role);
    }

    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
    // <editor-fold desc="CONFIGATTRIBUTES API" defaultstate="collapsed">
    @Override
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
    // <editor-fold desc="ACTIONS API" defaultstate="collapsed">
    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // </editor-fold>
}
