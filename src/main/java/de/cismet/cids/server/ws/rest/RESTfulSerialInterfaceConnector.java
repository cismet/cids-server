/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.localserver.method.MethodMap;
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ws.Converter;

import de.cismet.security.Proxy;

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
    private static final int TIMEOUT = 10000;

    //~ Instance fields --------------------------------------------------------

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;

    private final transient Proxy proxy;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource) {
        this(rootResource, null, null);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         config proxyURL DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource, final Proxy proxy) {
        this(rootResource, proxy, null);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource, final SSLConfig sslConfig) {
        this(rootResource, null, sslConfig);
    }

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     * @param  proxy         proxyConfig proxyURL DOCUMENT ME!
     * @param  sslConfig     DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource,
            final Proxy proxy,
            final SSLConfig sslConfig) {
        if (sslConfig != null) {
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

        clientCache = new Hashtable<String, Client>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  sslConfig  DOCUMENT ME!
     */
    private void initSSL(final SSLConfig sslConfig) {
//        try {
//            // server certificate for trustmanager
//            final KeyStore ks = KeyStore.getInstance("JKS");
//            ks.load(null, null);
//            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            final X509Certificate cert = (X509Certificate)cf.generateCertificate(new BufferedInputStream(
//                        new FileInputStream(
//                            "/Users/mscholl/svnwork/central/de/cismet/cids/cids-server/trunk/src/main/cert/cids-server-jetty.cert")));
//            ks.setCertificateEntry("cids-server-jetty", cert);
//            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//            tmf.init(ks);
//
//            // client certificate and key for key manager
//            final KeyStore keystore = KeyStore.getInstance("JKS");
//            keystore.load(new BufferedInputStream(
//                    new FileInputStream(
//                        "/Users/mscholl/svnwork/central/de/cismet/cids/cids-server/trunk/src/main/cert/cids-server-client.keystore")),
//                "b3vwi98zb".toCharArray());
//            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(keystore, "345txfx97c".toCharArray());
//
//            // init context
//            final SSLContext context = SSLContext.getInstance("TLS");
//            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new SSLHostnameVerifier());
//        } catch (final NoSuchAlgorithmException e) {
//            throw new IllegalStateException("system does not support SSL", e);
//        } catch (final KeyStoreException e) {
//            throw new IllegalStateException("system does not support java keystores", e);
//        } catch (final FileNotFoundException e) {
//            throw new IllegalStateException("cannot find keystore file", e);
//        } catch (final IOException e) {
//            throw new IllegalArgumentException("cannot read keystore", e);
//        } catch (final CertificateException e) {
//            throw new IllegalArgumentException("illegal certificate", e);
//        } catch (final KeyManagementException e) {
//            throw new IllegalStateException("ssl context init properly initialised", e);
//        } catch (final UnrecoverableKeyException e) {
//            throw new IllegalStateException("cannot get key from keystore", e);
//        }
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
            final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
            if ((proxy.getHost() != null) && (proxy.getPort() > 0)) {
                final URL url;
                try {
                    final String protocol;
                    if (proxy.getHost().startsWith("http")) {                          // NOI18N
                        protocol = "";                                                 // NOI18N
                    } else {
                        protocol = "http://";                                          // NOI18N
                    }
                    url = new URL(protocol + proxy.getHost() + ":" + proxy.getPort()); // NOI18N
                    config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PROXY_URI, url.toString());
                } catch (final MalformedURLException ex) {
                    LOG.warn("illegal proxy url, using no proxy", ex);                 // NOI18N
                }
            }
            if ((proxy.getUsername() != null) && (proxy.getPassword() != null)) {
                if (proxy.getDomain() != null) {
                    config.getState()
                            .setProxyCredentials(
                                null,
                                null,
                                -1,
                                proxy.getUsername(),
                                proxy.getPassword(),
                                "",                                                    // NOI18N
                                proxy.getDomain());
                } else {
                    config.getState().setProxyCredentials(null, null, -1, proxy.getUsername(), proxy.getPassword());
                }
            }
            clientCache.put(path, ApacheHttpClient.create(config));
        }
        final Client c = clientCache.get(path);
        c.setConnectTimeout(TIMEOUT);
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
     * @param   <T>   DOCUMENT ME!
     * @param   path  DOCUMENT ME!
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponsePOST(final String path, final Class<T> type) throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path);

        return getResponsePOST(builder, type, null);
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

        final byte[] bytes = builder.post(byte[].class, queryData);

        return Converter.deserialiseFromBase64(bytes, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   domainName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final String domainName) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domainName != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domainName));
            }

            try {
                return getResponsePOST("getRootsByDomain", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("getRoots", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   usr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getChildren(final Node node, final User usr) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (node != null) {
                queryParams.add(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }

            try {
                return getResponsePOST("getChildren", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node    DOCUMENT ME!
     * @param   parent  DOCUMENT ME!
     * @param   user    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (node != null) {
                queryParams.add(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (parent != null) {
                queryParams.add(PARAM_LINK_PARENT, Converter.serialiseToString(parent));
            }

            try {
                return getResponsePOST("addNode", queryParams, Node.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (node != null) {
                queryParams.add(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("deleteNode", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (from != null) {
                queryParams.add(PARAM_NODE_FROM, Converter.serialiseToString(from));
            }
            if (to != null) {
                queryParams.add(PARAM_NODE_TO, Converter.serialiseToString(to));
            }
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("addLink", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (from != null) {
                queryParams.add(PARAM_NODE_FROM, Converter.serialiseToString(from));
            }
            if (to != null) {
                queryParams.add(PARAM_NODE_TO, Converter.serialiseToString(to));
            }
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("deleteLink", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public String[] getDomains() throws RemoteException {
        try {
            return getResponsePOST("getDomains", String[].class); // NOI18N
        } catch (final UniformInterfaceException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("exception during request, remapping", ex);
            }

            final ClientResponse response = ex.getResponse();

            final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
            if (remEx == null) {
                throw ex;
            } else {
                throw remEx;
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr     DOCUMENT ME!
     * @param   nodeID  DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node getMetaObjectNode(final User usr, final int nodeID, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }

            queryParams.add(PARAM_NODE_ID, Converter.serialiseToString(nodeID));

            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getMetaObjectNodeByID", queryParams, Node.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final String query) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponsePOST("getMetaObjectNodeByString", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getMetaObjectNode(final User usr, final Query query) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponsePOST("getMetaObjectNodeByQuery", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject[] getMetaObject(final User usr, final String query) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponsePOST("getMetaObjectByString", queryParams, MetaObject[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject[] getMetaObject(final User usr, final Query query) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponsePOST("getMetaObjectByQuery", queryParams, MetaObject[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
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
    public MetaObject getMetaObject(final User usr, final int objectID, final int classID, final String domain)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (usr != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(usr));
            }

            queryParams.add(PARAM_OBJECT_ID, Converter.serialiseToString(objectID));
            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classID));

            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getMetaObjectByID", queryParams, MetaObject.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject insertMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.add(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("insertMetaObject", queryParams, MetaObject.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
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
    @Override
    public int insertMetaObject(final User user, final Query query, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("insertMetaObjectByQuery", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int updateMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.add(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("updateMetaObject", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int deleteMetaObject(final User user, final MetaObject metaObject, final String domain)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.add(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("deleteMetaObject", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
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
    @Override
    public int update(final User user, final String query, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("update", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   c     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaObject getInstance(final User user, final MetaClass c) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (c != null) {
                queryParams.add(PARAM_METACLASS, Converter.serialiseToString(c));
            }

            try {
                return getResponsePOST("getInstance", queryParams, MetaObject.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user       DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClassByTableName(final User user, final String tableName, final String domain)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (tableName != null) {
                queryParams.add(PARAM_TABLE_NAME, Converter.serialiseToString(tableName));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getClassByTableName", queryParams, MetaClass.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     * @param   domain   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass getClass(final User user, final int classID, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classID));

            try {
                return getResponsePOST("getClassByID", queryParams, MetaClass.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MetaClass[] getClasses(final User user, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getClasses", queryParams, MetaClass[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("getClassTreeNodesByUser", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getClassTreeNodesByDomain", queryParams, Node[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("getMethodsByUser", queryParams, MethodMap.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public MethodMap getMethods(final User user, final String localServerName) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (localServerName != null) {
                queryParams.add(PARAM_LOCAL_SERVER_NAME, Converter.serialiseToString(localServerName));
            }

            try {
                return getResponsePOST("getMethodsByDomain", queryParams, MethodMap.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classId));

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (representationFields != null) {
                queryParams.add(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }
            if (representationPattern != null) {
                queryParams.add(PARAM_REP_PATTERN, Converter.serialiseToString(representationPattern));
            }

            try {
                return getResponsePOST(
                        "getAllLightweightMetaObjectsForClassByPattern", // NOI18N
                        queryParams,
                        LightweightMetaObject[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classId,
            final User user,
            final String[] representationFields) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classId));

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (representationFields != null) {
                queryParams.add(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }

            try {
                return getResponsePOST(
                        "getAllLightweightMetaObjectsForClass", // NOI18N
                        queryParams,
                        LightweightMetaObject[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classId));

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (representationFields != null) {
                queryParams.add(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }
            if (representationPattern != null) {
                queryParams.add(PARAM_REP_PATTERN, Converter.serialiseToString(representationPattern));
            }

            try {
                return getResponsePOST(
                        "getLightweightMetaObjectsByQueryAndPattern", // NOI18N
                        queryParams,
                        LightweightMetaObject[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classId));

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.add(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (representationFields != null) {
                queryParams.add(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }

            try {
                return getResponsePOST(
                        "getLightweightMetaObjectsByQuery", // NOI18N
                        queryParams,
                        LightweightMetaObject[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean storeQuery(final User user, final QueryData data) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (data != null) {
                queryParams.add(PARAM_QUERY_DATA, Converter.serialiseToString(data));
            }

            try {
                return getResponsePOST("storeQuery", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Info[] getQueryInfos(final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("getQueryInfosByUser", queryParams, Info[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Info[] getQueryInfos(final UserGroup userGroup) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (userGroup != null) {
                queryParams.add(PARAM_USERGROUP, Converter.serialiseToString(userGroup));
            }

            try {
                return getResponsePOST("getQueryInfosByUserGroup", queryParams, Info[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public QueryData getQuery(final int id, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_QUERY_ID, Converter.serialiseToString(id));

            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getQuery", queryParams, QueryData.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean delete(final int id, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            queryParams.add(PARAM_QUERY_ID, Converter.serialiseToString(id));

            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("delete", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     * @param   resultType   DOCUMENT ME!
     * @param   isUpdate     DOCUMENT ME!
     * @param   isBatch      DOCUMENT ME!
     * @param   isRoot       DOCUMENT ME!
     * @param   isUnion      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
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
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (name != null) {
                queryParams.add(PARAM_QUERY_NAME, Converter.serialiseToString(name));
            }
            if (description != null) {
                queryParams.add(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (statement != null) {
                queryParams.add(PARAM_STATEMENT, Converter.serialiseToString(statement));
            }

            queryParams.add(PARAM_RESULT_TYPE, Converter.serialiseToString(resultType));
            queryParams.add(PARAM_IS_BATCH, Converter.serialiseToString(isBatch));
            queryParams.add(PARAM_IS_UPDATE, Converter.serialiseToString(isUpdate));
            queryParams.add(PARAM_IS_ROOT, Converter.serialiseToString(isRoot));
            queryParams.add(PARAM_IS_UNION, Converter.serialiseToString(isUnion));

            try {
                return getResponsePOST("addQueryFull", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public int addQuery(final User user, final String name, final String description, final String statement)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (name != null) {
                queryParams.add(PARAM_QUERY_NAME, Converter.serialiseToString(name));
            }
            if (description != null) {
                queryParams.add(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (statement != null) {
                queryParams.add(PARAM_STATEMENT, Converter.serialiseToString(statement));
            }

            try {
                return getResponsePOST("addQuery", queryParams, int.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   queryId        DOCUMENT ME!
     * @param   typeId         DOCUMENT ME!
     * @param   paramkey       DOCUMENT ME!
     * @param   description    DOCUMENT ME!
     * @param   isQueryResult  DOCUMENT ME!
     * @param   queryPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            queryParams.add(PARAM_QUERY_ID, Converter.serialiseToString(queryId));

            if (paramkey != null) {
                queryParams.add(PARAM_PARAM_KEY, Converter.serialiseToString(paramkey));
            }
            if (description != null) {
                queryParams.add(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (typeId >= 0) {
                queryParams.add(PARAM_TYPE_ID, Converter.serialiseToString(typeId));
            }

            queryParams.add(PARAM_QUERY_RESULT, Converter.serialiseToString(isQueryResult));
            queryParams.add(PARAM_QUERY_POSITION, Converter.serialiseToString(queryPosition));

            try {
                return getResponsePOST("addQueryParameterFull", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   queryId      DOCUMENT ME!
     * @param   paramkey     DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addQueryParameter(final User user,
            final int queryId,
            final String paramkey,
            final String description) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            queryParams.add(PARAM_QUERY_ID, Converter.serialiseToString(queryId));

            if (paramkey != null) {
                queryParams.add(PARAM_PARAM_KEY, Converter.serialiseToString(paramkey));
            }
            if (description != null) {
                queryParams.add(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }

            try {
                return getResponsePOST("addQueryParameter", queryParams, boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public HashMap getSearchOptions(final User user) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponsePOST("getSearchOptionsByUser", queryParams, HashMap.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.add(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePOST("getSearchOptionsByDomain", queryParams, HashMap.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   classIds       DOCUMENT ME!
     * @param   searchOptions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public SearchResult search(final User user, final String[] classIds, final SearchOption[] searchOptions)
            throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (classIds != null) {
                queryParams.add(PARAM_CLASS_ID, Converter.serialiseToString(classIds));
            }
            if (searchOptions != null) {
                queryParams.add(PARAM_SEARCH_OPTIONS, Converter.serialiseToString(searchOptions));
            }

            try {
                return getResponsePOST("search", queryParams, SearchResult.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lsName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons(final String lsName) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (lsName != null) {
                queryParams.add(PARAM_LS_NAME, Converter.serialiseToString(lsName));
            }

            try {
                return getResponsePOST("getDefaultIconsByLSName", queryParams, Image[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Image[] getDefaultIcons() throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            try {
                return getResponsePOST("getDefaultIcons", queryParams, Image[].class);
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                if (remEx == null) {
                    throw ex;
                } else {
                    throw remEx;
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Override
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (user != null) {
                queryParams.add(PARAM_USER, Converter.serialiseToString(user));
            }
            if (oldPassword != null) {
                queryParams.add(PARAM_OLD_PASSWORD, Converter.serialiseToString(oldPassword));
            }
            if (newPassword != null) {
                queryParams.add(PARAM_NEW_PASSWORD, Converter.serialiseToString(newPassword));
            }

            try {
                return getResponsePOST("changePassword", queryParams, Boolean.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();
                if (HttpStatus.SC_UNAUTHORIZED == response.getStatus()) {
                    final UserException userEx = ServerExceptionMapper.fromResponse(response, UserException.class);
                    if (userEx == null) {
                        throw ex;
                    } else {
                        throw userEx;
                    }
                } else {
                    final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                    if (remEx == null) {
                        throw ex;
                    } else {
                        throw remEx;
                    }
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
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
            final String password) throws RemoteException, UserException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();

            if (userGroupLsName != null) {
                queryParams.add(PARAM_USERGROUP_LS_NAME, Converter.serialiseToString(userGroupLsName));
            }
            if (userGroupName != null) {
                queryParams.add(PARAM_USERGROUP_NAME, Converter.serialiseToString(userGroupName));
            }
            if (userLsName != null) {
                queryParams.add(PARAM_USER_LS_NAME, Converter.serialiseToString(userLsName));
            }
            if (userName != null) {
                queryParams.add(PARAM_USERNAME, Converter.serialiseToString(userName));
            }
            if (password != null) {
                queryParams.add(PARAM_PASSWORD, Converter.serialiseToString(password));
            }

            try {
                return getResponsePOST("getUser", queryParams, User.class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();
                if (HttpStatus.SC_UNAUTHORIZED == response.getStatus()) {
                    final UserException userEx = ServerExceptionMapper.fromResponse(response, UserException.class);
                    if (userEx == null) {
                        throw ex;
                    } else {
                        throw userEx;
                    }
                } else {
                    final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
                    if (remEx == null) {
                        throw ex;
                    } else {
                        throw remEx;
                    }
                }
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames() throws RemoteException {
        try {
            return getResponsePOST("getUserGroupNames", Vector.class); // NOI18N
        } catch (final UniformInterfaceException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("exception during request, remapping", ex);
            }

            final ClientResponse response = ex.getResponse();

            final RemoteException remEx = ServerExceptionMapper.fromResponse(response, RemoteException.class);
            if (remEx == null) {
                throw ex;
            } else {
                throw remEx;
            }
        } catch (final IOException ex) {
            final String message = "could not convert params"; // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create class";   // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        try {
            final MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
            if (userName != null) {
                queryParams.add(PARAM_USERNAME, Converter.serialiseToString(userName));
            }
            if (lsHome != null) {
                queryParams.add(PARAM_LS_HOME, Converter.serialiseToString(lsHome));
            }

            return getResponsePOST("getUserGroupNamesByUser", queryParams, Vector.class); // NOI18N
        } catch (final IOException ex) {
            final String message = "could not convert params";                            // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create vector";                             // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    // --> SSLConfig
    public static final class SSLConfig {

        //~ Instance fields ----------------------------------------------------

        private transient String keystore;
        private transient String keystorePass;
        private transient boolean useSSL;
        private transient String serverKeystore;
        private transient String serverKeystorePass;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SSLConfig object.
         */
        public SSLConfig() {
        }

        /**
         * Creates a new SSLConfig object.
         *
         * @param  keystore            DOCUMENT ME!
         * @param  keystorePass        DOCUMENT ME!
         * @param  useSSL              DOCUMENT ME!
         * @param  serverKeystore      DOCUMENT ME!
         * @param  serverKeystorePass  DOCUMENT ME!
         */
        public SSLConfig(final String keystore,
                final String keystorePass,
                final boolean useSSL,
                final String serverKeystore,
                final String serverKeystorePass) {
            this.keystore = keystore;
            this.keystorePass = keystorePass;
            this.useSSL = useSSL;
            this.serverKeystore = serverKeystore;
            this.serverKeystorePass = serverKeystorePass;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getKeystore() {
            return keystore;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  keystore  DOCUMENT ME!
         */
        public void setKeystore(final String keystore) {
            this.keystore = keystore;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getKeystorePass() {
            return keystorePass;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  keystorePass  DOCUMENT ME!
         */
        public void setKeystorePass(final String keystorePass) {
            this.keystorePass = keystorePass;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getServerKeystore() {
            return serverKeystore;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  serverKeystore  DOCUMENT ME!
         */
        public void setServerKeystore(final String serverKeystore) {
            this.serverKeystore = serverKeystore;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getServerKeystorePass() {
            return serverKeystorePass;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  serverKeystorePass  DOCUMENT ME!
         */
        public void setServerKeystorePass(final String serverKeystorePass) {
            this.serverKeystorePass = serverKeystorePass;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isUseSSL() {
            return useSSL;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  useSSL  DOCUMENT ME!
         */
        public void setUseSSL(final boolean useSSL) {
            this.useSSL = useSSL;
        }
    }
}
