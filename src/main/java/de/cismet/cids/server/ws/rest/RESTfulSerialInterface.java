/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws.rest;

import Sirius.server.ServerExitError;
import Sirius.server.dataretrieval.DataObject;
import Sirius.server.dataretrieval.DataRetrievalException;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.middleware.impls.proxy.StartProxy;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.search.Query;
import Sirius.server.search.SearchOption;
import Sirius.server.search.store.Info;
import Sirius.server.search.store.QueryData;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.ws.Converter;

import java.io.IOException;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Map;

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

    private static RESTfulSerialInterface instance;

    public static final String PARAM_DOMAIN = "domain";

    //~ Instance fields --------------------------------------------------------

    private final transient CallServerService callserver;
    private final transient int port;
//    private final transient Server server;
    private final transient SelectorThread selector;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTfulSerialInterface object.
     *
     * @param   port  callserver DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public RESTfulSerialInterface(final int port) throws ServerExitError {
        final StartProxy proxy = StartProxy.getInstance();
        this.callserver = proxy.getCallServer();
        this.port = port;

        final String baseURI = "http://" + proxy.getServer().getIP() + ":" + port + "/";            // NOI18N
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "de.cismet.cids.server.ws.rest"); // NOI18N

//        final ServletHolder servlet = new ServletHolder(ServletContainer.class);
//        servlet.setInitParameters(initParams);
//
//        server = new Server(port);
//        final Context context = new Context(server, "/", Context.SESSIONS);
//        context.addServlet(servlet, "/*");

        try {
            selector = GrizzlyWebContainerFactory.create(baseURI, initParams);
//            server.start();
        } catch (final Exception ex) {
            final String message = "could not create jetty web container on port: " + port; // NOI18N
            LOG.error(message, ex);
            throw new ServerExitError(message, ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   port  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public static synchronized void up(final int port) throws ServerExitError {
        if (!isUp()) {
            instance = new RESTfulSerialInterface(port);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static synchronized void down() {
        if (isUp()) {
            try {
//                instance.server.stop();
                instance.selector.stopEndpoint();
            } catch (final Exception ex) {
//                LOG.warn("could not stop jetty", ex);
                LOG.warn("could not stop grizzly", ex);
            }
            instance = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized boolean isUp() {
        return instance != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized int getPort() {
        if (isUp()) {
            return instance.port;
        } else {
            return -1;
        }
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
    public MetaObject getMetaObject(User usr, int objectID, int classID, String domain) throws RemoteException {
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
    public int deleteMetaObject(User user, MetaObject metaObject, String domain) throws RemoteException {
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
    public int update(User user, String query, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public MetaObject getInstance(User user, MetaClass c) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public MetaClass getClass(User user, int classID, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public MetaClass getClassByTableName(User user, String tableName, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public MetaClass[] getClasses(User user, String domain) throws RemoteException {
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
    public Node[] getClassTreeNodes(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public Node[] getClassTreeNodes(User user, String domain) throws RemoteException {
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
    public MethodMap getMethods(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
    public MethodMap getMethods(User user, String localServerName) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classId,
            User user,
            String[] representationFields) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean storeQuery(User user, QueryData data) throws RemoteException {
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
    public Info[] getQueryInfos(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userGroup  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public Info[] getQueryInfos(UserGroup userGroup) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public QueryData getQuery(int id, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id      DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean delete(int id, String domain) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public int addQuery(
            User user,
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public int addQuery(User user, String name, String description, String statement) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean addQueryParameter(
            User user,
            int queryId,
            int typeId,
            String paramkey,
            String description,
            char isQueryResult,
            int queryPosition) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean addQueryParameter(User user, int queryId, String paramkey, String description)
        throws RemoteException {
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
    public HashMap getSearchOptions(User user) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   userBytes  user DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException                DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    @GET
    @Path("/GET/getSearchOptions")
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getSearchOptions(
            @QueryParam(PARAM_USER) final String userBytes,
            @QueryParam(PARAM_DOMAIN) final String domainBytes){
        try
        {
            final User user = Converter.deserialiseFromString(userBytes, User.class);
            final String domain = Converter.deserialiseFromString(domainBytes, String.class);

            return createResponse(callserver.getSearchOptions(user, domain));
        }catch(final Exception e)
        {
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
