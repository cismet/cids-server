/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver;

import Sirius.server.Shutdown;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.history.HistoryServer;
import Sirius.server.localserver.method.MethodCache;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.object.ObjectFactory;
import Sirius.server.localserver.object.PersistenceManager;
import Sirius.server.localserver.tree.AbstractTree;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.tree.VirtualTree;
import Sirius.server.localserver.user.UserStore;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.User;
import Sirius.server.newuser.permission.PolicyHolder;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class DBServer extends Shutdown implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DBServer.class);

    //~ Instance fields --------------------------------------------------------

    // DefaultObject of the inner class for updates and inserts etc. of the meta data base
    private final PersistenceManager objectPersistence;
    /** The Navigational structure of a localserver. */
    private final AbstractTree tree;
    /** Holds several connection to ths ls' db and other used dbs. */
    private final transient DBConnectionPool connectionPool;

    /** Holds and delivers a localsservers objects. */
    private final ObjectFactory objects;
    /** Holds and delivers a localsservers classes. */
    private final ClassCache classes;
    /** Holds and delivers a localsservers methods. */
    private final MethodCache methods;
    /** To check user rights. */
    private final UserStore userstore;
    /** To check policies. */
    private final PolicyHolder policyHolder;
    /** provides object history.* */
    private final HistoryServer historyServer;
    /** Initial server-settings. */
    private final ServerProperties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public DBServer(final ServerProperties properties) throws Throwable {
        this.properties = properties;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Info :: DBServer instantiate connectionPool"); // NOI18N
        }

        connectionPool = new DBConnectionPool(properties);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServer connectionPool instantiated :: Instantiate PolicyHolder "); // NOI18N
        }

        policyHolder = new PolicyHolder(connectionPool, properties.getInternalDialect());
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServer PolicyHolder instantiated :: Instantiate ClassCache "); // NOI18N
        }
        // needs tob instaniated before the OcjectFactory
        classes = new ClassCache(connectionPool, properties, policyHolder);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServer ClassCache instantiated :: Instantiate ObjectFactory "); // NOI18N
        }

        objects = new ObjectFactory(connectionPool, classes);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServerObjectFactory instantiated :: Instantiate Tree "); // NOI18N
        }

        tree = new VirtualTree(connectionPool, properties, policyHolder, classes);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServer Tree instantiated :: Instantiate methodCache "); // NOI18N
        }

        methods = new MethodCache(connectionPool, properties);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DBServer MethodCache instantiated :: Instantiate UserService "); // NOI18N
        }

        userstore = new UserStore(connectionPool, properties);

        historyServer = new HistoryServer(this);

        objectPersistence = new PersistenceManager(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------
     *
     * @param   classID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass getClass(final int classID) {
        return new MetaClass(classes.getClass(classID), getDomain());
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass[] getClasses() throws Throwable {
        final List tmpClasses = classes.getAllClasses();

        final MetaClass[] middleWareClasses = new MetaClass[tmpClasses.size()];

        for (int i = 0; i < tmpClasses.size(); i++) {
            middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class)tmpClasses.get(i),
                    getDomain());
        }

        return middleWareClasses;
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @param   u        DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass getClass(final User u, final int classID) {
        final Sirius.server.localserver._class.Class c = classes.getClass(u, classID);
        if (c != null) {
            return new MetaClass(c, getDomain());
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u          DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass getClassByTableName(final User u, final String tableName) throws Throwable {
        final Sirius.server.localserver._class.Class c = classes.getClassNyTableName(u, tableName);
        if (c != null) {
            return new MetaClass(c, getDomain());
        } else {
            return null;
        }
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaClass[] getClasses(final User u) {
        final List tmpClasses = classes.getAllClasses(u);
        MetaClass[] middleWareClasses = null;

        if (tmpClasses != null) {
            middleWareClasses = new MetaClass[tmpClasses.size()];

            for (int i = 0; i < tmpClasses.size(); i++) {
                middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class)tmpClasses.get(i),
                        getDomain());
            }
        }

        return middleWareClasses;
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public ClassCache getClassCache() {
        return classes;
    }
    /**
     * ---------------------------------------------------------------------------- public NodeReferenceList
     * getChildren(int nodeID,UserGroup ug) throws Throwable { return tree.getChildren(nodeID,ug); }.
     *
     * @param   node  DOCUMENT ME!
     * @param   u     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getChildren(final Node node, final User u) throws Throwable {
        return tree.getChildren(node, u);
    }
    /**
     * ----------------------------------------------------------------------------- public
     * Sirius.server.middleware.types.Node[] getParents(int nodeID,UserGroup ug) throws Throwable { return new
     * Sirius.server.middleware.types.NodeReferenceList(tree.getParents(nodeID,ug),this,ug).getNodes(); }
     * -------------------------------------------------------------------------
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getTops(final User u) throws Throwable {
        return new NodeReferenceList(tree.getTopNodes(u));
    }
    /**
     * -------------------------------------------------------------------------
     *
     * @param   u  DOCUMENT ME!git status
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getClassTreeNodes(final User u) throws Throwable {
        return new NodeReferenceList(tree.getClassTreeNodes(u));
    }
    /**
     * ----------------------------------------------------------------------------
     *
     * @param   objectID  DOCUMENT ME!
     * @param   usr       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  Throwable DOCUMENT ME!
     */
    public MetaObject getObject(final String objectID, final User usr) throws SQLException {
        final int oId;
        final int cId;

        final String[] ids = objectID.split("@"); // NOI18N
        oId = new Integer(ids[0]).intValue();
        cId = new Integer(ids[1]).intValue();

        // An dieser Stelle wird die Referenz neu gesetzt. Deshalb funzt getParent() der ObjectAttributes nicht richtig
        // zusaetzlich erzeugt auch die filter Methode eine neue Adresse
        final Sirius.server.localserver.object.Object o = objects.getObject(oId, cId, usr);

        if (o != null) {
            final MetaObject mo = new DefaultMetaObject(o.filter(usr), getDomain(), usr);
            // mo.setMetaClass(new MetaClass(classes.getClass(cId), getDomain()));

            mo.setAllClasses(classes.getClassHashMap());

            return mo;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectIDs  DOCUMENT ME!
     * @param   usr        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaObject[] getObjects(final String[] objectIDs, final User usr) throws Throwable {
        final MetaObject[] obs = new MetaObject[objectIDs.length];

        for (int i = 0; i < objectIDs.length; i++) {
            obs[i] = getObject(objectIDs[i], usr);
        }

        return obs;
    }
    /**
     * //bugfix public Node getNode(Node node, UserGroup ug) throws Throwable { if(node instanceof
     * Sirius.server.localserver.tree.node.ObjectNode) { Sirius.server.localserver.tree.node.ObjectNode newNode =
     * (Sirius.server.localserver.tree.node.ObjectNode)node; return new MetaObjectNode( newNode,getDomain()); } else if
     * (node instanceof Sirius.server.localserver.tree.node.ClassNode) return new
     * MetaClassNode((Sirius.server.localserver.tree.node.ClassNode)node,getDomain()); return new
     * MetaNode(node,getDomain()); } -----------------------------------------------------------------
     *
     * @param   ids  DOCUMENT ME!
     * @param   u    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public Sirius.server.middleware.types.Node[] getNodes(final int[] ids, final User u) throws Throwable {
        final Sirius.server.middleware.types.Node[] n = new Sirius.server.middleware.types.Node[ids.length];

        for (int i = 0; i < ids.length; i++) {
            n[i] = tree.getNode(ids[i], u);
        }

        return n;
    }

    /**
     * public Sirius.server.localserver.tree.NodeReferenceList getObjectNodes(String[] objectIDs,UserGroup ug) throws
     * Throwable { //estimated 2 Nodes per object in average ! java.util.ArrayList<Node> v = new
     * java.util.ArrayList<Node>(objectIDs.length * 2); for(int i =0;i< objectIDs.length;i++) {
     * v.addAll(tree.getObjectNodes(objectIDs[i],ug)); } return new Sirius.server.localserver.tree.NodeReferenceList(v);
     * } ----------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public AbstractTree getTree() throws Throwable {
        return tree;
    }
    /**
     * -------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public DBConnection getActiveDBConnection() {
        return connectionPool.getDBConnection();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DBConnectionPool getConnectionPool() {
        return connectionPool;
    }
    /**
     * public final Connection getTranslConnection(){return
     * connectionPool.getDBConnection("transl").getDBConnection();}.
     *
     * @return  DOCUMENT ME!
     */
    public ServerProperties getSystemProperties() {
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MethodMap getMethods() {
        return methods.getMethods();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MethodMap getMethods(final User u) throws Throwable {
        return methods.getMethods(u);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public UserStore getUserStore() {
        return userstore;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ObjectFactory getObjectFactory() {
        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PersistenceManager getObjectPersitenceManager() {
        return objectPersistence;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ServerProperties getProperties() {
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HistoryServer getHistoryServer() {
        return historyServer;
    }

    /**
     * ---!!!
     *
     * @param   classID                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classID,
            final User user,
            final String[] representationFields,
            final String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classes.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(
                c,
                user,
                findAllStmnt.toString(),
                representationFields,
                new StringPatternFormater(representationPattern, representationFields));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classID               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(final int classID,
            final User user,
            final String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classes.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(c, user, findAllStmnt.toString(), representationFields, null);
    }

    /**
     * ---!!!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields,
            final String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classes.getClass(classId);
        return getLightweightMetaObjectsByQuery(
                c,
                user,
                query,
                representationFields,
                new StringPatternFormater(representationPattern, representationFields));
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
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(final int classId,
            final User user,
            final String query,
            final String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classes.getClass(classId);
        return getLightweightMetaObjectsByQuery(c, user, query, representationFields, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c                     DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   formater              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final Sirius.server.localserver._class.Class c,
            final User user,
            final String query,
            final String[] representationFields,
            final AbstractAttributeRepresentationFormater formater) throws SQLException {
        final String primaryKeyField = c.getPrimaryKey();
        if (LOG.isDebugEnabled()) {
            LOG.debug("LightweightMO by Query: " + query); // NOI18N
        }
        Statement stmnt = null;
        ResultSet rs = null;

        try {
            stmnt = this.getActiveDBConnection().getConnection().createStatement();
            rs = stmnt.executeQuery(query);

            final Set<LightweightMetaObject> lwMoSet = new LinkedHashSet<LightweightMetaObject>();
            while (rs.next()) {
                final Map<String, java.lang.Object> attributeMap = new HashMap<String, java.lang.Object>();
                // primary key must be returned by the query!
                final int oID = rs.getInt(primaryKeyField);
                attributeMap.put(primaryKeyField, oID);
                final java.lang.Object[] repObjs = new java.lang.Object[representationFields.length];
                for (int i = 0; i < repObjs.length; ++i) {
                    final String fld = representationFields[i];
                    final java.lang.Object retAttrVal = checkSerializabilityAndMakeSerializable(rs.getObject(fld));
                    attributeMap.put(fld.toLowerCase(), retAttrVal);
                    repObjs[i] = retAttrVal;
                }
                lwMoSet.add(new LightweightMetaObject(c.getID(), oID, getDomain(), user, attributeMap, formater));
            }

            return lwMoSet.toArray(new LightweightMetaObject[lwMoSet.size()]);
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmnt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c                     DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String createFindAllQueryForClassID(final Sirius.server.localserver._class.Class c,
            final String[] representationFields) {
        final String primaryKeyField = c.getPrimaryKey();
        final ClassAttribute sortingColumnAttribute = c.getClassAttribute("sortingColumn"); // NOI18N
        final StringBuilder findAllStmnt = new StringBuilder("select " + primaryKeyField);  // NOI18N

        for (int i = 0; i < representationFields.length; ++i) {
            findAllStmnt.append(", "); // NOI18N
            final String field = representationFields[i];
            findAllStmnt.append(field);
        }

        findAllStmnt.append(" from "); // NOI18N
        findAllStmnt.append(c.getTableName());

        if (sortingColumnAttribute != null) {
            findAllStmnt.append(" order by ").append(sortingColumnAttribute.getValue()); // NOI18N
        }

        return findAllStmnt.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private java.lang.Object checkSerializabilityAndMakeSerializable(final java.lang.Object o) {
        if ((o == null) || (o instanceof Serializable)) {
            return o;
        } else {
            return o.toString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return properties.getServerName();
    }
}
