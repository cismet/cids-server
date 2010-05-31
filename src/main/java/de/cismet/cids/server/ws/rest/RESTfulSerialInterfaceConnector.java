/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.dataretrieval.DataObject;
import Sirius.server.dataretrieval.DataRetrievalException;
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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ws.Converter;

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

    //~ Instance fields --------------------------------------------------------

    private final transient String rootResource;
    private final transient Map<String, Client> clientCache;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterfaceConnector object.
     *
     * @param  rootResource  DOCUMENT ME!
     */
    public RESTfulSerialInterfaceConnector(final String rootResource) {
        // add training '/' to the root resource if not present
        if ('/' == rootResource.charAt(rootResource.length() - 1)) {
            this.rootResource = rootResource;
        } else {
            this.rootResource = rootResource + "/"; // NOI18N
        }

        clientCache = new Hashtable<String, Client>();
    }

    //~ Methods ----------------------------------------------------------------

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
            clientCache.put(path, Client.create());
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
        return wr.type(MediaType.APPLICATION_OCTET_STREAM_TYPE).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE);
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
    private <T> T getResponseGET(final String path, final Class<T> type) throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path);

        return getResponseGET(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>          DOCUMENT ME!
     * @param   path         DOCUMENT ME!
     * @param   queryParams  DOCUMENT ME!
     * @param   type         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponseGET(final String path, final Map<String, String> queryParams, final Class<T> type)
            throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path, queryParams);

        return getResponseGET(builder, type);
    }

    /**
     * Extracts the real return value from the web service call's response.
     *
     * @param   <T>      Type of the response value
     * @param   builder  initialised {@link WebResouce.Builder}
     * @param   type     Class of the return type
     *
     * @return  the extracted response of type <code>T</code>
     *
     * @throws  IOException             if any error occurs during response conversion
     * @throws  ClassNotFoundException  if the class to convert in cannot be found
     * @throws  IllegalStateException   DOCUMENT ME!
     */
    private <T> T getResponseGET(final WebResource.Builder builder, final Class<T> type) throws IOException,
        ClassNotFoundException {
        if ((builder == null) || (type == null)) {
            throw new IllegalStateException("neither builder nor type may be null"); // NOI18N
        }

        final byte[] bytes = builder.get(byte[].class);

        return Converter.deserialiseFromBase64(bytes, type);
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

        return getResponsePOST(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>          DOCUMENT ME!
     * @param   path         DOCUMENT ME!
     * @param   queryParams  DOCUMENT ME!
     * @param   type         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponsePOST(final String path, final Map<String, String> queryParams, final Class<T> type)
            throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path, queryParams);

        return getResponsePOST(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>      DOCUMENT ME!
     * @param   builder  DOCUMENT ME!
     * @param   type     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  IllegalStateException   DOCUMENT ME!
     */
    private <T> T getResponsePOST(final WebResource.Builder builder, final Class<T> type) throws IOException,
        ClassNotFoundException {
        if ((builder == null) || (type == null)) {
            throw new IllegalStateException("neither builder nor type may be null"); // NOI18N
        }

        final byte[] bytes = builder.post(byte[].class);

        return Converter.deserialiseFromBase64(bytes, type);
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
    private <T> T getResponsePUT(final String path, final Class<T> type) throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path);

        return getResponsePUT(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>          DOCUMENT ME!
     * @param   path         DOCUMENT ME!
     * @param   queryParams  DOCUMENT ME!
     * @param   type         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponsePUT(final String path, final Map<String, String> queryParams, final Class<T> type)
            throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path, queryParams);

        return getResponsePUT(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>      DOCUMENT ME!
     * @param   builder  DOCUMENT ME!
     * @param   type     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  IllegalStateException   DOCUMENT ME!
     */
    private <T> T getResponsePUT(final WebResource.Builder builder, final Class<T> type) throws IOException,
        ClassNotFoundException {
        if ((builder == null) || (type == null)) {
            throw new IllegalStateException("neither builder nor type may be null"); // NOI18N
        }

        final byte[] bytes = builder.put(byte[].class);

        return Converter.deserialiseFromBase64(bytes, type);
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
    private <T> T getResponseDELETE(final String path, final Class<T> type) throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path);

        return getResponseDELETE(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>          DOCUMENT ME!
     * @param   path         DOCUMENT ME!
     * @param   queryParams  DOCUMENT ME!
     * @param   type         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private <T> T getResponseDELETE(final String path, final Map<String, String> queryParams, final Class<T> type)
            throws IOException, ClassNotFoundException {
        final WebResource.Builder builder = createWebResourceBuilder(path, queryParams);

        return getResponseDELETE(builder, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>      DOCUMENT ME!
     * @param   builder  DOCUMENT ME!
     * @param   type     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     * @throws  IllegalStateException   DOCUMENT ME!
     */
    private <T> T getResponseDELETE(final WebResource.Builder builder, final Class<T> type) throws IOException,
        ClassNotFoundException {
        if ((builder == null) || (type == null)) {
            throw new IllegalStateException("neither builder nor type may be null"); // NOI18N
        }

        final byte[] bytes = builder.delete(byte[].class);

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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domainName != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domainName));
            }

            try {
                return getResponseGET("getRootsByDomain", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseGET("getRoots", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (node != null) {
                queryParams.put(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }

            try {
                return getResponseGET("getChildren", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (node != null) {
                queryParams.put(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (parent != null) {
                queryParams.put(PARAM_LINK_PARENT, Converter.serialiseToString(parent));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (node != null) {
                queryParams.put(PARAM_NODE, Converter.serialiseToString(node));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseDELETE("deleteNode", queryParams, boolean.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (from != null) {
                queryParams.put(PARAM_NODE_FROM, Converter.serialiseToString(from));
            }
            if (to != null) {
                queryParams.put(PARAM_NODE_TO, Converter.serialiseToString(to));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (from != null) {
                queryParams.put(PARAM_NODE_FROM, Converter.serialiseToString(from));
            }
            if (to != null) {
                queryParams.put(PARAM_NODE_TO, Converter.serialiseToString(to));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseDELETE("deleteLink", queryParams, boolean.class); // NOI18N
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
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public DataObject getDataObject(final User user, final MetaObject metaObject) throws RemoteException {
        try {
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.put(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }

            try {
                return getResponseGET("getDataObjectByMetaObject", queryParams, DataObject.class); // NOI18N
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
     * @param   user   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    @Override
    public DataObject[] getDataObject(final User user, final Query query) throws RemoteException,
        DataRetrievalException {
        try {
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponseGET("getDataObjectByQuery", queryParams, DataObject[].class); // NOI18N
            } catch (final UniformInterfaceException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception during request, remapping", ex);
                }

                final ClientResponse response = ex.getResponse();

                if(HttpStatus.SC_NOT_FOUND == response.getStatus()) {
                    final DataRetrievalException dataEx = ServerExceptionMapper.fromResponse(response, DataRetrievalException.class);
                    if (dataEx == null) {
                        throw ex;
                    } else {
                        throw dataEx;
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
    public String[] getDomains() throws RemoteException {
        try {
            return getResponseGET("getDomains", String[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (nodeID >= 0) {
                queryParams.put(PARAM_NODE_ID, Converter.serialiseToString(nodeID));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getMetaObjectNodeByID", queryParams, Node.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponseGET("getMetaObjectNodeByString", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponseGET("getMetaObjectNodeByQuery", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponseGET("getMetaObjectByString", queryParams, MetaObject[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }

            try {
                return getResponseGET("getMetaObjectByQuery", queryParams, MetaObject[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(4, 1);

            if (usr != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(usr));
            }
            if (objectID != -1) {
                queryParams.put(PARAM_OBJECT_ID, Converter.serialiseToString(objectID));
            }
            if (classID != -1) {
                queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classID));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getMetaObjectByID", queryParams, MetaObject.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.put(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.put(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePUT("updateMetaObject", queryParams, int.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (metaObject != null) {
                queryParams.put(PARAM_METAOBJECT, Converter.serialiseToString(metaObject));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseDELETE("deleteMetaObject", queryParams, int.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponsePUT("update", queryParams, int.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (c != null) {
                queryParams.put(PARAM_METACLASS, Converter.serialiseToString(c));
            }

            try {
                return getResponseGET("getInstance", queryParams, MetaObject.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (tableName != null) {
                queryParams.put(PARAM_TABLE_NAME, Converter.serialiseToString(tableName));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getClassByTableName", queryParams, MetaClass.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }
            if (classID >= 0) {
                queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classID));
            }

            try {
                return getResponseGET("getClassByID", queryParams, MetaClass.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getClasses", queryParams, MetaClass[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseGET("getClassTreeNodesByUser", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getClassTreeNodesByDomain", queryParams, Node[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseGET("getMethodsByUser", queryParams, MethodMap.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (localServerName != null) {
                queryParams.put(PARAM_LOCAL_SERVER_NAME, Converter.serialiseToString(localServerName));
            }

            try {
                return getResponseGET("getMethodsByDomain", queryParams, MethodMap.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(4, 1);

            if (classId >= 0) {
                queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classId));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (representationFields != null) {
                queryParams.put(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }
            if (representationPattern != null) {
                queryParams.put(PARAM_REP_PATTERN, Converter.serialiseToString(representationPattern));
            }

            try {
                return getResponseGET(
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (classId >= 0) {
                queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classId));
            }
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (representationFields != null) {
                queryParams.put(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }

            try {
                return getResponseGET(
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(5, 1);

            queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classId));
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (representationFields != null) {
                queryParams.put(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }
            if (representationPattern != null) {
                queryParams.put(PARAM_REP_PATTERN, Converter.serialiseToString(representationPattern));
            }

            try {
                return getResponseGET(
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(5, 1);

            queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classId));
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (query != null) {
                queryParams.put(PARAM_QUERY, Converter.serialiseToString(query));
            }
            if (representationFields != null) {
                queryParams.put(PARAM_REP_FIELDS, Converter.serialiseToString(representationFields));
            }

            try {
                return getResponseGET(
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (data != null) {
                queryParams.put(PARAM_QUERY_DATA, Converter.serialiseToString(data));
            }

            try {
                return getResponsePUT("storeQuery", queryParams, boolean.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseGET("getQueryInfosByUser", queryParams, Info[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (userGroup != null) {
                queryParams.put(PARAM_USERGROUP, Converter.serialiseToString(userGroup));
            }

            try {
                return getResponseGET("getQueryInfosByUserGroup", queryParams, Info[].class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (id >= 0) {
                queryParams.put(PARAM_QUERY_ID, Converter.serialiseToString(id));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getQuery", queryParams, QueryData.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (id >= 0) {
                queryParams.put(PARAM_QUERY_ID, Converter.serialiseToString(id));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseDELETE("delete", queryParams, boolean.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(9, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (name != null) {
                queryParams.put(PARAM_QUERY_NAME, Converter.serialiseToString(name));
            }
            if (description != null) {
                queryParams.put(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (statement != null) {
                queryParams.put(PARAM_STATEMENT, Converter.serialiseToString(statement));
            }
            if (resultType >= 0) {
                queryParams.put(PARAM_RESULT_TYPE, Converter.serialiseToString(resultType));
            }
            if (('f' == isBatch) || ('t' == isBatch)
                        || ('1' == isBatch)
                        || ('0' == isBatch)) {
                queryParams.put(PARAM_IS_BATCH, Converter.serialiseToString(isBatch));
            }
            if (('f' == isUpdate) || ('t' == isUpdate)
                        || ('1' == isUpdate)
                        || ('0' == isUpdate)) {
                queryParams.put(PARAM_IS_UPDATE, Converter.serialiseToString(isUpdate));
            }
            if (('f' == isRoot) || ('t' == isRoot)
                        || ('1' == isRoot)
                        || ('0' == isRoot)) {
                queryParams.put(PARAM_IS_ROOT, Converter.serialiseToString(isRoot));
            }
            if (('f' == isUnion) || ('t' == isUnion)
                        || ('1' == isUnion)
                        || ('0' == isUnion)) {
                queryParams.put(PARAM_IS_UNION, Converter.serialiseToString(isUnion));
            }

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
            final HashMap<String, String> queryParams = new HashMap<String, String>(4, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (name != null) {
                queryParams.put(PARAM_QUERY_NAME, Converter.serialiseToString(name));
            }
            if (description != null) {
                queryParams.put(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (statement != null) {
                queryParams.put(PARAM_STATEMENT, Converter.serialiseToString(statement));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(7, 1);
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (queryId >= 0) {
                queryParams.put(PARAM_QUERY_ID, Converter.serialiseToString(queryId));
            }
            if (paramkey != null) {
                queryParams.put(PARAM_PARAM_KEY, Converter.serialiseToString(paramkey));
            }
            if (description != null) {
                queryParams.put(PARAM_DESCRIPTION, Converter.serialiseToString(description));
            }
            if (typeId >= 0) {
                queryParams.put(PARAM_TYPE_ID, Converter.serialiseToString(typeId));
            }
            if (('f' == isQueryResult) || ('t' == isQueryResult)
                        || ('1' == isQueryResult)
                        || ('0' == isQueryResult)) {
                queryParams.put(PARAM_QUERY_RESULT, Converter.serialiseToString(isQueryResult));
            }
            if (queryPosition != -1) {
                queryParams.put(PARAM_QUERY_POSITION, Converter.serialiseToString(queryPosition));
            }

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
            final HashMap<String, String> queryParams = new HashMap<String, String>(4, 1);
            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (queryId >= 0) {
                queryParams.put(PARAM_QUERY_ID, Converter.serialiseToString(queryId));
            }
            if (paramkey != null) {
                queryParams.put(PARAM_PARAM_KEY, Converter.serialiseToString(paramkey));
            }
            if (description != null) {
                queryParams.put(PARAM_DESCRIPTION, Converter.serialiseToString(description));
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }

            try {
                return getResponseGET("getSearchOptionsByUser", queryParams, HashMap.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (domain != null) {
                queryParams.put(PARAM_DOMAIN, Converter.serialiseToString(domain));
            }

            try {
                return getResponseGET("getSearchOptionsByDomain", queryParams, HashMap.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (classIds != null) {
                queryParams.put(PARAM_CLASS_ID, Converter.serialiseToString(classIds));
            }
            if (searchOptions != null) {
                queryParams.put(PARAM_SEARCH_OPTIONS, Converter.serialiseToString(searchOptions));
            }

            try {
                return getResponseGET("search", queryParams, SearchResult.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(1, 1);

            if (lsName != null) {
                queryParams.put(PARAM_LS_NAME, Converter.serialiseToString(lsName));
            }

            try {
                return getResponseGET("getDefaultIconsByLSName", queryParams, Image[].class);
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(0, 1);

            try {
                return getResponseGET("getDefaultIcons", queryParams, Image[].class);
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(3, 1);

            if (user != null) {
                queryParams.put(PARAM_USER, Converter.serialiseToString(user));
            }
            if (oldPassword != null) {
                queryParams.put(PARAM_OLD_PASSWORD, Converter.serialiseToString(oldPassword));
            }
            if (newPassword != null) {
                queryParams.put(PARAM_NEW_PASSWORD, Converter.serialiseToString(newPassword));
            }

            try {
                return getResponsePUT("changePassword", queryParams, Boolean.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(5, 1);

            if (userGroupLsName != null) {
                queryParams.put(PARAM_USERGROUP_LS_NAME, Converter.serialiseToString(userGroupLsName));
            }
            if (userGroupName != null) {
                queryParams.put(PARAM_USERGROUP_NAME, Converter.serialiseToString(userGroupName));
            }
            if (userLsName != null) {
                queryParams.put(PARAM_USER_LS_NAME, Converter.serialiseToString(userLsName));
            }
            if (userName != null) {
                queryParams.put(PARAM_USERNAME, Converter.serialiseToString(userName));
            }
            if (password != null) {
                queryParams.put(PARAM_PASSWORD, Converter.serialiseToString(password));
            }

            try {
                return getResponseGET("getUser", queryParams, User.class); // NOI18N
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
            return getResponseGET("getUserGroupNames", Vector.class); // NOI18N
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
            final HashMap<String, String> queryParams = new HashMap<String, String>(2, 1);
            if (userName != null) {
                queryParams.put(PARAM_USERNAME, Converter.serialiseToString(userName));
            }
            if (lsHome != null) {
                queryParams.put(PARAM_LS_HOME, Converter.serialiseToString(lsHome));
            }

            return getResponseGET("getUserGroupNamesByUser", queryParams, Vector.class); // NOI18N
        } catch (final IOException ex) {
            final String message = "could not convert params";                           // NOI18N
            LOG.error(message, ex);
            throw new RemoteException(message, ex);
        } catch (final ClassNotFoundException e) {
            final String message = "could not create vector";                            // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }
}
