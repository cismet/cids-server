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

import com.sun.jersey.core.util.Base64;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.rmi.RemoteException;

import java.util.Date;

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
import de.cismet.cids.server.connectioncontext.ConnectionContext;
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
    public static final String PARAM_CONTEXT = "context";                         // NOI18N

    //~ Instance fields --------------------------------------------------------

    private Boolean compressionEnabled = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterface object.
     */
    public RESTfulSerialInterface() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CallServerService getCallserver() {
        return StartProxy.getInstance().getCallServer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isCompressionEnabled() {
        if (compressionEnabled == null) {
            compressionEnabled = StartProxy.getInstance().getServerProperties().isCompressionEnabled();
        }
        return compressionEnabled;
    }

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
        if (isCompressionEnabled()) {
            return Response.ok(Converter.serialiseToGzip(o)).build();
        } else {
            return Response.ok(Converter.serialiseToBase64(o)).build();
        }
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
        if (isCompressionEnabled()) {
            return Response.ok(Converter.serialiseToGzip(o)).type(type).build();
        } else {
            return Response.ok(Converter.serialiseToBase64(o)).type(type).build();
        }
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
            final String... additionalInfo) {
        if (RESTfulService.isThreadNamingEnabled()) {
            final long t = System.currentTimeMillis();
            final String currentName = Thread.currentThread().getName();
            final int indexOfAdditionalInfo = currentName.indexOf("[");
            String rawName = currentName;
            if (indexOfAdditionalInfo > 1) {
                rawName = currentName.substring(0, indexOfAdditionalInfo - 1);
            }
            String additionalInfos = "";
            if (additionalInfo.length == 0) {
                additionalInfos = "-";
            } else {
                for (final String ai : additionalInfo) {
                    additionalInfos = additionalInfos + ai + ",";
                }
            }
            additionalInfos = additionalInfos.substring(0, additionalInfos.length() - 1);

            Thread.currentThread()
                    .setName(rawName + " [@" + t + "," + new Date(t) + ", " + methodName + ", " + user + "@"
                        + hsr.getLocalAddr() + ", " + additionalInfos
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
        nameTheThread(hsr, "getTest", "anonymous");
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
     * @param   hsr              DOCUMENT ME!
     * @param   userBytes        user DOCUMENT ME!
     * @param   domainNameBytes  DOCUMENT ME!
     * @param   contextBytes     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getRootsByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRootsByDomain(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainNameBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getRootsByDomain", "[bytes]", "domain=[bytes]");

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(
                    domainNameBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getRootsByDomain", user.toString(), "domain=" + domain);

            return createResponse(getCallserver().getRoots(user, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getRoots")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRoots(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getRoots", "[bytes]");

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getRoots", user.toString());

            return createResponse(getCallserver().getRoots(user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   nodeBytes     node DOCUMENT ME!
     * @param   usrBytes      usr DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getChildren")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getChildren(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getChildren", "[bytes]", "node=[bytes]");

        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(usrBytes, User.class, isCompressionEnabled());
            nameTheThread(hsr, "/getChildren", user.toString(), "node=" + ((node != null) ? node.toString() : "null"));

            return createResponse(getCallserver().getChildren(node, user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   nodeBytes     node DOCUMENT ME!
     * @param   parentBytes   DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/addNode")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addNode(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_LINK_PARENT) final String parentBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/addNode", "[bytes]", "node=[bytes]");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class, isCompressionEnabled());
            final Link parent = Converter.deserialiseFromString(parentBytes, Link.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            nameTheThread(hsr, "/addNode", user.toString(), "node=" + ((node != null) ? node.toString() : "null"));

            return createResponse(getCallserver().addNode(node, parent, user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   nodeBytes     node DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/deleteNode")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteNode(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_NODE) final String nodeBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/deleteNode", "[bytes]", "node=[bytes]");

        try {
            final Node node = Converter.deserialiseFromString(nodeBytes, Node.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/deleteNode", user.toString(), "node=" + ((node != null) ? node.toString() : "null"));

            return createResponse(getCallserver().deleteNode(node, user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   fromBytes     from DOCUMENT ME!
     * @param   toBytes       to DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/addLink")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response addLink(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_NODE_FROM) final String fromBytes,
            @FormParam(PARAM_NODE_TO) final String toBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/addLink", "[bytes]", "link=[bytes]");

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final Node from = Converter.deserialiseFromString(fromBytes, Node.class, isCompressionEnabled());
            final Node to = Converter.deserialiseFromString(toBytes, Node.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/addLink",
                user.toString(),
                "link="
                        + ((from != null) ? from.toString() : "null")
                        + "-->"
                        + ((to != null) ? to.toString() : "null"));

            return createResponse(getCallserver().addLink(from, to, user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   fromBytes     from DOCUMENT ME!
     * @param   toBytes       to DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/deleteLink")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteLink(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_NODE_FROM) final String fromBytes,
            @FormParam(PARAM_NODE_TO) final String toBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/deleteLink", "[bytes]", "link=[bytes]");

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final Node from = Converter.deserialiseFromString(fromBytes, Node.class, isCompressionEnabled());
            final Node to = Converter.deserialiseFromString(toBytes, Node.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/deleteLink",
                user.toString(),
                "link="
                        + ((from != null) ? from.toString() : "null")
                        + "-->"
                        + ((to != null) ? to.toString() : "null"));

            return createResponse(getCallserver().deleteLink(from, to, user));
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
     * @param   hsr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getDomains")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDomains(@Context final HttpServletRequest hsr) throws RemoteException {
        nameTheThread(hsr, "/getDomains", "anonymous");
        try {
            return createResponse(getCallserver().getDomains());
        } catch (final IOException e) {
            final String message = "could not get domains"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     usr DOCUMENT ME!
     * @param   nodeIDBytes   DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectNodeByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNode(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_NODE_ID) final String nodeIDBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMetaObjectNodeByID", "[bytes]", "domain=[bytes]", "nodeId=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final int nodeID = Converter.deserialiseFromString(nodeIDBytes, int.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getMetaObjectNodeByID", user.toString(), "domain=" + domain, ",nodeId=" + nodeID);

            return createResponse(getCallserver().getMetaObjectNode(user, nodeID, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   usrBytes      usr DOCUMENT ME!
     * @param   queryBytes    DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectNodeByString")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectNodeByString(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMetaObjectNodeByString", "[bytes]", "query=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getMetaObjectNodeByString", user.toString(), "query=" + query);

            return createResponse(getCallserver().getMetaObjectNode(user, query));
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
     * @param   hsr           DOCUMENT ME!
     * @param   usrBytes      usr DOCUMENT ME!
     * @param   queryBytes    DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectByString")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByString(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMetaObjectByString", "[bytes]", "query=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getMetaObjectByString", user.toString(), "query=" + query);

            return createResponse(getCallserver().getMetaObject(user, query));
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
     * @param   hsr           DOCUMENT ME!
     * @param   usrBytes      DOCUMENT ME!
     * @param   queryBytes    DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectByStringAndDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObjectByString(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String usrBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMetaObjectByStringAndDomain", "[bytes]", "domain=[bytes]", " query=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(usrBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/getMetaObjectByStringAndDomain",
                user.toString(),
                "domain="
                        + domain,
                "query="
                        + query);

            return createResponse(getCallserver().getMetaObject(user, query, domain));
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
     * @param   hsr            DOCUMENT ME!
     * @param   userBytes      usr DOCUMENT ME!
     * @param   objectIDBytes  DOCUMENT ME!
     * @param   classIDBytes   DOCUMENT ME!
     * @param   domainBytes    DOCUMENT ME!
     * @param   contextBytes   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMetaObjectByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMetaObject(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_OBJECT_ID) final String objectIDBytes,
            @FormParam(PARAM_CLASS_ID) final String classIDBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMetaObjectByID", "[bytes]", "domain=[bytes]", "classId=[bytes]", "objectId=[bytes]");

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final int objectID = Converter.deserialiseFromString(objectIDBytes, int.class, isCompressionEnabled());
            final int classID = Converter.deserialiseFromString(classIDBytes, int.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/getMetaObjectByID",
                user.toString(),
                "domain="
                        + domain,
                "classId="
                        + classID,
                "objectId="
                        + objectID);

            return createResponse(getCallserver().getMetaObject(user, objectID, classID, domain));
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
     * @param   hsr              DOCUMENT ME!
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     * @param   contextBytes     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/insertMetaObject")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response insertMetaObject(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/insertMetaObject", "[bytes]", "domain=[bytes]", "object=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final MetaObject metaObject = Converter.deserialiseFromString(
                    metaObjectBytes,
                    MetaObject.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/insertMetaObject",
                user.toString(),
                "domain="
                        + domain,
                "object="
                        + metaObject.getID()
                        + "@"
                        + metaObject.getMetaClass().getTableName());

            return createResponse(getCallserver().insertMetaObject(user, metaObject, domain));
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
     * @param   hsr              DOCUMENT ME!
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     * @param   contextBytes     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/updateMetaObject")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response updateMetaObject(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/updateMetaObject", "[bytes]", "domain=[bytes]", "object=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final MetaObject metaObject = Converter.deserialiseFromString(
                    metaObjectBytes,
                    MetaObject.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/updateMetaObject",
                user.toString(),
                "domain="
                        + domain,
                "object="
                        + metaObject.getID()
                        + "@"
                        + metaObject.getMetaClass().getTableName());

            return createResponse(getCallserver().updateMetaObject(user, metaObject, domain));
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
     * @param   hsr              DOCUMENT ME!
     * @param   userBytes        user DOCUMENT ME!
     * @param   metaObjectBytes  DOCUMENT ME!
     * @param   domainBytes      DOCUMENT ME!
     * @param   contextBytes     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/deleteMetaObject")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteMetaObject(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METAOBJECT) final String metaObjectBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/deleteMetaObject", "[bytes]", "domain=[bytes]", "object=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final MetaObject metaObject = Converter.deserialiseFromString(
                    metaObjectBytes,
                    MetaObject.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(
                hsr,
                "/deleteMetaObject",
                user.toString(),
                "domain="
                        + domain,
                "object="
                        + metaObject.getID()
                        + "@"
                        + metaObject.getMetaClass().getTableName());

            return createResponse(getCallserver().deleteMetaObject(user, metaObject, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   queryBytes    DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response update(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/update", "[bytes]", "domain=[bytes]", "query=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/update", user.toString(), "domain=" + domain, "query=" + query);

            return createResponse(getCallserver().update(user, query, domain));
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
     * @param   hsr             DOCUMENT ME!
     * @param   userBytes       user DOCUMENT ME!
     * @param   metaClassBytes  c DOCUMENT ME!
     * @param   contextBytes    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getInstance")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getInstance(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_METACLASS) final String metaClassBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getInstance", "[bytes]", "class=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final MetaClass metaClass = Converter.deserialiseFromString(
                    metaClassBytes,
                    MetaClass.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getInstance", user.toString(), "class=" + metaClass.toString());

            return createResponse(getCallserver().getInstance(user, metaClass));
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
     * @param   hsr             DOCUMENT ME!
     * @param   userBytes       user DOCUMENT ME!
     * @param   tableNameBytes  DOCUMENT ME!
     * @param   domainBytes     DOCUMENT ME!
     * @param   contextBytes    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getClassByTableName")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassByTableName(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_TABLE_NAME) final String tableNameBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getClassByTableName", "[bytes]", "domain=[bytes]", "tableName=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String tableName = Converter.deserialiseFromString(
                    tableNameBytes,
                    String.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getClassByTableName", user.toString(), "domain=" + domain, "tableName=" + tableName);

            return createResponse(getCallserver().getClassByTableName(user, tableName, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   classIdBytes  classID DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getClassByID")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClass(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getClassByID", "[bytes]", "domain=[bytes]", "classId=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getClassByID", user.toString(), "domain=" + domain, "classId=" + classId);

            return createResponse(getCallserver().getClass(user, classId, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getClasses")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClasses(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getClasses", "[bytes]", "domain=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getClasses", user.toString(), "domain=" + domain);
            return createResponse(getCallserver().getClasses(user, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getClassTreeNodesByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getClassTreeNodesByUser", "[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getClassTreeNodesByUser", user.toString());

            return createResponse(getCallserver().getClassTreeNodes(user));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   domainBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getClassTreeNodesByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClassTreeNodes(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getClassTreeNodesByDomain", "[bytes]", "domain=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getClassTreeNodesByDomain", user.toString(), "domain=" + domain);

            return createResponse(getCallserver().getClassTreeNodes(user, domain));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     user DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMethodsByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethodsByUser(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMethodsByUser", "[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getMethodsByUser", user.toString());

            return createResponse(getCallserver().getMethods(user));
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
     * @param   hsr                   DOCUMENT ME!
     * @param   userBytes             user DOCUMENT ME!
     * @param   localServerNameBytes  DOCUMENT ME!
     * @param   contextBytes          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getMethodsByDomain")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMethods(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_LOCAL_SERVER_NAME) final String localServerNameBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getMethodsByDomain", "[bytes]", "domain=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String localServerName = Converter.deserialiseFromString(
                    localServerNameBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getMethodsByDomain", user.toString(), "domain=" + localServerName);

            return createResponse(getCallserver().getMethods(user, localServerName));
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
     * @param   hsr                         DOCUMENT ME!
     * @param   classIdBytes                DOCUMENT ME!
     * @param   userBytes                   user DOCUMENT ME!
     * @param   representationFieldsBytes   DOCUMENT ME!
     * @param   representationPatternBytes  DOCUMENT ME!
     * @param   contextBytes                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/getAllLightweightMetaObjectsForClassByPattern")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClassWithPattern(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_REP_PATTERN) final String representationPatternBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getAllLightweightMetaObjectsForClassByPattern", "[bytes]", "classId=[bytes]", "...");
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class,
                    isCompressionEnabled());
            final String representationPattern = Converter.deserialiseFromString(
                    representationPatternBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(
                hsr,
                "/getAllLightweightMetaObjectsForClassByPattern",
                user.toString(),
                "classId="
                        + classId,
                "...");

            return createResponse(getCallserver().getAllLightweightMetaObjectsForClass(
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
     * @param   hsr                        DOCUMENT ME!
     * @param   classIdBytes               DOCUMENT ME!
     * @param   userBytes                  user DOCUMENT ME!
     * @param   representationFieldsBytes  DOCUMENT ME!
     * @param   contextBytes               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getAllLightweightMetaObjectsForClass")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllLightweightMetaObjectsForClass(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getAllLightweightMetaObjectsForClass", "[bytes]", "classId=[bytes]", "...");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getAllLightweightMetaObjectsForClass", user.toString(), "classId=" + classId, "...");

            return createResponse(getCallserver().getAllLightweightMetaObjectsForClass(
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
     * @param   hsr                         DOCUMENT ME!
     * @param   classIdBytes                DOCUMENT ME!
     * @param   userBytes                   user DOCUMENT ME!
     * @param   queryBytes                  DOCUMENT ME!
     * @param   representationFieldsBytes   DOCUMENT ME!
     * @param   representationPatternBytes  DOCUMENT ME!
     * @param   contextBytes                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException DOCUMENT ME!
     */
    @POST
    @Path("/getLightweightMetaObjectsByQueryAndPattern")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQueryAndPattern(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_REP_PATTERN) final String representationPatternBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getLightweightMetaObjectsByQueryAndPattern", "[bytes]", "classId=[bytes]", "...");
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class,
                    isCompressionEnabled());
            final String representationPattern = Converter.deserialiseFromString(
                    representationPatternBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(
                hsr,
                "/getLightweightMetaObjectsByQueryAndPattern",
                user.toString(),
                "classId="
                        + classId,
                "...");

            return createResponse(getCallserver().getLightweightMetaObjectsByQuery(
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
     * @param   hsr                        DOCUMENT ME!
     * @param   classIdBytes               DOCUMENT ME!
     * @param   userBytes                  user DOCUMENT ME!
     * @param   queryBytes                 DOCUMENT ME!
     * @param   representationFieldsBytes  DOCUMENT ME!
     * @param   contextBytes               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getLightweightMetaObjectsByQuery")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLightweightMetaObjectsByQuery(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_QUERY) final String queryBytes,
            @FormParam(PARAM_REP_FIELDS) final String representationFieldsBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getLightweightMetaObjectsByQuery", "[bytes]", "classId=[bytes]", "...");
        try {
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String query = Converter.deserialiseFromString(queryBytes, String.class, isCompressionEnabled());
            final String[] representationFields = Converter.deserialiseFromString(
                    representationFieldsBytes,
                    String[].class,
                    isCompressionEnabled());

            nameTheThread(hsr, "/getLightweightMetaObjectsByQuery", user.toString(), "classId=" + classId, "...");
            return createResponse(getCallserver().getLightweightMetaObjectsByQuery(
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
     * @param   hsr           DOCUMENT ME!
     * @param   lsNameBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getDefaultIconsByLSName")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDefaultIconsByLSName(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_LS_NAME) final String lsNameBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getDefaultIconsByLSName", "anonymous", "domain=[bytes]");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String lsName = Converter.deserialiseFromString(lsNameBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getDefaultIconsByLSName", "anonymous", "domain=" + lsName);

            return createResponse(getCallserver().getDefaultIcons(lsName));
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
     * @param   hsr           DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getDefaultIcons")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDefaultIcons(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getDefaultIcons", "anonymous");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            return createResponse(getCallserver().getDefaultIcons());
        } catch (final IOException e) {
            final String message = "could not get default icons"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "could not get default icons"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr               DOCUMENT ME!
     * @param   userBytes         user DOCUMENT ME!
     * @param   oldPasswordBytes  DOCUMENT ME!
     * @param   newPasswordBytes  DOCUMENT ME!
     * @param   contextBytes      DOCUMENT ME!
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
    public Response changePasswordGET(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_OLD_PASSWORD) final String oldPasswordBytes,
            @FormParam(PARAM_NEW_PASSWORD) final String newPasswordBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException, UserException {
        nameTheThread(hsr, "/changePassword", "[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String oldPassword = Converter.deserialiseFromString(
                    oldPasswordBytes,
                    String.class,
                    isCompressionEnabled());
            final String newPassword = Converter.deserialiseFromString(
                    newPasswordBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/changePassword", user.toString());

            return createResponse(getCallserver().changePassword(user, oldPassword, newPassword));
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
     * @param   contextBytes   DOCUMENT ME!
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
            @FormParam(PARAM_PASSWORD) final String passwordBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException, UserException {
        nameTheThread(hsr, "/getUser", "[bytes]");

        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String ugLsName = Converter.deserialiseFromString(
                    ugLsNameBytes,
                    String.class,
                    isCompressionEnabled());
            final String ugName = Converter.deserialiseFromString(ugNameBytes, String.class, isCompressionEnabled());
            final String uLsName = Converter.deserialiseFromString(uLsNameBytes, String.class, isCompressionEnabled());
            final String uname = Converter.deserialiseFromString(unameBytes, String.class, isCompressionEnabled());
            final String password = Converter.deserialiseFromString(
                    passwordBytes,
                    String.class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/getUser", uname);

            return createResponse(getCallserver().getUser(ugLsName, ugName, uLsName, uname, password));
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
     * @param   hsr           DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  WebApplicationException RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/getUserGroupNames")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGroupNames(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getUserGroupNames", "anonymous");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            return createResponse(getCallserver().getUserGroupNames());
        } catch (final IOException e) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        } catch (final ClassNotFoundException e) {
            final String message = "ould not get usergroup names";  // NOI18N
            LOG.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hsr           DOCUMENT ME!
     * @param   unameBytes    DOCUMENT ME!
     * @param   lsHomeBytes   DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getUserGroupNamesByUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getUserGroupNamesGET(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USERNAME) final String unameBytes,
            @FormParam(PARAM_LS_HOME) final String lsHomeBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getUserGroupNamesByUser", "[bytes]", "userdomain=[bytes]");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String uname = Converter.deserialiseFromString(unameBytes, String.class, isCompressionEnabled());
            final String lsHome = Converter.deserialiseFromString(lsHomeBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getUserGroupNamesByUser", uname, "userdomain=" + lsHome);

            return createResponse(getCallserver().getUserGroupNames(uname, lsHome));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     DOCUMENT ME!
     * @param   keyBytes      DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/getConfigAttr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getConfigAttr(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_KEY) final String keyBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getConfigAttr", "[bytes]", "key=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String key = Converter.deserialiseFromString(keyBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/getConfigAttr", user.toString(), "key=" + key);

            return createResponse(getCallserver().getConfigAttr(user, key));
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
     * @param   hsr           DOCUMENT ME!
     * @param   userBytes     DOCUMENT ME!
     * @param   keyBytes      DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/hasConfigAttr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response hasConfigAttr(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_KEY) final String keyBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/hasConfigAttr", "[bytes]", "key=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String key = Converter.deserialiseFromString(keyBytes, String.class, isCompressionEnabled());
            nameTheThread(hsr, "/hasConfigAttr", user.toString(), "key=" + key);

            return createResponse(getCallserver().hasConfigAttr(user, key));
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
     * @param   hsr                      DOCUMENT ME!
     * @param   userBytes                DOCUMENT ME!
     * @param   customServerSearchBytes  DOCUMENT ME!
     * @param   contextBytes             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("customServerSearch")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response customServerSearchPOST(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_CUSTOM_SERVER_SEARCH) final String customServerSearchBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/customServerSearch", "[bytes]", "serverSearch=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final CidsServerSearch serverSearch = Converter.deserialiseFromString(
                    customServerSearchBytes,
                    CidsServerSearch.class,
                    isCompressionEnabled());
            nameTheThread(
                hsr,
                "/customServerSearch",
                user.toString(),
                "serverSearch="
                        + serverSearch.getClass().getCanonicalName());

            return createResponse(getCallserver().customServerSearch(user, serverSearch));
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
     * @param   hsr            DOCUMENT ME!
     * @param   classIdBytes   DOCUMENT ME!
     * @param   objectIdBytes  DOCUMENT ME!
     * @param   domainBytes    DOCUMENT ME!
     * @param   userBytes      DOCUMENT ME!
     * @param   elementsBytes  DOCUMENT ME!
     * @param   contextBytes   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("getHistory")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getHistoryPOST(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_CLASS_ID) final String classIdBytes,
            @FormParam(PARAM_OBJECT_ID) final String objectIdBytes,
            @FormParam(PARAM_DOMAIN) final String domainBytes,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_ELEMENTS) final String elementsBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/getHistory", "[bytes]");
        try {
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class, isCompressionEnabled());
            final int objectId = Converter.deserialiseFromString(objectIdBytes, int.class, isCompressionEnabled());
            final String domain = Converter.deserialiseFromString(domainBytes, String.class, isCompressionEnabled());
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final int elements = Converter.deserialiseFromString(elementsBytes, int.class, isCompressionEnabled());
            nameTheThread(hsr, "/getHistory", user.toString());

            return createResponse(getCallserver().getHistory(classId, objectId, domain, user, elements));
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
     * @param   hsr               DOCUMENT ME!
     * @param   userBytes         DOCUMENT ME!
     * @param   taskdomainBytes   DOCUMENT ME!
     * @param   tasknameBytes     DOCUMENT ME!
     * @param   bodyBytes         DOCUMENT ME!
     * @param   paramelipseBytes  DOCUMENT ME!
     * @param   contextBytes      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/executeTask")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.WILDCARD)
    public Response executeTask(@Context final HttpServletRequest hsr,
            @FormParam(PARAM_USER) final String userBytes,
            @FormParam(PARAM_DOMAIN) final String taskdomainBytes,
            @FormParam(PARAM_TASKNAME) final String tasknameBytes,
            @FormParam(PARAM_BODY) final String bodyBytes,
            @FormParam(PARAM_PARAMELIPSE) final String paramelipseBytes,
            @FormParam(PARAM_CONTEXT) final String contextBytes) throws RemoteException {
        nameTheThread(hsr, "/executeTask", "[bytes]", "domain=[bytes]", "taskname=[bytes]");
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class, isCompressionEnabled());
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());
            final String taskdomain = Converter.deserialiseFromString(
                    taskdomainBytes,
                    String.class,
                    isCompressionEnabled());
            final String taskname = Converter.deserialiseFromString(
                    tasknameBytes,
                    String.class,
                    isCompressionEnabled());
            final Object body = Converter.deserialiseFromString(bodyBytes, String.class, isCompressionEnabled());
            final ServerActionParameter[] params = Converter.deserialiseFromString(
                    paramelipseBytes,
                    ServerActionParameter[].class,
                    isCompressionEnabled());
            nameTheThread(hsr, "/executeTask", user.toString(), "domain=" + taskdomain, "taskname=" + taskname);

            return createResponse(getCallserver().executeTask(user, taskname, taskdomain, context, body, params), null);
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
     * @param   hsr           DOCUMENT ME!
     * @param   uriInfo       DOCUMENT ME!
     * @param   headers       DOCUMENT ME!
     * @param   authString    DOCUMENT ME!
     * @param   taskname      DOCUMENT ME!
     * @param   taskdomain    DOCUMENT ME!
     * @param   contextBytes  DOCUMENT ME!
     * @param   body          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @POST
    @Path("/executeTask/{taskname}@{taskdomain}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    public Response executeTaskWithPost(@Context final HttpServletRequest hsr,
            @Context final UriInfo uriInfo,
            @Context final HttpHeaders headers,
            @HeaderParam("Authorization") final String authString,
            @PathParam("taskname") final String taskname,
            @PathParam("taskdomain") final String taskdomain,
            @FormParam(PARAM_CONTEXT) final String contextBytes,
            final Object body) throws RemoteException {
        nameTheThread(
            hsr,
            "/executeTask/{taskname}@{taskdomain}",
            "[bytes]",
            "domain="
                    + taskdomain,
            "taskname="
                    + taskname);

        try {
            if (authString == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Basic realm=\"Please Authenticate with cids Credentials\"")
                            .build();
            }
            final ConnectionContext context = Converter.deserialiseFromString(
                    contextBytes,
                    ConnectionContext.class,
                    isCompressionEnabled());

            final User u = getCidsUserFromBasicAuth(authString);
            System.out.println(taskname + "@" + taskdomain);
            nameTheThread(
                hsr,
                "/executeTask/{taskname}@{taskdomain}",
                u.toString(),
                "domain="
                        + taskdomain,
                "taskname="
                        + taskname);

            final Object resp = getCallserver().executeTask(
                    u,
                    taskname,
                    taskdomain,
                    context,
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
     * @param   hsr         DOCUMENT ME!
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
    public Response executeTaskWithGet(@Context final HttpServletRequest hsr,
            @Context final UriInfo uriInfo,
            @Context final HttpHeaders headers,
            @HeaderParam("Authorization") final String authString,
            @PathParam("taskname") final String taskname,
            @PathParam("taskdomain") final String taskdomain) throws RemoteException {
        return executeTaskWithPost(hsr, uriInfo, headers, authString, taskname, taskdomain, null, null);
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

        return getCallserver().getUser(ugLsName, ugName, uLsName, uname, password);
    }
}
