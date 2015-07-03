/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.middleware.types.StringPatternFormater;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;

import Sirius.util.image.Image;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.sun.jersey.multipart.FormDataMultiPart;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.rmi.RemoteException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import de.cismet.cids.base.types.Type;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.api.types.ActionTask;
import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.CidsNode;
import de.cismet.cids.server.api.types.GenericCollectionResource;
import de.cismet.cids.server.api.types.GenericResourceWithContentType;
import de.cismet.cids.server.api.types.SearchInfo;
import de.cismet.cids.server.api.types.SearchParameters;
import de.cismet.cids.server.api.types.legacy.CidsBeanFactory;
import de.cismet.cids.server.api.types.legacy.CidsClassFactory;
import de.cismet.cids.server.api.types.legacy.CidsNodeFactory;
import de.cismet.cids.server.api.types.legacy.ClassNameCache;
import de.cismet.cids.server.api.types.legacy.ServerSearchFactory;
import de.cismet.cids.server.api.types.legacy.UserFactory;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.RestApiCidsServerSearch;
import de.cismet.cids.server.search.builtin.legacy.LightweightMetaObjectsByQuerySearch;
import de.cismet.cids.server.search.builtin.legacy.MetaObjectNodesByQuerySearch;
import de.cismet.cids.server.search.builtin.legacy.MetaObjectsByQuerySearch;
import de.cismet.cids.server.ws.SSLConfig;

import de.cismet.netutil.Proxy;

/**
 * This is the common CallServerService implementation for interacting with the cids Pure REST API and for translating
 * between cids REST JSON Entities and cids server Java Types.
 *
 * @author   Pascal Dihé (pascal.dihe@cismet.de))
 * @version  0.1 2015/04/17
 */
public class RESTfulInterfaceConnector implements CallServerService {

    //~ Static fields/initializers ---------------------------------------------

    public static final String USERS_API = "users";
    public static final String CLASSES_API = "classes";
    public static final String ENTITIES_API = "";
    public static final String NODES_API = "nodes";
    public static final String SEARCH_API = "searches";
    public static final String SEARCH_API_RESULTS = "/results";
    public static final String ACTIONS_API = "actions";
    public static final String ACTIONS_API_TASKS = "/tasks";

    private static final transient Logger LOG = Logger.getLogger(RESTfulInterfaceConnector.class);
    private static final int TIMEOUT = 10000;
    private static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    // <editor-fold desc="~ Constructors" defaultstate="collapsed">
    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource) {
        this(rootResource, null, null);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         config proxyURL DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource, final Proxy proxy) {
        this(rootResource, proxy, null);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
     */
    public RESTfulInterfaceConnector(final String rootResource, final SSLConfig sslConfig) {
        this(rootResource, null, sslConfig);
    }

    /**
     * Creates a new RESTfulInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         proxyConfig proxyURL DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
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

    // <editor-fold desc="~ Helper Methods" defaultstate="collapsed">
    /**
     * DOCUMENT ME!
     *
     * @param   sslConfig  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
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
     * @return  DOCUMENT ME!
     */
    protected String getRootResource() {
        return rootResource;
    }

    /**
     * Creates a {@link WebResource.Builder} from the given path and the given params. The given path will be appended
     * to the root path of this connector, thus shall denote a path relative to the root resource. The given {@link Map}
     * of queryParams will be appended to the query.
     *
     * @param   path  the path relative to the root resource
     *
     * @return  a <code>WebResource</code> ready to perform an operation (GET, POST, PUT...)
     */
    protected WebResource createWebResource(final String path) {
        // remove leading '/' if present
        final String resource;
        if ((path == null) || path.isEmpty()) {
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

    /**
     * DOCUMENT ME!
     *
     * @param   webResource  DOCUMENT ME!
     * @param   user         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    protected WebResource.Builder createAuthorisationHeader(final WebResource webResource, final User user)
            throws RemoteException {
        final String basicAuthString = this.getBasicAuthString(user);
        final WebResource.Builder builder = webResource.header("Authorization", basicAuthString);
        return builder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   builder  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected WebResource.Builder createMediaTypeHeaders(final WebResource.Builder builder) {
        return builder.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   webResource  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected WebResource.Builder createMediaTypeHeaders(final WebResource webResource) {
        return webResource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Helper Method for adding 'domain' and 'role' query parameters to an existing query parameters map from a legacy
     * cids user object.
     *
     * @param   queryParams  existing query parameters that should be extended by this method
     * @param   user         legacy cids user object
     *
     * @return  query parameters that have been extended by this method
     */
    protected MultivaluedMap createUserParameters(final MultivaluedMap queryParams, final User user) {
        // if (user.getDomain() != null) {
        // queryParams.add("domain", user.getDomain());
        // }

        if (user.getUserGroup() != null) {
            queryParams.add("role", user.getUserGroup().getName());
        }

        return queryParams;
    }

    /**
     * Helper Method for creating 'domain' and 'role' query parameters from legacy cids user object.
     *
     * @param   user  legacy cids user object
     *
     * @return  query parameters 'domain' and 'role'
     */
    protected MultivaluedMap createUserParameters(final User user) {
        return this.createUserParameters(new MultivaluedMapImpl(), user);
    }

    /**
     * Tries to lookup a basic auth string for a previously authenticated user FIXME: better work with session ids, etc.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    protected String getBasicAuthString(final User user) throws RemoteException {
        final String message;
        if ((user != null) && (user.getName() != null) && (user.getDomain() != null)) {
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
     * Stores the BasicAuthString for a specific user in the credentials map.
     *
     * @param   user      legacy cids user
     * @param   password  password of the user
     *
     * @throws  RemoteException  if the login fails
     */
    protected void putBasicAuthString(final User user, final String password) throws RemoteException {
        if ((user != null) && (user.getName() != null) && (user.getDomain() != null) && (password != null)) {
            final String key = user.getName() + "@" + user.getDomain();
            final String basicAuthString = "Basic "
                        + new String(Base64.encode(key + ":" + password));

            if (this.credentialsCache.containsKey(key)) {
                LOG.warn("user '" + user.getName() + "' is already logged-in at domain '" + user.getDomain()
                            + ", overwriting credentials");
            }
            this.credentialsCache.put(key, basicAuthString);
        } else {
            final String message = "cannot login user, either, username, password or domain is null: " + user;
            LOG.error(message);
            throw new RemoteException(message);
        }
    }

    /**
     * Removes the BasicAuthString for a specific user in the credentials map, e.g. when the login failed.
     *
     * @param  user  user object o remove from the credentials map
     */
    protected void removeBasicAuthString(final User user) {
        final String key = user.getName() + "@" + user.getDomain();
        if (!this.credentialsCache.containsKey(key)) {
            LOG.warn("user '" + user.getName() + "' is not authenticated at '" + user.getDomain() + ", cannot remove");
        } else {
            this.credentialsCache.remove(key);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    private String getClassNameForClassId(final User user, final String domain, final int classId)
            throws RemoteException {
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
            // return null;
            throw new RemoteException(message);
        }

        return className;
    }

    // </editor-fold>
    // <editor-fold desc="NODES API" defaultstate="collapsed">
    /**
     * Gets legacy root nodes of a specific domain.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/nodes?domain=SWITCHON&limit=100&offset=0&role=all">
     * http://localhost:8890/nodes?domain=SWITCHON&limit=100&offset=0&role=all</a></code>
     *
     * @param   user        legacy user needed for authentication
     * @param   domainName  DOCUMENT ME!
     *
     * @return  array of legacy root nodes
     *
     * @throws  RemoteException  if any server error occurs
     */
    @Override
    public Node[] getRoots(final User user, final String domainName) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("domain", domainName);
        final WebResource webResource = this.createWebResource(NODES_API).queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoots for user '" + user + "': " + webResource.toString());
        }

        try {
            final GenericCollectionResource<CidsNode> restCidsNodes = builder.get(
                    new GenericType<GenericCollectionResource<CidsNode>>() {
                    });

            if ((restCidsNodes != null) && (restCidsNodes.get$collection() != null)
                        && (restCidsNodes.get$collection().size() > 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("found " + restCidsNodes.get$collection().size()
                                + " root nodes for user '" + user.getName()
                                + "'. performing conversion to cids legacy nodes.");
                }

                final Node[] legacyNodes = new Node[restCidsNodes.get$collection().size()];
                int i = 0;
                for (final CidsNode cidsNode : restCidsNodes.get$collection()) {
                    try {
                        final Node legacyNode = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(cidsNode);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * Gets all legacy root nodes of all domains supported by the server.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/nodes?domain=switchon&role=all">
     * http://localhost:8890/nodes?domain=switchon&role=all</a></code>
     *
     * @param   user  legacy user needed for authentication
     *
     * @return  array of legacy root nodes
     *
     * @throws  RemoteException  if any server error occurs
     */
    @Override
    public Node[] getRoots(final User user) throws RemoteException {
        return this.getRoots(user, user.getDomain());
    }

    /**
     * Gets the children of the specified node for the specified user. The specified node has to contain either a valid
     * node id or a dynamic children statement. Depending on whether the node contains an id or a dynamic children
     * statement, the call is performed against the "getChildrenById" or "getChildrenByQuery" REST Node API.<br>
     * <br>
     * <strong>Example REST Call (without dynamic children):</strong><br>
     * <code><a href="http://localhost:8890/nodes/SWITCHON.7/children?limit=100&offset=0&role=all">
     * http://localhost:8890/nodes/SWITCHON.7/children?limit=100&offset=0&role=all</a></code><br>
     * <br>
     * <strong>Example REST Call (with dynamic children):</strong><br>
     * <code>curl --user username@SWITCHON:password -H "Content-Type: application/json" -X POST -d "SELECT -1 AS id,
     * taggroup.name, cs_class.id AS class_id, NULL AS object_id, 'N' AS node_type, NULL AS url, csdc.tags(taggroup.id)
     * AS dynamic_children, FALSE AS sql_sort FROM taggroup, cs_class WHERE cs_class.name = 'taggroup' ORDER BY
     * taggroup.name;" http://localhost:8890/nodes/SWITCHON/children?limit=100&offset=0&role=all</code>
     *
     * @param   node  the legacy node that contains either a valid node id or a dynamic children statements
     * @param   user  user performing the request
     *
     * @return  child nodes as legacy node array
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public Node[] getChildren(final Node node, final User user) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final GenericCollectionResource<CidsNode> restCidsNodes;
        try {
            if (node.getId() == -1) {
                if ((node.getDynamicChildrenStatement() == null) || node.getDynamicChildrenStatement().isEmpty()) {
                    final String message = "node '" + node.getName() + "' with id '" + node.getId()
                                + "' has no dynamic children statement, returning null!";
                    LOG.error(message);
                    return new Node[] {};
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("node '" + node.getName() + "' with id '" + node.getId()
                                + "' has a dynamic children statement");
                }

                final WebResource webResource = this.createWebResource(NODES_API)
                            .path(user.getDomain() + "/children")
                            .queryParams(queryParameters);
                WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
                builder = this.createMediaTypeHeaders(builder);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getDynamicChildren of node '" + node.getName()
                                + "' (" + node.getId() + ") for user '" + user + "': " + webResource.toString());
                }
                restCidsNodes = builder.post(new GenericType<GenericCollectionResource<CidsNode>>() {
                        },
                        node.getDynamicChildrenStatement());
            } else {
                final WebResource webResource = this.createWebResource(NODES_API)
                            .path(user.getDomain() + "." + String.valueOf(node.getId()) + "/children")
                            .queryParams(queryParameters);
                WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
                builder = this.createMediaTypeHeaders(builder);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getChildren of node '" + node.getName()
                                + "' (" + node.getId() + ") for user '" + user + "': " + webResource.toString());
                }
                restCidsNodes = builder.get(new GenericType<GenericCollectionResource<CidsNode>>() {
                        });
            }

            if ((restCidsNodes != null) && (restCidsNodes.get$collection() != null)
                        && (restCidsNodes.get$collection().size() > 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("found " + restCidsNodes.get$collection().size()
                                + " child nodes of node '" + node.getName()
                                + "' (" + node.getId() + ") for user '" + user.getName()
                                + "'. performing conversion to cids legacy nodes.");
                }

                final Node[] legacyNodes = new Node[restCidsNodes.get$collection().size()];
                int i = 0;
                for (final CidsNode cidsNode : restCidsNodes.get$collection()) {
                    try {
                        final Node legacyNode = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(cidsNode);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * TODO: Implement method in Nodes API or remove
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   node    TODO
     * @param   parent  TODO
     * @param   user    TODO
     *
     * @return  TODO
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  Unsupported Operation
     */
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
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   node  TODO
     * @param   user  TODO
     *
     * @return  TODO
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  always thrown
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
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   from  TODO
     * @param   to    TODO
     * @param   user  TODO
     *
     * @return  TODO
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  Unsupported Operation
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
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   from  TODO
     * @param   to    TODO
     * @param   user  TODO
     *
     * @return  TODO
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  always thrown
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
     * Gets a legacy node by its id. The method is not restricted to nodes of type of MetaObjectNode!<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/nodes/SWITCHON.7">http://localhost:8890/nodes/SWITCHON.7</a></code>
     *
     * @param   user    legacy user
     * @param   nodeID  id of the legacy node
     * @param   domain  domain name
     *
     * @return  legacy node object or null
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User user, final int nodeID, final String domain) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(NODES_API)
                    .path(user.getDomain() + "." + String.valueOf(nodeID))
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaObjectNode by id '" + nodeID
                        + "' for user '" + user + "' and domain '"
                        + user.getDomain() + "': " + webResource.toString());
        }

        try {
            final CidsNode restCidsNode = builder.get(CidsNode.class);

            if (restCidsNode != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("found node '" + restCidsNode.getName()
                                + "' (" + restCidsNode.getId() + ") for user '" + user.getName()
                                + "'. performing conversion to cids legacy nodes.");
                }
                try {
                    final Node legacyNode = CidsNodeFactory.getFactory().legacyCidsNodeFromRestCidsNode(restCidsNode);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * Performs a search for MetaObjectNodes by SQL Query. The query has to generate a result set that contains the
     * columns classId and objectId.<br>
     * <br>
     * <strong>Note</strong>: This method is delegated to the cids custom server search
     * {@link MetaObjectNodesByQuerySearch} and thus deprecated.
     *
     * @param       user   user performing the request
     * @param       query  SQL query that returns classId and objectId
     *
     * @return      Array of meta object nodes or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @deprecated  should be replaced by custom search
     */
    @Override
    public Node[] getMetaObjectNode(final User user, final String query) throws RemoteException {
        LOG.warn("delegating getMetaObjectNodes(String query, ...) with query '"
                    + query + "' to legacy custom server search!");

        final MetaObjectNodesByQuerySearch metaObjectNodesByQuerySearch = new MetaObjectNodesByQuerySearch();

        metaObjectNodesByQuerySearch.setDomain(user.getDomain());
        metaObjectNodesByQuerySearch.setQuery(query);

        final Collection metaObjectNodeCollection = this.customServerSearch(user, metaObjectNodesByQuerySearch);

        final MetaObjectNode[] metaObjectNodes = (MetaObjectNode[])metaObjectNodeCollection.toArray(
                new MetaObjectNode[metaObjectNodeCollection.size()]);

        return metaObjectNodes;
    }

    /**
     * See {@link #getMetaObjectNode(Sirius.server.newuser.User, java.lang.String) }.
     *
     * @param       user   user performing the request
     * @param       query  SQL query that returns classId and objectId
     *
     * @return      Array of meta object nodes or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @deprecated  should be replaced by custom search
     */
    @Override
    public Node[] getMetaObjectNode(final User user, final Query query) throws RemoteException {
        return this.getMetaObjectNode(user, query.getStatement());
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user  parameter is ignored
     *
     * @return      empty node array
     *
     * @throws      RemoteException  is never thrown
     *
     * @deprecated  ClassTreeNodes no longer supported
     */
    @Override
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by Nodes REST API!";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new Node[] {};
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user    parameter is ignored
     * @param       domain  parameter is ignored
     *
     * @return      empty node array
     *
     * @throws      RemoteException  is never thrown
     *
     * @deprecated  ClassTreeNodes no longer supported
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by Nodes REST API!";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new Node[] {};
    }
    // <editor-fold desc="ENTITY INFO (CLASSES) API" defaultstate="collapsed">
    /**
     * Gets a meta class by its legacy class id (int) for the user form the specified domain. The connector performs
     * fist a lookup for legacy meta class ids (int) against REST meta class ids (strings) in a local class key cache
     * and then delegates the call to
     * {@link #getClassByTableName(Sirius.server.newuser.User, java.lang.String, java.lang.String)}.<br>
     * <br>
     * <strong>Example REST Call (delegated):</strong><br>
     * <code><a href="http://localhost:8890/classes/SWITCHON.tag">http://localhost:8890/classes/SWITCHON.tag</a></code>
     *
     * @param   user     user performing the request
     * @param   classId  legacy id of the class
     * @param   domain   domain of the class
     *
     * @return  MetaClass matching the ID
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public MetaClass getClass(final User user, final int classId, final String domain) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getClass '" + classId + "@" + domain + "' for user '" + user + "'");
        }

        if (!this.classKeyCache.isDomainCached(domain)) {
            LOG.info("class key cache does not contain class ids for domain '" + domain
                        + "', need to fill the cache first!");
            this.getClasses(user, domain);
        }

        final String className = this.classKeyCache.getClassNameForClassId(domain, classId);
        if (className == null) {
            final String message = "could not find class with id '" + classId
                        + "' at domain '" + domain + "' for user '"
                        + user.getName() + "', class key map does not contain id.";
            LOG.error(message);
            // return null;
            throw new RemoteException(message);
        }

        return this.getClassByTableName(user, className, domain);
    }

    /**
     * <p>Gets a meta class by its name for the user form the specified domain. The REST meta class id is constructed as
     * <code>tableName.domain</code></p>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/classes/SWITCHON.tag">http://localhost:8890/classes/SWITCHON.tag</a></code>
     *
     * @param   user       user performing the request
     * @param   tableName  name of the class
     * @param   domain     domain of the class
     *
     * @return  MetaClass matching the (table)name
     *
     * @throws  RemoteException  if any remote error occurs
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getClassByTableName '" + tableName + "@" + domain + "' for user '" + user + "': "
                        + webResource.toString());
        }

        try {
            final CidsClass cidsClass = builder.get(CidsClass.class);
            if (cidsClass != null) {
                try {
                    final MetaClass metaClass = CidsClassFactory.getFactory()
                                .legacyCidsClassFromRestCidsClass(cidsClass);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * <p>Gets all meta classes for the user from the specified the domain.</p>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/classes?domain=SWITCHON">http://localhost:8890/classes?domain=SWITCHON</a>
     * </code>
     *
     * @param   user    legacy cids user performing the request
     * @param   domain  domain (~localserver)
     *
     * @return  Array with all meta classes
     *
     * @throws  RemoteException  if any server error occurs
     */
    @Override
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("domain", domain);
        final WebResource webResource = this.createWebResource(CLASSES_API).queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getClasses from domain '" + domain + "' for user '" + user + "': "
                        + webResource.toString());
        }

        try {
            final GenericCollectionResource<CidsClass> restCidsClasses = builder.get(
                    new GenericType<GenericCollectionResource<CidsClass>>() {
                    });

            if ((restCidsClasses != null) && (restCidsClasses.get$collection() != null)
                        && (restCidsClasses.get$collection().size() > 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("found " + restCidsClasses.get$collection().size()
                                + " cids classes at domain '" + domain
                                + "' for user '" + user.getName() + "'. performing conversion to cids legacy class.");
                }

                final MetaClass[] metaClasses = new MetaClass[restCidsClasses.get$collection().size()];
                int i = 0;
                for (final CidsClass cidsClass : restCidsClasses.get$collection()) {
                    try {
                        final MetaClass metaClass = CidsClassFactory.getFactory()
                                    .legacyCidsClassFromRestCidsClass(cidsClass);
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
            final String message = "could not get cids classes at domain '"
                        + domain + "' for user '" + user.getName() + "': "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    // </editor-fold>
    // <editor-fold desc="METHODS (DEPRECATED)" defaultstate="collapsed">
    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user  parameter is ignored
     *
     * @return      <strong>empty</strong> MethodMap;
     *
     * @throws      RemoteException  never thrown
     *
     * @deprecated  UnsupportedOperation
     */
    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new MethodMap();
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user             parameter is ignored
     * @param       localServerName  parameter is ignored
     *
     * @return      <strong>empty</strong> MethodMap;
     *
     * @throws      RemoteException  never thrown
     *
     * @deprecated  UnsupportedOperation
     */
    @Override
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new MethodMap();
    }

    // </editor-fold>
    // <editor-fold desc="QUERIES (DEPRECATED)" defaultstate="collapsed">
    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       user  parameter is ignored
     * @param       data  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public boolean storeQuery(final User user, final QueryData data) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       user  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public Info[] getQueryInfos(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       userGroup  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       id      parameter is ignored
     * @param       domain  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public QueryData getQuery(final int id, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       id      parameter is ignored
     * @param       domain  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  always thrown
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public boolean delete(final int id, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       user         parameter is ignored
     * @param       name         parameter is ignored
     * @param       description  parameter is ignored
     * @param       statement    parameter is ignored
     * @param       resultType   parameter is ignored
     * @param       isUpdate     parameter is ignored
     * @param       isBatch      parameter is ignored
     * @param       isRoot       parameter is ignored
     * @param       isUnion      parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  Unsupported Operation
     *
     * @deprecated  Unsupported Operation
     */
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
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       user         parameter is ignored
     * @param       name         parameter is ignored
     * @param       description  parameter is ignored
     * @param       statement    parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  Unsupported Operation
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public int addQuery(final User user, final String name, final String description, final String statement)
            throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       user           parameter is ignored
     * @param       queryId        parameter is ignored
     * @param       typeId         parameter is ignored
     * @param       paramkey       parameter is ignored
     * @param       description    parameter is ignored
     * @param       isQueryResult  parameter is ignored
     * @param       queryPosition  parameter is ignored
     *
     * @return      UnsupportedOperationException
     *
     * @throws      RemoteException                never thrown
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param                                user         parameter is ignored
     * @param                                queryId      parameter is ignored
     * @param                                paramkey     parameter is ignored
     * @param                                description  parameter is ignored
     *
     * @return                               DOCUMENT ME!
     *
     * @throws                               RemoteException                never thrown
     * @throws                               UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated                           Unsupported Operation
     * @returnUnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final String paramkey,
            final String description) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    // </editor-fold>
    // <editor-fold desc="SEARCH API" defaultstate="collapsed">
    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user  parameter is ignored
     *
     * @return      <strong>empty</strong> HashMap;
     *
     * @throws      RemoteException  never thrown
     *
     * @deprecated  >Unsupported Operation
     */
    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by cids REST API! -> returns empty result";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new HashMap(0);
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it returns an empty result!</p>
     *
     * @param       user    parameter is ignored
     * @param       domain  parameter is ignored
     *
     * @return      <strong>empty</strong> HashMap;
     *
     * @throws      RemoteException  never thrown
     *
     * @deprecated  Unsupported Operation
     */
    @Override
    public HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by cids REST API! -> returns empty result";
        LOG.warn(message);
        // throw new UnsupportedOperationException(message);
        return new HashMap(0);
    }

    @Override
    /**
     * <strong>Unsupported Operation</strong>
     * <p>
     * This operation is not supported anymore in the cids REST API, it throws
     * an UnsupportedOperationException Exception</p>
     *
     * @param user parameter is ignored
     * @param classIds parameter is ignored
     * @param searchOptions parameter is ignored
     * @return UnsupportedOperationException
     * @throws RemoteException not thrown
     * @deprecated
     */
    public SearchResult search(final User user, final String[] classIds, final SearchOption[] searchOptions)
            throws RemoteException {
        final String message = "The method '" + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Performs a remote server search by submitting a <i>parameterized</i> CidsServerSearch instance. If the
     * CidsServerSearch does not implement the {@link RestApiCidsServerSearch} interface, the
     * {@link ServerSearchFactory} tries to automatically generate a {@link SearchInfo} object hat is required by the
     * REST Search API.<br>
     * <strong>Example REST Call:</strong><br>
     * <code>curl --user username@SWITCHON:password -H "Content-Type: application/json" -X POST -d
     * "{""list"":[{""key"":""Query"",""value"":""keyword:\\""soil\\"" limit:\\""5\\"""" }]}"
     * http://localhost:8890/searches/SWITCHON.de.cismet.cids.custom.switchon.search.server.MetaObjectUniversalSearchStatement/results
     * </code>
     *
     * @param   user          The user performing the request
     * @param   serverSearch  The CidsServerSearch instance
     *
     * @return  Untyped Collection of results
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        final String searchKey = serverSearch.getClass().getName();
        final SearchInfo searchInfo = ServerSearchFactory.getFactory().getServerSearchInfo(searchKey);

        if ((searchInfo == null)) {
            final String message = "could not find cids server search  '" + searchKey + "'";
            LOG.error(message);
            throw new RemoteException(message);
        }

        final String domain = user.getDomain();
        final GenericCollectionResource<JsonNode> searchResults;
        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(SEARCH_API)
                    .path(user.getDomain() + "." + searchKey + SEARCH_API_RESULTS)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("customServerSearch '" + searchKey
                        + "' for user '" + user + "' and domain '"
                        + domain + "': " + webResource.toString());
        }

        final SearchParameters searchParameters;

        try {
            searchParameters = ServerSearchFactory.getFactory()
                        .searchParametersFromServerSearchInstance(searchKey, serverSearch);
        } catch (Exception ex) {
            final String message = "could not perform customServerSearch '" + searchKey
                        + "' for user '" + user + "', error during creation of search parameters:"
                        + ex.getMessage();
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        }

        searchResults = builder.post(new GenericType<GenericCollectionResource<JsonNode>>() {
                }, searchParameters);

        if ((searchResults != null) && (searchResults.get$collection() != null)
                    && (searchResults.get$collection().size() > 0)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("found " + searchResults.get$collection().size()
                            + " search results. performing conversion to legacy objects of type '"
                            + searchInfo.getResultDescription().getType().name() + "'");
            }

            if (searchInfo.getResultDescription().getType() == Type.ENTITY_REFERENCE) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("search result of cids server search '"
                                + searchKey + "' is a LightweightMetaObject, need to perform custom conversion");
                }

                final Collection<LightweightMetaObject> resultCollection = new LinkedList<LightweightMetaObject>();

                for (final JsonNode jsonNode : searchResults.get$collection()) {
                    try {
                        if (jsonNode.isObject()) {
                            final ObjectNode objectNode = (ObjectNode)jsonNode;
                            final String classKey = CidsBeanFactory.getFactory().getClassKey(objectNode);
                            final int classId = classKeyCache.getClassIdForClassKey(classKey);
                            final LightweightMetaObject lightweightMetaObject = CidsBeanFactory.getFactory()
                                        .lightweightMetaObjectFromJsonNode(
                                            objectNode,
                                            classId,
                                            domain,
                                            user);
                            resultCollection.add(lightweightMetaObject);
                        } else {
                            final String message = "could not deserialize cids bean from "
                                        + "object node to create LightweightMetaObject: JsonNode '"
                                        + jsonNode + "' is no ObjectNode!";
                            LOG.error(message);
                            throw new RemoteException(message);
                        }
                    } catch (Exception ex) {
                        final String message =
                            "could not deserialize cids bean from object node to create LightweightMetaObject: "
                                    + ex.getMessage();
                        LOG.error(message, ex);
                        throw new RemoteException(message, ex);
                    }
                }
                return resultCollection;
            } else {
                try {
                    final Collection resultCollection = ServerSearchFactory.getFactory()
                                .resultCollectionfromJsonNodes(
                                    searchResults.get$collection(),
                                    searchInfo);
                    return resultCollection;
                } catch (Exception ex) {
                    final String message = "could not perform converison of result collection of customServerSearch '"
                                + searchKey
                                + "' for user '"
                                + user
                                + ": "
                                + ex.getMessage();
                    LOG.error(message, ex);
                    throw new RemoteException(message, ex);
                }
            }
        } else {
            LOG.warn("customServerSearch '" + searchKey + "' for user '" + user
                        + "' with " + searchParameters.getList().size()
                        + " search parameters did not return any results");
            return new LinkedList();
        }
    }

    // </editor-fold>
    // <editor-fold desc="INFRASTRUCTURE API (NEW)" defaultstate="collapsed">
    /**
     * TODO: To be implemented in cids REST Infrastructure API.
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @return  list with domain names
     *
     * @throws  RemoteException                if any remote error occurs
     * @throws  UnsupportedOperationException  always thrown
     */
    @Override
    public String[] getDomains() throws RemoteException {
        // TODO: Implement method in INFRASTRUCTURE API or remove
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: To be implemented in cids REST Infrastructure API
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param       domain  name of the domain
     *
     * @return      TODO
     *
     * @throws      RemoteException                if any remote error occurs
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  should not return binary images!
     */
    @Override
    public Image[] getDefaultIcons(final String domain) throws RemoteException {
        // TODO: Implement method in INFRASTRUCTURE API or remove
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: To be implemented in cids REST Infrastructure API
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @return      TODO
     *
     * @throws      RemoteException                if any remote error occurs
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  should not return binary images!
     */
    @Override
    public Image[] getDefaultIcons() throws RemoteException {
        // TODO: Implement method in INFRASTRUCTURE API or remove
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: To be implemented in cids REST Infrastructure API
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   classId   TODO
     * @param   objectId  TODO
     * @param   domain    TODO
     * @param   user      TODO
     * @param   elements  TODO
     *
     * @return  TODO
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        // TODO: Implement method in INFRASTRUCTURE API or remove
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    // </editor-fold>
    // <editor-fold desc="USERS API" defaultstate="collapsed">
    /**
     * TODO: Implement Method in Users API or remove.<br>
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * <p>See <a href="https://github.com/cismet/cids-server/issues/103">
     * https://github.com/cismet/cids-server/issues/103</a></p>
     *
     * @param   user         TODO
     * @param   oldPassword  TODO
     * @param   newPassword  TODO
     *
     * @return  UnsupportedOperationException
     *
     * @throws  RemoteException                TODO
     * @throws  UserException                  TODO
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        // TODO:  Implement Method in Users API or remove.
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the Users REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Authenticates a user with the specified name and password at the specified domain.
     *
     * @param   userGroupLsName  not supported by REST API
     * @param   userGroupName    not supported by REST API
     * @param   userLsName       user domain
     * @param   userName         user name
     * @param   password         password of the users
     *
     * @return  legacy user object
     *
     * @throws  RemoteException  if any remote error occours
     * @throws  UserException    if the login fails
     */
    @Override
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing validation of user '" + userName + "' af domain '" + userLsName + "'");
        }
        LOG.warn("userGroupLsName '" + userGroupLsName + "' and userGroupName '" + userGroupName
                    + "' are ignored since they are not supported by RESTful service interface (yet).");

        final User cidsUser = new User(-1, userName, userLsName);

        this.putBasicAuthString(cidsUser, password);

        final WebResource webResource = this.createWebResource(USERS_API);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, cidsUser);
        builder = this.createMediaTypeHeaders(builder);

        try {
            final de.cismet.cids.server.api.types.User restUser = builder.get(
                    de.cismet.cids.server.api.types.User.class);
            return UserFactory.getFactory().cidsUserFromRestUser(restUser);
        } catch (UniformInterfaceException ue) {
            this.removeBasicAuthString(cidsUser);

            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "login of user '"
                        + userName
                        + "' at domain '"
                        + userLsName
                        + "' failed: "
                        + status.toString();
            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }

            if (status.getStatusCode() < 500) {
                throw new UserException(message, true, true, false, false);
            } else {
                throw new RemoteException(message, ue);
            }
        }
    }

    /**
     * TODO: Implement Method in Users API or remove.<br>
     *
     * <p>See <a href="https://github.com/cismet/cids-server/issues/103">
     * https://github.com/cismet/cids-server/issues/103</a></p>
     *
     * @return  UnsupportedOperationException
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames() throws RemoteException {
        // TODO:  Implement Method in Users API or remove.
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the Users REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: Implement Method in Users API or remove.<br>
     *
     * <p>See <a href="https://github.com/cismet/cids-server/issues/103">
     * https://github.com/cismet/cids-server/issues/103</a></p>
     *
     * @param   userName  TODO
     * @param   lsHome    TODO
     *
     * @return  UnsupportedOperationException
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        // TODO:  Implement Method in Users API or remove.
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the Users REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    // </editor-fold>
    // <editor-fold desc="CONFIGATTRIBUTES API" defaultstate="collapsed">
    /**
     * TODO: Implement ConfigAttributes API. See <a href="https://github.com/cismet/cids-server/issues/118">
     * https://github.com/cismet/cids-server/issues/118</a>
     *
     * <p>This operation is currently not implemented in the cids REST API, it throws an Unsupported Operation
     * Exception!</p>
     *
     * @param   user  TODO
     * @param   key   TODO
     *
     * @return  UnsupportedOperationException
     *
     * @throws  RemoteException                if any remote error occurs
     * @throws  UnsupportedOperationException  Implement ConfigAttributes API
     */
    @Override
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        // TODO: Implement ConfigAttributes API.
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the ConfigAttributes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * TODO: Implement ConfigAttributes API. See <a href="https://github.com/cismet/cids-server/issues/118">
     * https://github.com/cismet/cids-server/issues/118</a>
     *
     * @param   user  TODO
     * @param   key   TODO
     *
     * @return  UnsupportedOperationException
     *
     * @throws  RemoteException                TODO
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @Override
    public boolean hasConfigAttr(final User user, final String key) throws RemoteException {
        // TODO: Implement ConfigAttributes API.
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is not yet supported by the ConfigAttributes REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    // </editor-fold>

    //~ Instance fields --------------------------------------------------------

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;
    private final transient ClassNameCache classKeyCache;
    private final transient Proxy proxy;

    /** for caching username/password combinations (needed for basic auth). */
    private final transient Map<String, String> credentialsCache;

    //~ Methods ----------------------------------------------------------------

    //</editor-fold>
    // <editor-fold desc="ENTITIES API" defaultstate="collapsed">

    /**
     * See
     * {@link #getLightweightMetaObjectsByQuery(int, Sirius.server.newuser.User, java.lang.String, java.lang.String[]) }.
     *
     * @param       user   user performing the request
     * @param       query  SQL query to select meta objects
     *
     * @return      Array of meta objects or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @see         #getMetaObject(Sirius.server.newuser.User, java.lang.String, java.lang.String)
     * @deprecated  should be replaced by custom search
     */
    @Override
    public MetaObject[] getMetaObject(final User user, final String query) throws RemoteException {
        return this.getMetaObject(user, query, user.getDomain());
    }

    /**
     * Performs a search for MetaObjects by SQL Query. The query has to generate a result set that contains the columns
     * classId and objectId.<br>
     * <br>
     * <strong>Note</strong>: This method is delegated to the cids custom server search {@link MetaObjectsByQuerySearch}
     * and thus deprecated.
     *
     * @param       user    user performing the request
     * @param       query   SQL query that returns classId and objectId
     * @param       domain  DOCUMENT ME!
     *
     * @return      Array of meta objects or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @deprecated  should be replaced by custom search
     */
    @Override
    public MetaObject[] getMetaObject(final User user, final String query, final String domain) throws RemoteException {
        LOG.warn("delegating getMetaObject(String query, ...) with query '"
                    + query + "' to legacy custom server search!");

        final MetaObjectsByQuerySearch metaObjectsByQuerySearch = new MetaObjectsByQuerySearch();

        metaObjectsByQuerySearch.setDomain(user.getDomain());
        metaObjectsByQuerySearch.setQuery(query);

        final Collection metaObjectCollection = this.customServerSearch(user, metaObjectsByQuerySearch);

        final MetaObject[] metaObjects = (MetaObject[])metaObjectCollection.toArray(
                new MetaObject[metaObjectCollection.size()]);

        return metaObjects;
    }

    /**
     * See
     * {@link #getLightweightMetaObjectsByQuery(int, Sirius.server.newuser.User, java.lang.String, java.lang.String[]) }.
     *
     * @param       user   user performing the request
     * @param       query  SQL query to select meta objects
     *
     * @return      Array of meta objects or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @see         #getMetaObject(Sirius.server.newuser.User, java.lang.String, java.lang.String)
     * @deprecated  should be replaced by custom search
     */
    @Override
    public MetaObject[] getMetaObject(final User user, final Query query) throws RemoteException {
        return this.getMetaObject(user, query.getStatement());
    }

    /**
     * See
     * {@link #getLightweightMetaObjectsByQuery(int, Sirius.server.newuser.User, java.lang.String, java.lang.String[]) }.
     *
     * @param       user    user performing the request
     * @param       query   SQL query to select meta objects
     * @param       domain  domain to perform the query
     *
     * @return      Array of meta objects or empty array
     *
     * @throws      RemoteException  if any remote error occurs
     *
     * @see         #getMetaObject(Sirius.server.newuser.User, java.lang.String, java.lang.String)
     * @deprecated  should be replaced by custom search
     */
    @Override
    public MetaObject[] getMetaObject(final User user, final Query query, final String domain) throws RemoteException {
        return this.getMetaObject(user, query.getStatement(), domain);
    }

    /**
     * Retrieves a Meta data object referenced by a symbolic pointer to the MIS objctId@classID@domain from this
     * pointer.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/SWITCHON.CONTACT/76">http://localhost:8890/SWITCHON.CONTACT/76</a></code>
     *
     * @param   user      user token
     * @param   objectId  symbolic pointer to the meta object
     * @param   classId   class of the meta object
     * @param   domain    domain where the meta object is hosted
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getMetaObject(final User user, final int objectId, final int classId, final String domain)
            throws RemoteException {
        final String className = this.getClassNameForClassId(user, domain, classId);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("deduplicate", "true");
        final WebResource webResource = this.createWebResource(ENTITIES_API)
                    .path(domain + "." + className + "/" + objectId)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaObject '" + objectId + "@" + classId + "@" + domain
                        + "' (" + domain + "." + className + ") for user '" + user + "' :" + webResource.toString());
        }

        try {
            final JsonNode objectNode = builder.get(ObjectNode.class);
            if ((objectNode == null) || (objectNode.size() == 0)) {
                LOG.error("could not find meta object  '" + objectId + "@" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user + "'");
                return null;
            }

            final CidsBean cidsBean;
            try {
                cidsBean = CidsBean.createNewCidsBeanFromJSON(false, objectNode.toString());
            } catch (Exception ex) {
                final String message = "could not deserialize cids bean from object node  '"
                            + objectId
                            + "@"
                            + classId
                            + "@"
                            + domain
                            + "' ("
                            + domain
                            + "."
                            + className
                            + ") for user '"
                            + user
                            + "': "
                            + ex.getMessage();
                LOG.error(message, ex);
                throw new RemoteException(message, ex);
            }

            if (cidsBean != null) {
                final MetaObject metaObject = cidsBean.getMetaObject();
                return metaObject;
            } else {
                LOG.error("could not find meta object  '" + objectId + "@" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user + "'");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could not get meta object '"
                        + objectId
                        + "@"
                        + classId
                        + "@"
                        + domain
                        + "' ("
                        + domain
                        + "."
                        + className
                        + ") for user '"
                        + user
                        + "': "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * Creates a new meta object and returns the resulting instance.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>curl --user username@SWITCHON:password -H "Content-Type: application/json" -X POST -d
     * "{$self:'/SWITCHON.contact/31337' }" http://localhost:8890/SWITCHON.contact</code>
     *
     * @param   user        user token
     * @param   metaObject  the new meta object to be created
     * @param   domain      domain of the meta object
     *
     * @return  the remotely created meta object (resulting instance)
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        final int classId = metaObject.getClassID();
        final String className = this.getClassNameForClassId(user, domain, classId);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("requestResultingInstance", "true");
        final WebResource webResource = this.createWebResource(ENTITIES_API)
                    .path(domain + "." + className)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("insertMetaObject for class '" + classId + "@" + domain
                        + "' (" + domain + "." + className + ") for user '" + user + "' :" + webResource.toString());
        }

        try {
            final JsonNode objectNode = builder.post(ObjectNode.class,
                    metaObject.getBean().toJSONString(true));
            if ((objectNode == null) || (objectNode.size() == 0)) {
                LOG.error("could not insert meta object for class '" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user
                            + "': newly inserted meta object could not be found");
                return null;
            }

            final CidsBean cidsBean;
            try {
                cidsBean = CidsBean.createNewCidsBeanFromJSON(false, objectNode.toString());
            } catch (Exception ex) {
                final String message = "could not deserialize cids bean from object node for class '"
                            + classId
                            + "@"
                            + domain
                            + "' ("
                            + domain
                            + "."
                            + className
                            + ") for user '"
                            + user
                            + "': "
                            + ex.getMessage();
                LOG.error(message, ex);
                throw new RemoteException(message, ex);
            }

            if (cidsBean != null) {
                final MetaObject newMetaObject = cidsBean.getMetaObject();
                return newMetaObject;
            } else {
                LOG.error("could not insert meta object for class '" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user
                            + "': newly inserted meta object could not be found");
                return null;
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could not insert meta object for class  '"
                        + classId
                        + "@"
                        + domain
                        + "' ("
                        + domain
                        + "."
                        + className
                        + ") for user '"
                        + user
                        + "': "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an UnsupportedOperationException!</p>
     *
     * @param       user    DOCUMENT ME!
     * @param       query   DOCUMENT ME!
     * @param       domain  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @throws      RemoteException                DOCUMENT ME!
     * @throws      UnsupportedOperationException  always thrown
     *
     * @deprecated  update by SQL query not supported anymore
     */
    @Override
    public int insertMetaObject(final User user, final Query query, final String domain) throws RemoteException {
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Updates an existing meta object.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>curl --user username@SWITCHON:password -H "Content-Type: application/json" -X PUT -d
     * "{$self:'/SWITCHON.contact/31337' }" http://localhost:8890/SWITCHON.contact/31337</code>
     *
     * @param   user        user token
     * @param   metaObject  the meta object to be updated
     * @param   domain      domain of the meta object
     *
     * @return  status code (1 == successful)
     *
     * @throws  RemoteException  RemoteException if any remote error occurs
     */
    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        final int objectId = metaObject.getID();
        final int classId = metaObject.getClassID();
        final String className = this.getClassNameForClassId(user, domain, classId);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("requestResultingInstance", "false");
        final WebResource webResource = this.createWebResource(ENTITIES_API)
                    .path(domain + "." + className + "/" + objectId)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateMetaObject '" + objectId + "@" + classId + "@" + domain
                        + "' (" + domain + "." + className + ") for user '" + user + "' :" + webResource.toString());
        }

        try {
            builder.put(ObjectNode.class, metaObject.getBean().toJSONString(true));
            return 1;
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could not update meta object '"
                        + objectId
                        + "@"
                        + classId
                        + "@"
                        + domain
                        + "' ("
                        + domain
                        + "."
                        + className
                        + ") for user '"
                        + user
                        + "': "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * <strong>Unsupported Operation.</strong>
     *
     * <p>This operation is not supported anymore in the cids REST API, it throws an UnsupportedOperationException!</p>
     *
     * @param       user    user token
     * @param       query   sql query (update, insert, delete)
     * @param       domain  domain where the query is to be executed
     *
     * @return      how many data sets are affected
     *
     * @throws      RemoteException                server error (eg bad sql)
     * @throws      UnsupportedOperationException  always thrown
     *
     * @deprecated  no update by SQL query !
     */
    @Override
    public int update(final User user, final String query, final String domain) throws RemoteException {
        final String message = "The method '"
                    + Thread.currentThread().getStackTrace()[1].getMethodName()
                    + "' is deprecated and not supported by the cids REST API!";
        LOG.error(message);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Deletes a meta object.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>curl --user username@SWITCHON:password -X DELETE http://localhost:8890/switchon.contact/31337</code>
     *
     * @param   user        user token
     * @param   metaObject  the meta object to be deleted
     * @param   domain      the domain of the meta object
     *
     * @return  status code (1 == successful)
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        final int objectId = metaObject.getID();
        final int classId = metaObject.getClassID();
        final String className = this.getClassNameForClassId(user, domain, classId);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(ENTITIES_API)
                    .path(domain + "." + className + "/" + objectId)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteMetaObject '" + objectId + "@" + classId + "@" + domain
                        + "' (" + domain + "." + className + ") for user '" + user + "' :" + webResource.toString());
        }

        try {
            builder.delete(ObjectNode.class);
            return 1;
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could not delete meta object '"
                        + objectId
                        + "@"
                        + classId
                        + "@"
                        + domain
                        + "' ("
                        + domain
                        + "."
                        + className
                        + ") for user '"
                        + user
                        + "': "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }
    }

    /**
     * Returns all LightweightMetaObject of the class specified by classId. If The LightweightMetaObjects returned by
     * this method contain only the fields (attributes) specified by the representationFields String Array. The to
     * string representation {@link LightweightMetaObject#toString()} of the LightweightMetaObject is built from the
     * representationFields and formatted according to the {@link Formatter} representationPattern (e.g. "%1$2s").<br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/SWITCHON.CONTACT?level=1&fields=name,email">http://localhost:8890/SWITCHON.CONTACT?level=1&fields=name,email</a></code>
     *
     * @param   classId                legacy class id of the LightweightMetaObjects
     * @param   user                   user token
     * @param   representationFields   fields of the LightweightMetaObject
     * @param   representationPattern  the format pattern {@link Formatter}
     *
     * @return  Array of LightweightMetaObjects or null
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        final String domain = user.getDomain();
        final String className = this.getClassNameForClassId(user, domain, classId);
        final AbstractAttributeRepresentationFormater representationFormater;
        final LightweightMetaObject[] lightweightMetaObjects;
        final int representationFieldsLength = (representationFields != null) ? representationFields.length : 0;

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("deduplicate", "true");
        queryParameters.add("level",
            String.valueOf((representationFieldsLength > 0) ? 1 : 0));
        queryParameters.add("limit", String.valueOf(Integer.MAX_VALUE));

        final StringBuilder fieldsParameter = new StringBuilder();
        // fieldsParameter.append(metaClass.getPrimaryKey().toLowerCase());
        if (representationFieldsLength > 0) {
            for (final String representationField : representationFields) {
                fieldsParameter.append(representationField);
                fieldsParameter.append(',');
            }
            if (fieldsParameter.length() > 1) {
                fieldsParameter.deleteCharAt(fieldsParameter.length() - 1);
            }
        }
        queryParameters.add("fields", fieldsParameter.toString());

        final WebResource webResource = this.createWebResource(ENTITIES_API)
                    .path(domain + "." + className)
                    .queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllLightweightMetaObjectsForClass for class '" + classId + "@" + domain
                        + "' (" + domain + "." + className + ") for user '" + user
                        + "' with " + representationFieldsLength + " representation fields:"
                        + webResource.toString());
        }

        try {
            final GenericCollectionResource<ObjectNode> objectNodes = builder.get(
                    new GenericType<GenericCollectionResource<ObjectNode>>() {
                    });

            if ((objectNodes == null) || (objectNodes.get$collection() == null)
                        || objectNodes.get$collection().isEmpty()) {
                LOG.error("could not find any lightweight meta objects for class '" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user
                            + "' with " + representationFieldsLength + " representation fields.");
                return null;
            }

            if (representationPattern != null) {
                // LOG.warn("ignoring representation pattern '"+representationPattern+"'");
                representationFormater = new StringPatternFormater(representationPattern, representationFields);
            } else {
                // let the CidsBeanFactory handle the toString formatting if
                // CidsBeanFactory.LEGACY_DISPLAY_NAME is present
                representationFormater = null;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("found " + objectNodes.get$collection().size()
                            + " lightweight meta objects for class '" + classId + "@" + domain
                            + "' (" + domain + "." + className + ") for user '" + user
                            + "' with " + representationFieldsLength
                            + " representation fields. Performing conversion to cids legacy meta objects.");
            }

            lightweightMetaObjects = new LightweightMetaObject[objectNodes.get$collection().size()];
            int i = 0;
            for (final JsonNode objectNode : objectNodes.get$collection()) {
                final CidsBean cidsBean;
                try {
                    cidsBean = CidsBean.createNewCidsBeanFromJSON(false, objectNode.toString());
                } catch (Exception ex) {
                    final String message = "could not deserialize cids beans from object nodes for class '"
                                + classId
                                + "@"
                                + domain
                                + "' ("
                                + domain
                                + "."
                                + className
                                + ") for user '"
                                + user
                                + "' with "
                                + representationFieldsLength
                                + " representation fields: "
                                + ex.getMessage();
                    LOG.error(message, ex);
                    throw new RemoteException(message, ex);
                }

                // ensure that classKeyCache is initialized for CidsBeanFactory;
                if (!classKeyCache.isDomainCached(domain)) {
                    LOG.warn("class name cache not initialized yet for domain '" + domain
                                + "', need to fill the cache NOW!");
                    this.getClasses(user, domain);
                }

                if (cidsBean != null) {
                    final LightweightMetaObject lightweightMetaObject = CidsBeanFactory.getFactory()
                                .lightweightMetaObjectFromCidsBean(
                                    cidsBean,
                                    classId,
                                    domain,
                                    user,
                                    representationFields,
                                    representationFormater,
                                    this.classKeyCache);
                    lightweightMetaObjects[i] = lightweightMetaObject;
                    i++;
                } else {
                    LOG.error("could not find lightweight meta objects for class '" + classId + "@" + domain
                                + "' (" + domain + "." + className + ") for user '" + user
                                + "' with " + representationFieldsLength + " representation fields.");
                    return null;
                }
            }
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = "could not get lightweight meta object for class '"
                        + classId
                        + "@"
                        + domain
                        + "' ("
                        + domain
                        + "."
                        + className
                        + ") for user '"
                        + user
                        + "' with "
                        + representationFieldsLength
                        + " representation fields: "
                        + status.getReasonPhrase();

            LOG.error(message, ue);
            if (LOG.isDebugEnabled()) {
                LOG.debug(ue.getResponse().getEntity(String.class));
            }
            throw new RemoteException(message, ue);
        }

        return lightweightMetaObjects;
    }

    /**
     * Returns all LightweightMetaObject of the class specified by classId. If The LightweightMetaObjects returned by
     * this method contain only the fields (attributes) specified by the representationFields String Array.
     *
     * @param   classId               legacy class id of the LightweightMetaObjects
     * @param   user                  user token
     * @param   representationFields  files of the LightweightMetaObject
     *
     * @return  Array of LightweightMetaObjects or null
     *
     * @throws  RemoteException  if any remote error occurs
     *
     * @see     #getAllLightweightMetaObjectsForClass(int, Sirius.server.newuser.User, java.lang.String[],
     *          java.lang.String)
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        return this.getAllLightweightMetaObjectsForClass(classId, user,
                representationFields, null);
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
            final String representationPattern) throws RemoteException {
        LOG.warn("delegating getLightweightMetaObjectsByQuery for class + '"
                    + classId + "' with query '" + query + "' to legacy custom server search!");

        final LightweightMetaObjectsByQuerySearch lightweightMetaObjectsByQuerySearch =
            new LightweightMetaObjectsByQuerySearch();

        lightweightMetaObjectsByQuerySearch.setDomain(user.getDomain());
        lightweightMetaObjectsByQuerySearch.setClassId(classId);
        lightweightMetaObjectsByQuerySearch.setQuery(query);
        lightweightMetaObjectsByQuerySearch.setRepresentationFields(representationFields);
        lightweightMetaObjectsByQuerySearch.setRepresentationPattern(representationPattern);

        final Collection lwmoCollection = this.customServerSearch(user, lightweightMetaObjectsByQuerySearch);

        final LightweightMetaObject[] lightweightMetaObjects = (LightweightMetaObject[])lwmoCollection.toArray(
                new LightweightMetaObject[lwmoCollection.size()]);

        return lightweightMetaObjects;
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
            final String[] representationFields) throws RemoteException {
        return this.getLightweightMetaObjectsByQuery(
                classId,
                user,
                query,
                representationFields,
                null);
    }

    /**
     * Gets a new empty instance (MetaObject) of the specified MetaClass.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code><a href="http://localhost:8890/SWITCHON.RESOURCE/emptyInstance">http://localhost:8890/SWITCHON.RESOURCE/emptyInstance</a></code>
     *
     * @param   user       user performing the request
     * @param   metaClass  class of the new object
     *
     * @return  new meta object of class
     *
     * @throws  RemoteException  if any remote error occurs
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass metaClass) throws RemoteException {
        return metaClass.getEmptyInstance();
    }

    // </editor-fold>
    // <editor-fold desc="ACTIONS API" defaultstate="collapsed">

    /**
     * Executes a remote task in the context of the server.<br>
     * <br>
     * <strong>Example REST Call:</strong><br>
     * <code>curl --user admin@SWITCHON:cismet<br>
     * -F "taskparams"="{""actionKey"": ""downloadFile"",""description"": ""Download a remote file""
     * };type=application/json"<br>
     * -F "file"="filetodownload;text/plain"<br>
     * http://localhost:8890/actions/SWITCHON.downloadFile/tasks?role=all^&resultingInstanceType=result</code>
     *
     * @param   user      user performing the request
     * @param   taskname  name of the task to be performed
     * @param   domain    domain of the server / task
     * @param   body      body parameter of the task, e.g. byte[]
     * @param   params    0...n action parameters
     *
     * @return  result of the task, e.g. byte[]
     *
     * @throws  RemoteException  if the task execution fails
     */
    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String domain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        final GenericResourceWithContentType taskResult;
        final ActionTask actionTask = new ActionTask();
        final Map<String, Object> actionParameters = new LinkedHashMap();
        for (final ServerActionParameter param : params) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("processing ServerActionParameter '" + param.toString() + "'");
            }
            actionParameters.put(param.getKey(), param.getValue());
        }

        actionTask.setActionKey(taskname);
        actionTask.setParameters(actionParameters);

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        queryParameters.add("resultingInstanceType", "result");
        final WebResource webResource = this.createWebResource(ACTIONS_API)
                    .path(domain + "." + taskname + ACTIONS_API_TASKS)
                    .queryParams(queryParameters);
        final WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);

        if (LOG.isDebugEnabled()) {
            LOG.debug("executeTask '" + taskname
                        + "' for user '" + user + "' and domain '"
                        + domain + "' with " + params.length + " Server Action Parameters: "
                        + webResource.toString());
        }

        builder.type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);

        FormDataMultiPart multiPartData = new FormDataMultiPart();
        try {
            // taskparams lowercase !!!!!
            multiPartData = multiPartData.field(
                    "taskparams",
                    MAPPER.writeValueAsString(actionTask),
                    MediaType.APPLICATION_JSON_TYPE);
        } catch (IOException ex) {
            final String message = "could not serialize action task '"
                        + taskname
                        + "' for user '"
                        + user
                        + "' and domain '"
                        + domain
                        + "' with "
                        + params.length
                        + "': "
                        + ex.getMessage();
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        }

        if (body != null) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("creating Multi Part Form Data '" + MediaType.APPLICATION_OCTET_STREAM_TYPE + "'");
                }
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(body);
                final byte[] bodyBytes = bos.toByteArray();

//                final FormDataBodyPart formDataBodyPart = new FormDataBodyPart(
//                        "file",
//                        bodyBytes,
//                        MediaType.APPLICATION_OCTET_STREAM_TYPE);
//
//                multiPartData.bodyPart(formDataBodyPart);

                multiPartData = multiPartData.field("file", bodyBytes,
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);
            } catch (Throwable t) {
                final String message = "could not create binary attachment of action task '"
                            + taskname
                            + "' for user '"
                            + user
                            + "' and domain '"
                            + domain
                            + "' with "
                            + params.length
                            + "': "
                            + t.getMessage();
                LOG.error(message, t);
                throw new RemoteException(message, t);
            }
        }

        taskResult = builder.post(GenericResourceWithContentType.class, multiPartData);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeTask '" + taskname
                        + "' for user '" + user + "' and domain '"
                        + domain + "' with " + params.length
                        + "' Server Action Parameters returned result of type '"
                        + taskResult.getContentType() + "'");
        }

        return taskResult.getRes();
    }

    // </editor-fold>
}
