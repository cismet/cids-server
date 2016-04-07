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
import Sirius.server.search.store.QueryData;

import com.sun.jersey.core.util.Base64;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.io.IOException;

import java.rmi.RemoteException;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.commons.security.exceptions.BadHttpStatusCodeException;

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
    public static final String PARAM_TASKNAME = "taskname";                       // NOI18N
    public static final String PARAM_BODY = "json";                               // NOI18N
    public static final String PARAM_PARAMELIPSE = "paramelipse";                 // NOI18N

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
     * @param   o     DOCUMENT ME!
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private Response createResponse(final Object o, final String type) throws IOException {
        return Response.ok(Converter.serialiseToBase64(o)).type(type).build();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hsr             DOCUMENT ME!
     * @param  methodName      DOCUMENT ME!
     * @param  user            DOCUMENT ME!
     * @param  additionalInfo  DOCUMENT ME!
     */
    private void nameTheThread(final HttpServletRequest hsr,
            final String methodName,
            final String user,
            final String additionalInfo) {
        if (RESTfulService.isThreadNamingEnabled()) {
            final long t = System.currentTimeMillis();
            final String currentName = Thread.currentThread().getName();
            final int indexOfAdditionalInfo = currentName.indexOf("[");
            String rawName = currentName;
            if (indexOfAdditionalInfo > 1) {
                rawName = currentName.substring(0, indexOfAdditionalInfo - 1);
            }

            Thread.currentThread()
                    .setName(rawName + " [@" + t + "," + new Date(t) + ", " + methodName + ", " + user + "@"
                        + hsr.getLocalAddr() + ", " + additionalInfo
                        + "]");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    public Response getTest(@Context final HttpServletRequest hsr) throws RemoteException {
        nameTheThread(hsr, "getTest", "noUser", "1000ms sleep");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        return Response.ok(
                    "<html><h3>I'm sorry, Dave. I'm afraid I can't do that.<br>"
                            + "This interface is not meant for browser traffic. "
                            + "I put the following info in our intrusion detection log ;-) </h3><hr><pre>"
                            + "ADRESS:"
                            + hsr.getLocalAddr()
                            + "\n"
                            + "NAME: "
                            + hsr.getLocalName()
                            + "\n"
                            // + "Threading Status:"
                            // + RESTfulService.getThreadingStatus()
                            + "\n"
                            + hsr.toString()
                            + "</pre>"
                            + "<hr><h3>Dave, this conversation can serve no purpose anymore. Goodbye</h3></html>")
                    .build();
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
     * @param   usrBytes     DOCUMENT ME!
     * @param   queryBytes   DOCUMENT ME!
     * @param   domainBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectByStringAndDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByString(@FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getMetaObject(user, query, domain));
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
     * @param   hsr            DOCUMENT ME!
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
    public Response getUserGET(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USERGROUP_LS_NAME) final String ugLsNameBytes,
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

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         DOCUMENT ME!
     * @param   taskdomainBytes   DOCUMENT ME!
     * @param   tasknameBytes     DOCUMENT ME!
     * @param   bodyBytes         DOCUMENT ME!
     * @param   paramelipseBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/executeTask")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.WILDCARD)
    public Response executeTask(@FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String taskdomainBytes,
            @FormParam(PARAM_TASKNAME) final String tasknameBytes,
            @FormParam(PARAM_BODY) final String bodyBytes,
            @FormParam(PARAM_PARAMELIPSE) final String paramelipseBytes) throws RemoteException {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String taskdomain = Converter.deserialiseFromString(taskdomainBytes, String.class);
            final String taskname = Converter.deserialiseFromString(tasknameBytes, String.class);
            final Object body = Converter.deserialiseFromString(bodyBytes, String.class);
            final ServerActionParameter[] params = Converter.deserialiseFromString(
                    paramelipseBytes,
                    ServerActionParameter[].class);

            return createResponse(callserver.executeTask(user, taskname, taskdomain, body, params), null);
        } catch (final IOException e) {
            final String message = "could not execute task"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not execute task"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final RuntimeException e) {
            final String message = "could not execute task"; // NOI18N
            final Throwable cause = e.getCause();
            if (cause instanceof BadHttpStatusCodeException) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message, e);
                }
                throw new RemoteException(message);
            } else {
                LOG.error(message, e);
                throw new RemoteException(message, e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uriInfo     DOCUMENT ME!
     * @param   headers     DOCUMENT ME!
     * @param   authString  DOCUMENT ME!
     * @param   taskname    DOCUMENT ME!
     * @param   taskdomain  DOCUMENT ME!
     * @param   body        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/executeTask/{taskname}@{taskdomain}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    public Response executeTaskWithPost(@Context final UriInfo uriInfo,
            @Context final HttpHeaders headers,
            @HeaderParam("Authorization") final String authString,
            @PathParam("taskname") final String taskname,
            @PathParam("taskdomain") final String taskdomain,
            final Object body) throws RemoteException {
        try {
            if (authString == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Basic realm=\"Please Authenticate with cids Credentials\"")
                            .build();
            }
            final User u = getCidsUserFromBasicAuth(authString);
            System.out.println(taskname + "@" + taskdomain);
            final Object resp = callserver.executeTask(
                    u,
                    taskname,
                    taskdomain,
                    body,
                    ServerActionParameter.fromMVMap(uriInfo.getQueryParameters()));

            return Response.ok(resp).build();
        } catch (Exception e) {
            final String message = "could not testExecute Task"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uriInfo     DOCUMENT ME!
     * @param   headers     DOCUMENT ME!
     * @param   authString  DOCUMENT ME!
     * @param   taskname    DOCUMENT ME!
     * @param   taskdomain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @GET
    @Path("/executeTask/{taskname}@{taskdomain}")
    @Produces(MediaType.WILDCARD)
    public Response executeTaskWithGet(@Context final UriInfo uriInfo,
            @Context final HttpHeaders headers,
            @HeaderParam("Authorization") final String authString,
            @PathParam("taskname") final String taskname,
            @PathParam("taskdomain") final String taskdomain) throws RemoteException {
        return executeTaskWithPost(uriInfo, headers, authString, taskname, taskdomain, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   authString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private User getCidsUserFromBasicAuth(final String authString) throws Exception {
        // Decode Base64
        final String token = new String(Base64.decode(authString.substring(6)));
        final String[] parts = token.split(":");
        final String login = parts[0];
        final String password = parts[1];
        final String[] loginParts = login.split("@");
        final String ugLsName = loginParts[2];
        final String ugName = loginParts[1];
        final String uLsName = ugLsName;
        final String uname = loginParts[0];

        return callserver.getUser(ugLsName, ugName, uLsName, uname, password);
    }
}
