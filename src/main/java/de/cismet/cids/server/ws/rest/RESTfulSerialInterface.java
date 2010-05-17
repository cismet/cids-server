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
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.store.QueryData;


import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ws.Converter;

import java.io.IOException;

import java.rmi.RemoteException;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
// TODO: refine exception handling
@Path("/callserver/binary") // NOI18N
public final class RESTfulSerialInterface {

    //~ Static fields/initializers ---------------------------------------------
    private static final transient Logger LOG = Logger.getLogger(RESTfulSerialInterface.class);
    public static final String PARAM_USERGROUP_LS_NAME = "ugLsName"; // NOI18N
    public static final String PARAM_USERGROUP_NAME = "ugName";      // NOI18N
    public static final String PARAM_USER_LS_NAME = "uLsName";       // NOI18N
    public static final String PARAM_USERNAME = "uname";             // NOI18N
    public static final String PARAM_PASSWORD = "password";          // NOI18N
    public static final String PARAM_LS_HOME = "lsHome"; // NOI18N
    public static final String PARAM_USER = "user";                 // NOI18N
    public static final String PARAM_OLD_PASSWORD = "old_password"; // NOI18N
    public static final String PARAM_NEW_PASSWORD = "new_password"; // NOI18N
    public static final String PARAM_CLASS_IDS = "classIds"; // NOI18N
    public static final String PARAM_LS_NAME = "lsName"; // NOI18N
    public static final String PARAM_SEARCH_OPTIONS = "searchOptions"; // NOI18N
    public static final String PARAM_DOMAIN = "domain";
    public static final String PARAM_QUERY_ID = "queryID";
    public static final String PARAM_PARAM_KEY = "paramKey";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_TYPE_ID = "typeId";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_QUERY_RESULT = "queryResult";
    public static final String PARAM_QUERY_POSITION = "queryPosition";
    public static final String PARAM_QUERY_NAME = "queryName";
    public static final String PARAM_STATEMENT = "statement";
    public static final String PARAM_RESULT_TYPE = "resultType";
    public static final String PARAM_IS_UPDATE = "isUpdate";
    public static final String PARAM_IS_BATCH = "isBatch";
    public static final String PARAM_IS_ROOT = "isRoot";
    public static final String PARAM_IS_UNION = "isUnion";
    public static final String PARAM_USERGROUP = "userGroup";
    public static final String PARAM_QUERY_DATA = "queryData";
    public static final String PARAM_REP_FIELDS = "representationFields";
    public static final String PARAM_REP_PATTERN = "representationPatter";
    public static final String PARAM_LOCAL_SERVER_NAME = "localServerName";
    public static final String PARAM_TABLE_NAME = "tableName";
    public static final String PARAM_METAOBJECT = "metaObject";
    public static final String PARAM_METACLASS = "metaClass";
    public static final String PARAM_OBJECT_ID = "objectID";

    private final transient CallServerService callserver;

    public RESTfulSerialInterface()
    {
        callserver = StartProxy.getInstance().getCallServer();
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
        return Response.ok(Converter.serialiseToBase64(o)).build();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   domainName  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void getRoots(User user, String domainName) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node[] getRoots(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   usr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node[] getChildren(Node node, User usr) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node addNode(Node node, Link parent, User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean deleteNode(Node node, User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean addLink(Node from, Node to, User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean deleteLink(Node from, Node to, User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user        DOCUMENT ME!
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  DataRetrievalException         DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public DataObject getDataObject(User user, MetaObject metaObject) throws RemoteException, DataRetrievalException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  DataRetrievalException         DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public DataObject[] getDataObject(User user, Query query) throws RemoteException, DataRetrievalException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/GET/getDomains")
    @Produces("application/octet-stream")
    public Response getDomainsGET() {
        try {
            return createResponse(callserver.getDomains());
        } catch (final Exception ex) {
            final String message = "could not get domains";
            LOG.error(message, ex);
            throw new WebApplicationException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @POST
    @Path("/POST/getDomains")
    @Produces("application/octet-stream")
    public Response getDomainsPOST() {
        try {
            return createResponse(callserver.getDomains());
        } catch (final Exception ex) {
            final String message = "could not get domains";
            LOG.error(message, ex);
            throw new WebApplicationException(ex);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node getMetaObjectNode(User usr, int nodeID, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node[] getMetaObjectNode(User usr, String query) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Node[] getMetaObjectNode(User usr, Query query) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public MetaObject[] getMetaObject(User usr, String query) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usr    DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public MetaObject[] getMetaObject(User usr, Query query) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getMetaObject")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getMetaObject(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_OBJECT_ID) String objectIDBytes,
            @QueryParam(PARAM_CLASS_IDS) String classIDBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int objectID = Converter.deserialiseFromString(objectIDBytes, int.class);
            final int classID = Converter.deserialiseFromString(classIDBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getMetaObject(user, objectID, classID, domain));

        } catch (Exception e) {
            final String message = "could not get metaObject"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public MetaObject insertMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public int insertMetaObject(User user, Query query, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public int updateMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/deleteMetaObject")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response deleteMetaObject(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_METAOBJECT) String metaObjectBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaObject metaObject = Converter.deserialiseFromString(metaObjectBytes, MetaObject.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.deleteMetaObject(user, metaObject, domain));

        } catch (Exception e) {
            final String message = "could not delete metaObject"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/update")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response update(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_QUERY) String queryBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.update(user, query, domain));

        } catch (Exception e) {
            final String message = "could not update query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getInstance")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getInstance(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_METACLASS) String cBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final MetaClass metaClass = Converter.deserialiseFromString(cBytes, MetaClass.class);
            return createResponse(callserver.getInstance(user, metaClass));

        } catch (Exception e) {
            final String message = "could not get metaClass instance"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getClassByTableName")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getClassByTableName(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_TABLE_NAME) String tableNameBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String tableName = Converter.deserialiseFromString(tableNameBytes, String.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getClassByTableName(user, tableName, domain));

        } catch (Exception e) {
            final String message = "could not get class by table name"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getClass")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getClass(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_CLASS_IDS) String classIdBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getClass(user, classId, domain));

        } catch (Exception e) {
            final String message = "could not get class"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getClasses")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getClasses(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);
            return createResponse(callserver.getClasses(user, domain));

        } catch (Exception e) {
            final String message = "could not get classes"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
//    public Node[] getClassTreeNodes(User user) throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getClassTreeNodes")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getClassTreeNodes(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);

            if (domainBytes == null) {
                return createResponse(callserver.getClassTreeNodes(user));
            } else {
                final String domain = Converter.deserialiseFromString(domainBytes, String.class);
                return createResponse(callserver.getClassTreeNodes(user, domain));
            }

        } catch (Exception e) {
            final String message = "could not get ClassTreeNodes"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
//    public MethodMap getMethods(User user) throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getMethods")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getMethods(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_LOCAL_SERVER_NAME) String localServerNameBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);

            if (localServerNameBytes == null) {
                return createResponse(callserver.getMethods(user));
            } else {
                final String localServerName = Converter.deserialiseFromString(localServerNameBytes, String.class);
                return createResponse(callserver.getMethods(user, localServerName));
            }

        } catch (Exception e) {
            final String message = "could not get methods"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/getAllLightweightMetaObjectsForClass")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getAllLightweightMetaObjectsForClass(
            @QueryParam(PARAM_CLASS_IDS) String classIdBytes,
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_REP_FIELDS) String representationFieldsBytes,
            @QueryParam(PARAM_REP_PATTERN) String representationPatternBytes) {


        try {

            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String[] representationFields =
                    Converter.deserialiseFromString(representationFieldsBytes, String[].class);

            if (representationPatternBytes == null) {
                return createResponse(callserver.getAllLightweightMetaObjectsForClass(
                        classId, user, representationFields));
            } else {
                final String representationPattern =
                        Converter.deserialiseFromString(representationPatternBytes, String.class);

                return createResponse(callserver.getAllLightweightMetaObjectsForClass(
                        classId, user, representationFields, representationPattern));
            }
        } catch (Exception e) {
            final String message = "could not get LightweightMetaObjects for class"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
//    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
//            int classId,
//            User user,
//            String[] representationFields) throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/getLightweightMetaObjectsByQuery")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getLightweightMetaObjectsByQuery(
            @QueryParam(PARAM_CLASS_IDS) String classIdBytes,
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_QUERY_NAME) String queryBytes,
            @QueryParam(PARAM_REP_FIELDS) String representationFieldsBytes,
            @QueryParam(PARAM_REP_PATTERN) String representationPatternBytes) {

        try {

            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String query = Converter.deserialiseFromString(queryBytes, String.class);
            final String[] representationFields =
                    Converter.deserialiseFromString(representationFieldsBytes, String[].class);

            if (representationPatternBytes == null) {
                return createResponse(callserver.getLightweightMetaObjectsByQuery(classId,
                        user, query, representationFields));
            } else {
                final String representationPattern =
                        Converter.deserialiseFromString(representationPatternBytes, String.class);

                return createResponse(callserver.getLightweightMetaObjectsByQuery(
                        classId, user, query, representationFields, representationPattern));
            }

        } catch (Exception e) {
            final String message = "could not get LightweightMetaObjects by query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
//    @GET
//    @Path("/GET/getLightweightMetaObjectsByQuery")
//    @Consumes("application/octet-stream")
//    @Produces("application/octet-stream")
//    public Response getLightweightMetaObjectsByQuery(
//            @QueryParam(PARAM_CLASS_IDS) String classIdBytes,
//            @QueryParam(PARAM_USER) String userBytes,
//            @QueryParam(PARAM_QUERY_NAME) String queryBytes,
//            @QueryParam(PARAM_REP_FIELDS) String representationFieldsBytes) {
//
//        try {
//
//            final int classId = Converter.deserialiseFromString(classIdBytes, int.class);
//            final User user = Converter.deserialiseFromString(userBytes, User.class);
//            final String query = Converter.deserialiseFromString(queryBytes, String.class);
//            final String[] representationFields =
//                    Converter.deserialiseFromString(representationFieldsBytes,String[].class);
//
//            return createResponse(callserver.getLightweightMetaObjectsByQuery(classId,
//                    user, query, representationFields));
//
//        } catch (Exception e) {
//            final String message = "could not store query"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }
//    }
    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/storeQuery")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response storeQuery(
            @QueryParam(PARAM_USER) String userBytes,
            @QueryParam(PARAM_QUERY_DATA) String dataBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final QueryData data = Converter.deserialiseFromString(dataBytes, QueryData.class);

            return createResponse(callserver.storeQuery(user, data));

        } catch (Exception e) {
            final String message = "could not store query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }

    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    public Response getQueryInfos(User userBytes) {
        throw new UnsupportedOperationException("Not supported yet.");
//        try {
//
//            final User user = Converter.deserialiseFromString(userBytes, User.class);
//
//            return createResponse(callserver.getQueryInfos(user));
//
//        } catch (Exception e) {
//            final String message = "could not get query infos"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    public Response getQueryInfos(UserGroup userGroupBytes) {
        throw new UnsupportedOperationException("Not supported yet.");
//        try {
//
//            final UserGroup userGroup = Converter.deserialiseFromString(userGroupBytes, UserGroup.class);
//
//            return createResponse(callserver.getQueryInfos(userGroup));
//
//        } catch (Exception e) {
//            final String message = "could not get query infos"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }

    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/getQuery")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getQuery(
            @QueryParam(PARAM_QUERY_ID) String idBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final int id = Converter.deserialiseFromString(idBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getQuery(id, domain));

        } catch (Exception e) {
            final String message = "could not get query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/delete")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response delete(
            @QueryParam(PARAM_QUERY_ID) String idBytes,
            @QueryParam(PARAM_DOMAIN) String domainBytes) {

        try {

            final int id = Converter.deserialiseFromString(idBytes, int.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.delete(id, domain));

        } catch (Exception e) {
            final String message = "could not delete query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/addQuery")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response addQuery(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_NAME) final String nameBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @QueryParam(PARAM_STATEMENT) final String statementBytes,
            @QueryParam(PARAM_RESULT_TYPE) final String resultTypeBytes,
            @QueryParam(PARAM_IS_UPDATE) final String isUpdateBytes,
            @QueryParam(PARAM_IS_BATCH) final String isBatchBytes,
            @QueryParam(PARAM_IS_ROOT) final String isRootBytes,
            @QueryParam(PARAM_IS_UNION) final String isUnionBytes) {

        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String name = Converter.deserialiseFromString(nameBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
            final String statement = Converter.deserialiseFromString(statementBytes, String.class);

            if (resultTypeBytes == null && isUpdateBytes == null
                    && isBatchBytes == null && isRootBytes == null && isUnionBytes == null) {
                return createResponse(callserver.addQuery(user, name, description, statement));
            } else {

                final int resultType = Converter.deserialiseFromString(resultTypeBytes, int.class);
                final char isUpdate = Converter.deserialiseFromString(isUpdateBytes, char.class);
                final char isBatch = Converter.deserialiseFromString(isBatchBytes, char.class);
                final char isRoot = Converter.deserialiseFromString(isRootBytes, char.class);
                final char isUnion = Converter.deserialiseFromString(isUnionBytes, char.class);

                return createResponse(callserver.addQuery(user, name, description,
                        statement, resultType, isUpdate, isBatch, isRoot, isUnion));
            }

        } catch (Exception e) {
            final String message = "could not add query"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
//    @GET
//    @Path("/GET/addQuery")
//    @Consumes("application/octet-stream")
//    @Produces("application/octet-stream")
//    public Response addQuery(
//            @QueryParam(PARAM_USER) final String userBytes,
//            @QueryParam(PARAM_QUERY_NAME) final String nameBytes,
//            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
//            @QueryParam(PARAM_STATEMENT) final String statementBytes) {
//
//        try {
//            final User user = Converter.deserialiseFromString(userBytes, User.class);
//            final String name = Converter.deserialiseFromString(nameBytes, String.class);
//            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
//            final String statement = Converter.deserialiseFromString(statementBytes, String.class);
//
//            return createResponse(callserver.addQuery(user, name, description, statement));
//
//        } catch (Exception e) {
//            final String message = "could not add query parameter"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }
//
//    }
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/addQueryParameter")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response addQueryParameter(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_QUERY_ID) final String queryIdBytes,
            @QueryParam(PARAM_TYPE_ID) final String typeIdBytes,
            @QueryParam(PARAM_PARAM_KEY) final String paramkeyBytes,
            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes,
            @QueryParam(PARAM_QUERY_RESULT) final String isQueryResultBytes,
            @QueryParam(PARAM_QUERY_POSITION) final String queryPositionBytes) {

        try {

            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final int queryId = Converter.deserialiseFromString(queryIdBytes, int.class);
            final String paramkey = Converter.deserialiseFromString(paramkeyBytes, String.class);
            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);

            if (typeIdBytes == null && isQueryResultBytes == null && queryPositionBytes == null) {

                return createResponse(callserver.addQueryParameter(user, queryId, paramkey, description));

            } else {

                final int typeId = Converter.deserialiseFromString(typeIdBytes, int.class);
                final char isQueryResult = Converter.deserialiseFromString(isQueryResultBytes, char.class);
                final int queryPosition = Converter.deserialiseFromString(queryPositionBytes, int.class);

                return createResponse(callserver.addQueryParameter(user, queryId,
                        typeId, paramkey, description, isQueryResult, queryPosition));
            }



        } catch (Exception e) {
            final String message = "could not add query parameter"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException                DOCUMENT ME!
     */
//    @GET
//    @Path("/GET/addQueryParameter")
//    @Consumes("application/octet-stream")
//    @Produces("application/octet-stream")
//    public Response addQueryParameter(
//            @QueryParam(PARAM_USER) final String userBytes,
//            @QueryParam(PARAM_QUERY_ID) final String queryIdBytes,
//            @QueryParam(PARAM_PARAM_KEY) final String paramkeyBytes,
//            @QueryParam(PARAM_DESCRIPTION) final String descriptionBytes) {
//
//        try {
//
//            final User user = Converter.deserialiseFromString(userBytes, User.class);
//            final int queryID = Converter.deserialiseFromString(queryIdBytes, int.class);
//            final String paramkey = Converter.deserialiseFromString(paramkeyBytes, String.class);
//            final String description = Converter.deserialiseFromString(descriptionBytes, String.class);
//
//            return createResponse(callserver.addQueryParameter(user, queryID, paramkey, description));
//
//        } catch (Exception e) {
//            final String message = "could not add query parameter"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }
//    }
//    /**
//     * DOCUMENT ME!
//     *
//     * @param   user  DOCUMENT ME!
//     *
//     * @return  DOCUMENT ME!
//     *
//     * @throws  WebApplicationException                DOCUMENT ME!
//     */
//    @GET
//    @Path("/GET/getSearchOptions")
//    @Consumes("application/octet-stream")
//    @Produces("application/octet-stream")
//    public Response getSearchOptions(@QueryParam(PARAM_USER) final String userBytes) {
//        try {
//
//            final User user = Converter.deserialiseFromString(userBytes, User.class);
//
//            return createResponse(callserver.getSearchOptions(user));
//
//        } catch (final Exception e) {
//            final String message = "could not get search options"; // NOI18N
//            LOG.error(message, e);
//            throw new WebApplicationException(e);
//        }
//    }
    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException                DOCUMENT ME!
     */
    @GET
    @Path("/GET/getSearchOptions")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getSearchOptions(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes) {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);

            if (domainBytes == null) {
                return createResponse(callserver.getSearchOptions(user));
            } else {
                final String domain = Converter.deserialiseFromString(domainBytes, String.class);
                return createResponse(callserver.getSearchOptions(user, domain));
            }
        } catch (final Exception e) {
            final String message = "could not get search options"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
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
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/GET/search")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response searchGET(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_CLASS_IDS) final String classIdsBytes,
            @QueryParam(PARAM_SEARCH_OPTIONS) final String searchOptionsBytes) {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String[] classIds = Converter.deserialiseFromString(classIdsBytes, String[].class);
            final SearchOption[] options = Converter.deserialiseFromString(searchOptionsBytes, SearchOption[].class);

            return createResponse(callserver.search(user, classIds, options));
        } catch (final Exception e) {
            final String message = "could not perform search"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lsNameBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/GET/getDefaultIcons")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getDefaultIconsGET(@QueryParam(PARAM_LS_NAME) final String lsNameBytes) {
        try {
            final String lsName = Converter.deserialiseFromString(lsNameBytes, String.class);

            return createResponse(callserver.getDefaultIcons(lsName));
        } catch (final Exception ex) {
            final String message = "could not get default icons"; // NOI18N
            LOG.error(message, ex);
            throw new WebApplicationException(ex);
        }
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @return  DOCUMENT ME!
//     *
//     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
//     */
//    @GET
//    @Path("/GET/getDefaultIcons")
//    @Produces("application/octet-stream")
//    public String getDefaultIconsGET() {
//        try {
//            return Converter.serialiseToString(callserver.getDefaultIcons());
//        } catch (IOException ex) {
//            final String message = "could not get default icons"; // NOI18N
//            LOG.error(message, ex);
//            throw new WebApplicationException(ex);
//        }
//    }
    /**
     * DOCUMENT ME!
     *
     * @param   userBytes         user DOCUMENT ME!
     * @param   oldPasswordBytes  DOCUMENT ME!
     * @param   newPasswordBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/GET/changePassword")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response changePasswordGET(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_OLD_PASSWORD) final String oldPasswordBytes,
            @QueryParam(PARAM_NEW_PASSWORD) final String newPasswordBytes) {
        try {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String oldPassword = Converter.deserialiseFromString(oldPasswordBytes, String.class);
            final String newPassword = Converter.deserialiseFromString(newPasswordBytes, String.class);

            final boolean changed = callserver.changePassword(user, oldPassword, newPassword);
            final Response response = createResponse(changed);
            return response;
        } catch (final Exception ex) {
            final String message = "could not change password"; // NOI18N
            LOG.error(message, ex);
            throw new WebApplicationException(ex);
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
     * @throws  WebApplicationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getUser")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getUserGET(
            @QueryParam(PARAM_USERGROUP_LS_NAME) final String ugLsNameBytes,
            @QueryParam(PARAM_USERGROUP_NAME) final String ugNameBytes,
            @QueryParam(PARAM_USER_LS_NAME) final String uLsNameBytes,
            @QueryParam(PARAM_USERNAME) final String unameBytes,
            @QueryParam(PARAM_PASSWORD) final String passwordBytes) {
        try {
            final String ugLsName = Converter.deserialiseFromString(ugLsNameBytes, String.class);
            final String ugName = Converter.deserialiseFromString(ugNameBytes, String.class);
            final String uLsName = Converter.deserialiseFromString(uLsNameBytes, String.class);
            final String uname = Converter.deserialiseFromString(unameBytes, String.class);
            final String password = Converter.deserialiseFromString(passwordBytes, String.class);

            return createResponse(callserver.getUser(ugLsName, ugName, uLsName, uname, password));
        } catch (final Exception e) {
            final String message = "could not get user"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  WebApplicationException  RemoteException DOCUMENT ME!
     */
    @GET
    @Path("/GET/getUserGroupNames")
    @Produces("application/octet-stream")
    public Response getUserGroupNamesGET() {
        try {
            return createResponse(callserver.getUserGroupNames());
        } catch (final Exception ex) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, ex);
            throw new WebApplicationException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @POST
    @Path("/POST/getUserGroupNames")
    @Produces("application/octet-stream")
    public Response getUserGroupNamesPOST() {
        return getUserGroupNamesGET();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   unameBytes   DOCUMENT ME!
     * @param   lsHomeBytes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException          DOCUMENT ME!
     * @throws  WebApplicationException  UnsupportedOperationException DOCUMENT ME!
     */
    @GET
    @Path("/GET/getUserGroupNames")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getUserGroupNamesGET(
            @QueryParam(PARAM_USERNAME) final String unameBytes,
            @QueryParam(PARAM_LS_HOME) final String lsHomeBytes) throws RemoteException {
        try {
            final String uname = Converter.deserialiseFromString(unameBytes, String.class);
            final String lsHome = Converter.deserialiseFromString(lsHomeBytes, String.class);

            return createResponse(callserver.getUserGroupNames(uname, lsHome));
        } catch (final Exception e) {
            final String message = "could not get usergroup names"; // NOI18N
            LOG.error(message, e);
            throw new WebApplicationException(e);
        }
    }
}
