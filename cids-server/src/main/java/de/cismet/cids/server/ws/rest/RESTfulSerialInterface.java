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
import Sirius.server.search.CidsServerSearch;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.store.QueryData;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.cismet.cids.server.CallServerService;

import de.cismet.tools.Converter;

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
    public static final String PARAM_USERGROUP_LS_NAME = "ugLsName";              // NOI18N
    public static final String PARAM_USERGROUP_NAME = "ugName";                   // NOI18N
    public static final String PARAM_USER_LS_NAME = "uLsName";                    // NOI18N
    public static final String PARAM_USERNAME = "uname";                          // NOI18N
    public static final String PARAM_PASSWORD = "password";                       // NOI18N
    public static final String PARAM_LS_HOME = "lsHome";                          // NOI18N
    public static final String PARAM_USER = "user";                               // NOI18N
    public static final String PARAM_OLD_PASSWORD = "old_password";               // NOI18N
    public static final String PARAM_NEW_PASSWORD = "new_password";               // NOI18N
    public static final String PARAM_CLASS_ID = "classIds";                       // NOI18N
    public static final String PARAM_LS_NAME = "lsName";                          // NOI18N
    public static final String PARAM_SEARCH_OPTIONS = "searchOptions";            // NOI18N
    public static final String PARAM_DOMAIN = "domain";                           // NOI18N
    public static final String PARAM_QUERY_ID = "queryID";                        // NOI18N
    public static final String PARAM_PARAM_KEY = "paramKey";                      // NOI18N
    public static final String PARAM_DESCRIPTION = "description";                 // NOI18N
    public static final String PARAM_TYPE_ID = "typeId";                          // NOI18N
    public static final String PARAM_QUERY = "query";                             // NOI18N
    public static final String PARAM_QUERY_RESULT = "queryResult";                // NOI18N
    public static final String PARAM_QUERY_POSITION = "queryPosition";            // NOI18N
    public static final String PARAM_QUERY_NAME = "queryName";                    // NOI18N
    public static final String PARAM_STATEMENT = "statement";                     // NOI18N
    public static final String PARAM_RESULT_TYPE = "resultType";                  // NOI18N
    public static final String PARAM_IS_UPDATE = "isUpdate";                      // NOI18N
    public static final String PARAM_IS_BATCH = "isBatch";                        // NOI18N
    public static final String PARAM_IS_ROOT = "isRoot";                          // NOI18N
    public static final String PARAM_IS_UNION = "isUnion";                        // NOI18N
    public static final String PARAM_USERGROUP = "userGroup";                     // NOI18N
    public static final String PARAM_QUERY_DATA = "queryData";                    // NOI18N
    public static final String PARAM_REP_FIELDS = "representationFields";         // NOI18N
    public static final String PARAM_REP_PATTERN = "representationPatter";        // NOI18N
    public static final String PARAM_LOCAL_SERVER_NAME = "localServerName";       // NOI18N
    public static final String PARAM_TABLE_NAME = "tableName";                    // NOI18N
    public static final String PARAM_METAOBJECT = "metaObject";                   // NOI18N
    public static final String PARAM_METACLASS = "metaClass";                     // NOI18N
    public static final String PARAM_OBJECT_ID = "objectID";                      // NOI18N
    public static final String PARAM_NODE_FROM = "fromNode";                      // NOI18N
    public static final String PARAM_NODE_TO = "toNode";                          // NOI18N
    public static final String PARAM_NODE = "node";                               // NOI18N
    public static final String PARAM_LINK_PARENT = "linkParent";                  // NOI18N
    public static final String PARAM_NODE_ID = "nodeID";                          // NOI18N
    public static final String PARAM_KEY = "key";                                 // NOI18N
    public static final String PARAM_CUSTOM_SERVER_SEARCH = "customServerSearch"; // NOI18N
    public static final String PARAM_ELEMENTS = "elements";                       // NOI18N

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
    @POST
    @Path("/getRootsByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRootsByDomain(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainNameBytes) throws RemoteException {
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
    @POST
    @Path("/getRoots")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRoots(@FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/getChildren")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getChildren(@FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_USER) final String usrBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addNode(@FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_LINK_PARENT) final String parentBytes,
            @FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/deleteNode")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteNode(@FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addLink(@FormParam(PARAM_NODE_FROM) final String fromBytes,
            @FormParam(PARAM_NODE_TO) final String toBytes,
            @FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/deleteLink")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteLink(@FormParam(PARAM_NODE_FROM) final String fromBytes,
            @FormParam(PARAM_NODE_TO) final String toBytes,
            @FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
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
    @POST
    @Path("/getMetaObjectNodeByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNode(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_NODE_ID) final String nodeIDBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getMetaObjectNodeByString")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNodeByString(@FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
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
    @POST
    @Path("/getMetaObjectNodeByQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNodeByQuery(@FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
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
    @POST
    @Path("/getMetaObjectByString")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByString(@FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
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
    @POST
    @Path("/getMetaObjectByQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByQuery(@FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes) throws RemoteException {
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
    @POST
    @Path("/getMetaObjectByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObject(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_OBJECT_ID) final String objectIDBytes,
            @FormParam(PARAM_CLASS_ID) final String classIDBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response insertMetaObject(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response insertMetaObjectByQuery(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/updateMetaObject")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response updateMetaObject(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/deleteMetaObject")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteMetaObject(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response update(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getInstance")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getInstance(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METACLASS) final String metaClassBytes) throws RemoteException {
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
    @POST
    @Path("/getClassByTableName")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassByTableName(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_TABLE_NAME) final String tableNameBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getClassByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClass(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getClasses")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClasses(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getClassTreeNodesByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/getClassTreeNodesByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/getMethodsByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethodsByUser(@FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/getMethodsByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethods(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_LOCAL_SERVER_NAME) final String localServerNameBytes) throws RemoteException {
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
    @POST
    @Path("/getAllLightweightMetaObjectsForClassByPattern")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClassWithPattern(
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_REP_PATTERN) final String representationPatternBytes) throws RemoteException {
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
    @POST
    @Path("/getAllLightweightMetaObjectsForClass")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClass(@FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes) throws RemoteException {
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
    @POST
    @Path("/getLightweightMetaObjectsByQueryAndPattern")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQueryAndPattern(@FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_REP_PATTERN) final String representationPatternBytes) throws RemoteException {
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
    @POST
    @Path("/getLightweightMetaObjectsByQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQuery(@FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes) throws RemoteException {
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
    @POST
    @Path("/storeQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response storeQuery(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY_DATA) final String queryDataBytes) throws RemoteException {
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
    @POST
    @Path("/getQueryInfosByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQueryInfosByUser(@FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/getQueryInfosByUserGroup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQueryInfosByUserGroup(@FormParam(PARAM_USERGROUP) final String userGroupBytes)
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
    @POST
    @Path("/getQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getQuery(@FormParam(PARAM_QUERY_ID) final String idBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response delete(@FormParam(PARAM_QUERY_ID) final String idBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryFull(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY_NAME) final String nameBytes,
            @FormParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @FormParam(PARAM_STATEMENT) final String statementBytes,
            @FormParam(PARAM_RESULT_TYPE) final String resultTypeBytes,
            @FormParam(PARAM_IS_UPDATE) final String isUpdateBytes,
            @FormParam(PARAM_IS_BATCH) final String isBatchBytes,
            @FormParam(PARAM_IS_ROOT) final String isRootBytes,
            @FormParam(PARAM_IS_UNION) final String isUnionBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQuery(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY_NAME) final String nameBytes,
            @FormParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @FormParam(PARAM_STATEMENT) final String statementBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryParameterFull(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY_ID) final String queryIdBytes,
            @FormParam(PARAM_TYPE_ID) final String typeIdBytes,
            @FormParam(PARAM_PARAM_KEY) final String paramkeyBytes,
            @FormParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @FormParam(PARAM_QUERY_RESULT) final String isQueryResultBytes,
            @FormParam(PARAM_QUERY_POSITION) final String queryPositionBytes) throws RemoteException {
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addQueryParameter(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY_ID) final String queryIdBytes,
            @FormParam(PARAM_PARAM_KEY) final String paramkeyBytes,
            @FormParam(PARAM_DESCRIPTION) final String descriptionBytes) throws RemoteException {
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
    @POST
    @Path("/getSearchOptionsByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSearchOptions(@FormParam(PARAM_USER) final String userBytes) throws RemoteException {
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
    @POST
    @Path("/getSearchOptionsByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSearchOptions(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
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
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response search(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CLASS_ID) final String classIdsBytes,
            @FormParam(PARAM_SEARCH_OPTIONS) final String searchOptionsBytes) throws RemoteException {
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
    @POST
    @Path("/getDefaultIconsByLSName")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDefaultIconsByLSName(@FormParam(PARAM_LS_NAME) final String lsNameBytes) throws RemoteException {
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
    @POST
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
    @POST
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response changePasswordGET(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_OLD_PASSWORD) final String oldPasswordBytes,
            @FormParam(PARAM_NEW_PASSWORD) final String newPasswordBytes) throws RemoteException, UserException {
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
    @POST
    @Path("/getUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGET(@FormParam(PARAM_USERGROUP_LS_NAME) final String ugLsNameBytes,
            @FormParam(PARAM_USERGROUP_NAME) final String ugNameBytes,
            @FormParam(PARAM_USER_LS_NAME) final String uLsNameBytes,
            @FormParam(PARAM_USERNAME) final String unameBytes,
            @FormParam(PARAM_PASSWORD) final String passwordBytes) throws RemoteException, UserException {
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
    @POST
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
    @POST
    @Path("/getUserGroupNamesByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGroupNamesGET(@FormParam(PARAM_USERNAME) final String unameBytes,
            @FormParam(PARAM_LS_HOME) final String lsHomeBytes) throws RemoteException {
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

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  DOCUMENT ME!
     * @param   keyBytes   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getConfigAttr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getConfigAttr(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_KEY) final String keyBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String key = Converter.deserialiseFromString(keyBytes, String.class);

            return createResponse(callserver.getConfigAttr(user, key));
        } catch (final IOException e) {
            final String message = "could not get config attr"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get config attr"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  DOCUMENT ME!
     * @param   keyBytes   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/hasConfigAttr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response hasConfigAttr(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_KEY) final String keyBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String key = Converter.deserialiseFromString(keyBytes, String.class);

            return createResponse(callserver.hasConfigAttr(user, key));
        } catch (final IOException e) {
            final String message = "could not determine config attr"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not determine config attr"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes                DOCUMENT ME!
     * @param   customServerSearchBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("customServerSearch")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response customServerSearchPOST(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CUSTOM_SERVER_SEARCH) final String customServerSearchBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final CidsServerSearch serverSearch = Converter.deserialiseFromString(
                    customServerSearchBytes,
                    CidsServerSearch.class);

            return createResponse(callserver.customServerSearch(user, serverSearch));
        } catch (final IOException e) {
            final String message = "could not execute custom search"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not execute custom search"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIdBytes   DOCUMENT ME!
     * @param   objectIdBytes  DOCUMENT ME!
     * @param   domainBytes    DOCUMENT ME!
     * @param   userBytes      DOCUMENT ME!
     * @param   elementsBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("getHistory")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getHistoryPOST(@FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_OBJECT_ID) final String objectIdBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_ELEMENTS) final String elementsBytes) throws RemoteException {
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final int objectId = Converter.deserialiseFromString(objectIdBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int elements = Converter.deserialiseFromString(elementsBytes, int.class);

            return createResponse(callserver.getHistory(classId, objectId, domain, user, elements));
        } catch (final IOException e) {
            final String message = "could not get history"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get history"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }
}