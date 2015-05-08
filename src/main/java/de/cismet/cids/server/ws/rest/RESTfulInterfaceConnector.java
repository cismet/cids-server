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
import com.sun.jersey.api.client.ClientResponse;
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

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.CollectionResource;
import de.cismet.cids.server.api.types.GenericCollectionResource;
import de.cismet.cids.server.api.types.legacy.UserFactory;
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.ws.SSLConfig;

import de.cismet.netutil.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
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
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 * @version 0.1 2015/04/17
 */
public class RESTfulInterfaceConnector implements CallServerService {

    public final static String USERS_API = "users";
    public final static String CLASSES_API = "classes";

    private static final transient Logger LOG = Logger.getLogger(RESTfulInterfaceConnector.class);

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;
    private static final int TIMEOUT = 10000;
    private final transient Proxy proxy;

    /**
     * for caching username/password combinations (needed for basic auth)
     */
    private final transient Map<String, String> credentialsCache;

//~ Constructors -----------------------------------------------------------
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
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @param sslConfig DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
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
    public String getRootResource() {
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
    public WebResource createWebResource(final String path) {
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

    private WebResource.Builder createAuthorisationHeader(final WebResource webResource, final User user) throws RemoteException {
        final String basicAuthString = this.getBasicAuthString(user);
        final WebResource.Builder builder = webResource.header("Authorization", basicAuthString);
        return builder;
    }

    private WebResource.Builder createMediaTypeHeaders(final WebResource.Builder builder) {
        return builder.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private WebResource.Builder createMediaTypeHeaders(final WebResource webResource) {
        return webResource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private MultivaluedMap createUserParameters(final MultivaluedMap queryParams, final User user) {
        if (user.getDomain() != null) {
            queryParams.add("domain", user.getDomain());
        }

        if (user.getUserGroup() != null) {
            queryParams.add("role", user.getUserGroup());
        }

        return queryParams;
    }

    private MultivaluedMap createUserParameters(final User user) {
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
    private String getBasicAuthString(final User user) throws RemoteException {

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
     * @param user
     * @param password
     */
    private void putBasicAuthString(final User user, final String password) throws RemoteException {

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
     * @param user
     */
    private void removeBasicAuthString(final User user) {
        final String key = user.getName() + "@" + user.getDomain();
        if (!this.credentialsCache.containsKey(key)) {
            LOG.warn("user '" + user.getName() + "' is not authenticated at '" + user.getDomain() + ", cannot remove");
        } else {
            this.credentialsCache.remove(key);
        }
    }

    @Override
    public Node[] getRoots(final User user, final String domainName) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node[] getRoots(final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public String[] getDomains() throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node[] getMetaObjectNode(final User usr, final Query query) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

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
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
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
    public int update(final User user, final String query, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaClass getClass(final User user, final int classID, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {

        final MultivaluedMap queryParameters = this.createUserParameters(user);
        final WebResource webResource = this.createWebResource(CLASSES_API).queryParams(queryParameters);
        WebResource.Builder builder = this.createAuthorisationHeader(webResource, user);
        builder = this.createMediaTypeHeaders(builder);

        try {
            //final List<CidsClass> restCidsClasses = builder.get(new GenericType<List<CidsClass>>(){});
            //return UserFactory.getFactory().cidsUserFromRestUser(restUser);
            
            final GenericCollectionResource<CidsClass> restCidsClasses = builder.get(new GenericType<GenericCollectionResource<CidsClass>>(){});
            return new MetaClass[]{};
        } catch (UniformInterfaceException ue) {
            final Status status = ue.getResponse().getClientResponseStatus();
            final String message = status.toString();
            LOG.error(message, ue);
            throw new RemoteException(message, ue);
        }
    }

    @Override
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
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
    public HistoryObject[] getHistory(final int classId,
            final int objectId,
            final String domain,
            final User user,
            final int elements) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

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

    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
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

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        throw new UnsupportedOperationException(Thread.currentThread().getStackTrace()[1].getMethodName() + " is not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }
}
