/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.store.QueryData;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ws.Converter;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
@Path("/callserver/binary") // NOI18N
public final class RESTfulSerialInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RESTfulSerialInterface.class);
    public static final String PARAM_USERGROUP_LS_NAME = "ugLsName";        // NOI18N
    public static final String PARAM_USERGROUP_NAME = "ugName";             // NOI18N
    public static final String PARAM_USER_LS_NAME = "uLsName";              // NOI18N
    public static final String PARAM_USERNAME = "uname";                    // NOI18N
    public static final String PARAM_PASSWORD = "password";                 // NOI18N
    public static final String PARAM_LS_HOME = "lsHome";                    // NOI18N
    public static final String PARAM_USER = "user";                         // NOI18N
    public static final String PARAM_OLD_PASSWORD = "old_password";         // NOI18N
    public static final String PARAM_NEW_PASSWORD = "new_password";         // NOI18N
    public static final String PARAM_CLASS_ID = "classIds";                 // NOI18N
    public static final String PARAM_LS_NAME = "lsName";                    // NOI18N
    public static final String PARAM_SEARCH_OPTIONS = "searchOptions";      // NOI18N
    public static final String PARAM_DOMAIN = "domain";                     // NOI18N
    public static final String PARAM_QUERY_ID = "queryID";                  // NOI18N
    public static final String PARAM_PARAM_KEY = "paramKey";                // NOI18N
    public static final String PARAM_DESCRIPTION = "description";           // NOI18N
    public static final String PARAM_TYPE_ID = "typeId";                    // NOI18N
    public static final String PARAM_QUERY = "query";                       // NOI18N
    public static final String PARAM_QUERY_RESULT = "queryResult";          // NOI18N
    public static final String PARAM_QUERY_POSITION = "queryPosition";      // NOI18N
    public static final String PARAM_QUERY_NAME = "queryName";              // NOI18N
    public static final String PARAM_STATEMENT = "statement";               // NOI18N
    public static final String PARAM_RESULT_TYPE = "resultType";            // NOI18N
    public static final String PARAM_IS_UPDATE = "isUpdate";                // NOI18N
    public static final String PARAM_IS_BATCH = "isBatch";                  // NOI18N
    public static final String PARAM_IS_ROOT = "isRoot";                    // NOI18N
    public static final String PARAM_IS_UNION = "isUnion";                  // NOI18N
    public static final String PARAM_USERGROUP = "userGroup";               // NOI18N
    public static final String PARAM_QUERY_DATA = "queryData";              // NOI18N
    public static final String PARAM_REP_FIELDS = "representationFields";   // NOI18N
    public static final String PARAM_REP_PATTERN = "representationPatter";  // NOI18N
    public static final String PARAM_LOCAL_SERVER_NAME = "localServerName"; // NOI18N
    public static final String PARAM_TABLE_NAME = "tableName";              // NOI18N
    public static final String PARAM_METAOBJECT = "metaObject";             // NOI18N
    public static final String PARAM_METACLASS = "metaClass";               // NOI18N
    public static final String PARAM_OBJECT_ID = "objectID";                // NOI18N
    public static final String PARAM_NODE_FROM = "fromNode";                // NOI18N
    public static final String PARAM_NODE_TO = "toNode";                    // NOI18N
    public static final String PARAM_NODE = "node";                         // NOI18N
    public static final String PARAM_LINK_PARENT = "linkParent";            // NOI18N
    public static final String PARAM_NODE_ID = "nodeID";                    // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient CallServerService callserver;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterface object.
     */
    public RESTfulSerialInterface() {
        callserver = StartProxy.getInstance().getCallServer();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private Response createResponse(final Object o) throws IOException {
        return Response.ok(Converter.serialiseToBase64(o)).build();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes        user DOCUMENT ME!
     * @param   domainNameBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @GET
    @Path("/getRootsByDomain")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRootsByDomain(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainNameBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainNameBytes, String.class);

            return createResponse(callserver.getRoots(user, domain));
        } catch (final IOException e) {
            final String message = "could not get roots"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get roots"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getRoots")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRoots(@QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.getRoots(user));
        } catch (final IOException e) {
            final String message = "could not get roots"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get roots"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeBytes  node DOCUMENT ME!
     * @param   usrBytes   usr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @GET
    @Path("/getChildren")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getChildren(@QueryParam(PARAM_NODE) final String nodeBytes,
            @QueryParam(PARAM_USER) final String usrBytes) throws RemoteException {
        try {
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class);
            final User user = Converter.deserialiseFromString(usrBytes, User.class);

            return createResponse(callserver.getChildren(node, user));
        } catch (final IOException e) {
            final String message = "could not get children"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get children"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeBytes    node DOCUMENT ME!
     * @param   parentBytes  DOCUMENT ME!
     * @param   userBytes    user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/addNode")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addNode(@QueryParam(PARAM_NODE) final String nodeBytes,
            @QueryParam(PARAM_LINK_PARENT) final String parentBytes,
            @QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class);
            final Link parent = Converter.deserialiseFromString(parentBytes, Link.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.addNode(node, parent, user));
        } catch (final IOException e) {
            final String message = "could not add node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not add node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeBytes  node DOCUMENT ME!
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @DELETE
    @Path("/deleteNode")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteNode(@QueryParam(PARAM_NODE) final String nodeBytes,
            @QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.deleteNode(node, user));
        } catch (final IOException e) {
            final String message = "could not delete node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not delete node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fromBytes  from DOCUMENT ME!
     * @param   toBytes    to DOCUMENT ME!
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/addLink")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addLink(@QueryParam(PARAM_NODE_FROM) final String fromBytes,
            @QueryParam(PARAM_NODE_TO) final String toBytes,
            @QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final Node from = Converter.deserialiseFromString(fromBytes, Node.class);
            final Node to = Converter.deserialiseFromString(toBytes, Node.class);

            return createResponse(callserver.addLink(from, to, user));
        } catch (final IOException e) {
            final String message = "could not add link"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not add link"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fromBytes  from DOCUMENT ME!
     * @param   toBytes    to DOCUMENT ME!
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @DELETE
    @Path("/deleteLink")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteLink(@QueryParam(PARAM_NODE_FROM) final String fromBytes,
            @QueryParam(PARAM_NODE_TO) final String toBytes,
            @QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final Node from = Converter.deserialiseFromString(fromBytes, Node.class);
            final Node to = Converter.deserialiseFromString(toBytes, Node.class);

            return createResponse(callserver.deleteLink(from, to, user));
        } catch (final IOException e) {
            final String message = "could not delete link"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not delete link"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getDomains")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDomains() throws RemoteException {
        try {
            return createResponse(callserver.getDomains());
        } catch (final IOException e) {
            final String message = "could not get domains"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    usr DOCUMENT ME!
     * @param   nodeIDBytes  DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectNodeByID")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNode(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_NODE_ID) final String nodeIDBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int nodeID = Converter.deserialiseFromString(nodeIDBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getMetaObjectNode(user, nodeID, domain));
        } catch (final IOException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usrBytes    usr DOCUMENT ME!
     * @param   queryBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectNodeByString")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNodeByString(@QueryParam(PARAM_USER) final String usrBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);

            return createResponse(callserver.getMetaObjectNode(user, query));
        } catch (final IOException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usrBytes    usr DOCUMENT ME!
     * @param   queryBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectNodeByQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNodeByQuery(@QueryParam(PARAM_USER) final String usrBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class);
            final Query query = Converter.deserialiseFromString(queryBytes, Query.class);

            return createResponse(callserver.getMetaObjectNode(user, query));
        } catch (final IOException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject node"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usrBytes    usr DOCUMENT ME!
     * @param   queryBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectByString")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByString(@QueryParam(PARAM_USER) final String usrBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);

            return createResponse(callserver.getMetaObject(user, query));
        } catch (final IOException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usrBytes    usr DOCUMENT ME!
     * @param   queryBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectByQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByQuery(@QueryParam(PARAM_USER) final String usrBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class);
            final Query query = Converter.deserialiseFromString(queryBytes, Query.class);

            return createResponse(callserver.getMetaObject(user, query));
        } catch (final IOException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes      usr DOCUMENT ME!
     * @param   objectIDBytes  DOCUMENT ME!
     * @param   classIDBytes   DOCUMENT ME!
     * @param   domainBytes    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMetaObjectByID")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObject(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_OBJECT_ID) final String objectIDBytes,
            @QueryParam(PARAM_CLASS_ID) final String classIDBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int objectID = Converter.deserialiseFromString(objectIDBytes, int.class);
            final int classID = Converter.deserialiseFromString(classIDBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getMetaObject(user, objectID, classID, domain));
        } catch (final IOException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/insertMetaObject")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response insertMetaObject(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaObject metaObject = Converter.deserialiseFromString(metaObjectBytes, MetaObject.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.insertMetaObject(user, metaObject, domain));
        } catch (final IOException e) {
            final String message = "could not insert metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not insert metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    user DOCUMENT ME!
     * @param   queryBytes   DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/insertMetaObjectByQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response insertMetaObjectByQuery(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final Query query = Converter.deserialiseFromString(queryBytes, Query.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.insertMetaObject(user, query, domain));
        } catch (final IOException e) {
            final String message = "could not insert metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not insert metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @PUT
    @Path("/updateMetaObject")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response updateMetaObject(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaObject metaObject = Converter.deserialiseFromString(metaObjectBytes, MetaObject.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.updateMetaObject(user, metaObject, domain));
        } catch (final IOException e) {
            final String message = "could not update metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not update metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @DELETE
    @Path("/deleteMetaObject")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteMetaObject(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaObject metaObject = Converter.deserialiseFromString(metaObjectBytes, MetaObject.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.deleteMetaObject(user, metaObject, domain));
        } catch (final IOException e) {
            final String message = "could not delete metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not delete metaobject"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    user DOCUMENT ME!
     * @param   queryBytes   DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response update(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.update(user, query, domain));
        } catch (final IOException e) {
            final String message = "could not update"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not update"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes       user DOCUMENT ME!
     * @param   metaClassBytes  c DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getInstance")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getInstance(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_METACLASS) final String metaClassBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaClass metaClass = Converter.deserialiseFromString(metaClassBytes, MetaClass.class);

            return createResponse(callserver.getInstance(user, metaClass));
        } catch (final IOException e) {
            final String message = "could not get instance"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get instance"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes       user DOCUMENT ME!
     * @param   tableNameBytes  DOCUMENT ME!
     * @param   domainBytes     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getClassByTableName")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassByTableName(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_TABLE_NAME) final String tableNameBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String tableName = Converter.deserialiseFromString(tableNameBytes, String.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getClassByTableName(user, tableName, domain));
        } catch (final IOException e) {
            final String message = "could not get metaclass"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaclass"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes     user DOCUMENT ME!
     * @param   classIdBytes  classID DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getClassByID")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClass(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_CLASS_ID) final String classIdBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getClass(user, classId, domain));
        } catch (final IOException e) {
            final String message = "could not get metaclass"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaclass"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    user DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getClasses")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClasses(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getClasses(user, domain));
        } catch (final IOException e) {
            final String message = "could not get metaclasses"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get metaclasses"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getClassTreeNodesByUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.getClassTreeNodes(user));
        } catch (final IOException e) {
            final String message = "could not get classtree nodes"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get classtree nodes"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    user DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getClassTreeNodesByDomain")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getClassTreeNodes(user, domain));
        } catch (final IOException e) {
            final String message = "could not get classtree nodes"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get classtree nodes"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMethodsByUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethodsByUser(@QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.getMethods(user));
        } catch (final IOException e) {
            final String message = "could not get methods"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get methods"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes             user DOCUMENT ME!
     * @param   localServerNameBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getMethodsByDomain")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethods(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_LOCAL_SERVER_NAME) final String localServerNameBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String localServerName = Converter.deserialiseFromString(localServerNameBytes, String.class);

            return createResponse(callserver.getMethods(user, localServerName));
        } catch (final IOException e) {
            final String message = "could not get methods"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get methods"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIdBytes                DOCUMENT ME!
     * @param   userBytes                   user DOCUMENT ME!
     * @param   representationFieldsBytes   DOCUMENT ME!
     * @param   representationPatternBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getAllLightweightMetaObjectsForClassByPattern")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClassWithPattern(
            @QueryParam(PARAM_CLASS_ID) final String classIdBytes,
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @QueryParam(PARAM_REP_PATTERN) final String representationPatternBytes) throws RemoteException {
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class);
            final String representationPattern = Converter.deserialiseFromString(
                    representationPatternBytes,
                    String.class);

            return createResponse(callserver.getAllLightweightMetaObjectsForClass(
                        classId,
                        user,
                        representationFields,
                        representationPattern));
        } catch (final IOException e) {
            final String message = "could not get LightwightMetaObjects for class";  // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get LightweightMetaObjects for class"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIdBytes               DOCUMENT ME!
     * @param   userBytes                  user DOCUMENT ME!
     * @param   representationFieldsBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getAllLightweightMetaObjectsForClass")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClass(@QueryParam(PARAM_CLASS_ID) final String classIdBytes,
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_REP_FIELDS) final String representationFieldsBytes) throws RemoteException {
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class);

            return createResponse(callserver.getAllLightweightMetaObjectsForClass(
                        classId,
                        user,
                        representationFields));
        } catch (final IOException e) {
            final String message = "could not get LightweightMetaObjects for class"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get LightweightMetaObjects for class"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIdBytes                DOCUMENT ME!
     * @param   userBytes                   user DOCUMENT ME!
     * @param   queryBytes                  DOCUMENT ME!
     * @param   representationFieldsBytes   DOCUMENT ME!
     * @param   representationPatternBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getLightweightMetaObjectsByQueryAndPattern")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQueryAndPattern(@QueryParam(PARAM_CLASS_ID) final String classIdBytes,
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes,
            @QueryParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @QueryParam(PARAM_REP_PATTERN) final String representationPatternBytes) throws RemoteException {
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class);
            final String representationPattern = Converter.deserialiseFromString(
                    representationPatternBytes,
                    String.class);

            return createResponse(callserver.getLightweightMetaObjectsByQuery(
                        classId,
                        user,
                        query,
                        representationFields,
                        representationPattern));
        } catch (final IOException e) {
            final String message = "could not get LightweightMetaObjects"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get LightWeightMetaObjects"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIdBytes               DOCUMENT ME!
     * @param   userBytes                  user DOCUMENT ME!
     * @param   queryBytes                 DOCUMENT ME!
     * @param   representationFieldsBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getLightweightMetaObjectsByQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQuery(@QueryParam(PARAM_CLASS_ID) final String classIdBytes,
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY) final String queryBytes,
            @QueryParam(PARAM_REP_FIELDS) final String representationFieldsBytes) throws RemoteException {
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class);

            return createResponse(callserver.getLightweightMetaObjectsByQuery(
                        classId,
                        user,
                        query,
                        representationFields));
        } catch (final IOException e) {
            final String message = "could not get LightweightMetaObjects"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get LightweightMetaObjects"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes       user DOCUMENT ME!
     * @param   queryDataBytes  data DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @PUT
    @Path("/storeQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response storeQuery(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_DATA) final String queryDataBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final QueryData data = Converter.deserialiseFromString(queryDataBytes, QueryData.class);

            return createResponse(callserver.storeQuery(user, data));
        } catch (final IOException e) {
            final String message = "could not store query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not store query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getQueryInfosByUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQueryInfosByUser(@QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.getQueryInfos(user));
        } catch (final IOException e) {
            final String message = "could not get query infos"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get query infos"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getQueryInfosByUserGroup")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQueryInfosByUserGroup(@QueryParam(PARAM_USERGROUP) final String userGroupBytes)
            throws RemoteException {
        try {
            final UserGroup userGroup = Converter.deserialiseFromString(userGroupBytes, UserGroup.class);

            return createResponse(callserver.getQueryInfos(userGroup));
        } catch (final IOException e) {
            final String message = "could not get query infos"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get query infos"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   idBytes      id DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQuery(@QueryParam(PARAM_QUERY_ID) final String idBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final int id = Converter.deserialiseFromString(idBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getQuery(id, domain));
        } catch (final IOException e) {
            final String message = "could not get query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   idBytes      id DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response delete(@QueryParam(PARAM_QUERY_ID) final String idBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final int id = Converter.deserialiseFromString(idBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.delete(id, domain));
        } catch (final IOException e) {
            final String message = "could not delete query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not delete query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         user DOCUMENT ME!
     * @param   nameBytes         name DOCUMENT ME!
     * @param   descriptionBytes  DOCUMENT ME!
     * @param   statementBytes    DOCUMENT ME!
     * @param   resultTypeBytes   DOCUMENT ME!
     * @param   isUpdateBytes     DOCUMENT ME!
     * @param   isBatchBytes      DOCUMENT ME!
     * @param   isRootBytes       DOCUMENT ME!
     * @param   isUnionBytes      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/addQueryFull")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryFull(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_NAME) final String nameBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @QueryParam(PARAM_STATEMENT) final String statementBytes,
            @QueryParam(PARAM_RESULT_TYPE) final String resultTypeBytes,
            @QueryParam(PARAM_IS_UPDATE) final String isUpdateBytes,
            @QueryParam(PARAM_IS_BATCH) final String isBatchBytes,
            @QueryParam(PARAM_IS_ROOT) final String isRootBytes,
            @QueryParam(PARAM_IS_UNION) final String isUnionBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String name = Converter.deserialiseFromString(nameBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
            final String statement = Converter.deserialiseFromString(statementBytes, String.class);
            final int resultType = Converter.deserialiseFromString(resultTypeBytes, int.class);
            final char isUpdate = Converter.deserialiseFromString(isUpdateBytes, char.class);
            final char isBatch = Converter.deserialiseFromString(isBatchBytes, char.class);
            final char isRoot = Converter.deserialiseFromString(isRootBytes, char.class);
            final char isUnion = Converter.deserialiseFromString(isUnionBytes, char.class);

            return createResponse(callserver.addQuery(
                        user,
                        name,
                        description,
                        statement,
                        resultType,
                        isUpdate,
                        isBatch,
                        isRoot,
                        isUnion));
        } catch (final IOException e) {
            final String message = "could not add query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not add query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         user DOCUMENT ME!
     * @param   nameBytes         name DOCUMENT ME!
     * @param   descriptionBytes  DOCUMENT ME!
     * @param   statementBytes    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/addQuery")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQuery(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_NAME) final String nameBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @QueryParam(PARAM_STATEMENT) final String statementBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String name = Converter.deserialiseFromString(nameBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
            final String statement = Converter.deserialiseFromString(statementBytes, String.class);

            return createResponse(callserver.addQuery(user, name, description, statement));
        } catch (final IOException e) {
            final String message = "could not add query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not add query"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes           user DOCUMENT ME!
     * @param   queryIdBytes        DOCUMENT ME!
     * @param   typeIdBytes         DOCUMENT ME!
     * @param   paramkeyBytes       DOCUMENT ME!
     * @param   descriptionBytes    DOCUMENT ME!
     * @param   isQueryResultBytes  DOCUMENT ME!
     * @param   queryPositionBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/addQueryParameterFull")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryParameterFull(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_ID) final String queryIdBytes,
            @QueryParam(PARAM_TYPE_ID) final String typeIdBytes,
            @QueryParam(PARAM_PARAM_KEY) final String paramkeyBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @QueryParam(PARAM_QUERY_RESULT) final String isQueryResultBytes,
            @QueryParam(PARAM_QUERY_POSITION) final String queryPositionBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int queryId = Converter.deserialiseFromString(queryIdBytes, int.class);
            final String paramkey = Converter.deserialiseFromString(paramkeyBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
            final int typeId = Converter.deserialiseFromString(typeIdBytes, int.class);
            final char isQueryResult = Converter.deserialiseFromString(isQueryResultBytes, char.class);
            final int queryPosition = Converter.deserialiseFromString(queryPositionBytes, int.class);

            return createResponse(callserver.addQueryParameter(
                        user,
                        queryId,
                        typeId,
                        paramkey,
                        description,
                        isQueryResult,
                        queryPosition));
        } catch (final IOException e) {
            final String message = "could not add query parameter"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not add query parameter"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         user DOCUMENT ME!
     * @param   queryIdBytes      DOCUMENT ME!
     * @param   paramkeyBytes     DOCUMENT ME!
     * @param   descriptionBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/addQueryParameter")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryParameter(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_ID) final String queryIdBytes,
            @QueryParam(PARAM_PARAM_KEY) final String paramkeyBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int queryID = Converter.deserialiseFromString(queryIdBytes, int.class);
            final String paramkey = Converter.deserialiseFromString(paramkeyBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);

            return createResponse(callserver.addQueryParameter(user, queryID, paramkey, description));
        } catch (final IOException e) {
            final String message = "could not add query parameter"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get user";            // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getSearchOptionsByUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSearchOptions(@QueryParam(PARAM_USER) final String userBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            return createResponse(callserver.getSearchOptions(user));
        } catch (final IOException e) {
            final String message = "could not get search options"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get search options"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes    user DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @GET
    @Path("/getSearchOptionsByDomain")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSearchOptions(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getSearchOptions(user, domain));
        } catch (final IOException e) {
            final String message = "could not get search options"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get search options"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes           user DOCUMENT ME!
     * @param   classIdsBytes       DOCUMENT ME!
     * @param   searchOptionsBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response search(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_CLASS_ID) final String classIdsBytes,
            @QueryParam(PARAM_SEARCH_OPTIONS) final String searchOptionsBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String[] classIds = Converter.deserialiseFromString(classIdsBytes, String[].class);
            final SearchOption[] options = Converter.deserialiseFromString(searchOptionsBytes, SearchOption[].class);

            return createResponse(callserver.search(user, classIds, options));
        } catch (final IOException e) {
            final String message = "could not search"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not search"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lsNameBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getDefaultIconsByLSName")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDefaultIconsByLSName(@QueryParam(PARAM_LS_NAME) final String lsNameBytes)
            throws RemoteException {
        try {
            final String lsName = Converter.deserialiseFromString(lsNameBytes, String.class);

            return createResponse(callserver.getDefaultIcons(lsName));
        } catch (final IOException e) {
            final String message = "could not get icons"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get icons"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getDefaultIcons")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDefaultIcons() throws RemoteException {
        try {
            return createResponse(callserver.getDefaultIcons());
        } catch (final IOException e) {
            final String message = "could not get default icons"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         user DOCUMENT ME!
     * @param   oldPasswordBytes  DOCUMENT ME!
     * @param   newPasswordBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @PUT
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response changePasswordGET(@QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_OLD_PASSWORD) final String oldPasswordBytes,
            @QueryParam(PARAM_NEW_PASSWORD) final String newPasswordBytes) throws RemoteException, UserException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String oldPassword = Converter.deserialiseFromString(oldPasswordBytes, String.class);
            final String newPassword = Converter.deserialiseFromString(newPasswordBytes, String.class);

            return createResponse(callserver.changePassword(user, oldPassword, newPassword));
        } catch (final IOException e) {
            final String message = "could not change password"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not change password"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ugLsNameBytes  DOCUMENT ME!
     * @param   ugNameBytes    DOCUMENT ME!
     * @param   uLsNameBytes   DOCUMENT ME!
     * @param   unameBytes     DOCUMENT ME!
     * @param   passwordBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @GET
    @Path("/getUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGET(@QueryParam(PARAM_USERGROUP_LS_NAME) final String ugLsNameBytes,
            @QueryParam(PARAM_USERGROUP_NAME) final String ugNameBytes,
            @QueryParam(PARAM_USER_LS_NAME) final String uLsNameBytes,
            @QueryParam(PARAM_USERNAME) final String unameBytes,
            @QueryParam(PARAM_PASSWORD) final String passwordBytes) throws RemoteException, UserException {
        try {
            final String ugLsName = Converter.deserialiseFromString(ugLsNameBytes, String.class);
            final String ugName = Converter.deserialiseFromString(ugNameBytes, String.class);
            final String uLsName = Converter.deserialiseFromString(uLsNameBytes, String.class);
            final String uname = Converter.deserialiseFromString(unameBytes, String.class);
            final String password = Converter.deserialiseFromString(passwordBytes, String.class);

            return createResponse(callserver.getUser(ugLsName, ugName, uLsName, uname, password));
        } catch (final IOException e) {
            final String message = "could not get user"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get user"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/getUserGroupNames")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGroupNames() throws RemoteException {
        try {
            return createResponse(callserver.getUserGroupNames());
        } catch (final IOException e) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   unameBytes   DOCUMENT ME!
     * @param   lsHomeBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @GET
    @Path("/getUserGroupNamesByUser")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGroupNamesGET(@QueryParam(PARAM_USERNAME) final String unameBytes,
            @QueryParam(PARAM_LS_HOME) final String lsHomeBytes) throws RemoteException {
        try {
            final String uname = Converter.deserialiseFromString(unameBytes, String.class);
            final String lsHome = Converter.deserialiseFromString(lsHomeBytes, String.class);

            return createResponse(callserver.getUserGroupNames(uname, lsHome));
        } catch (final IOException e) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }
}
