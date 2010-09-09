/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver;

import Sirius.server.Shutdown;
import Sirius.server.Shutdownable;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.localserver.method.MethodCache;
import Sirius.server.localserver.method.MethodMap;
import Sirius.server.localserver.object.ObjectFactory;
import Sirius.server.localserver.object.PersistenceManager;
import Sirius.server.localserver.tree.AbstractTree;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.tree.VirtualTree;
import Sirius.server.localserver.user.UserStore;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.PolicyHolder;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBServer extends Shutdown implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DBServer.class);


    //~ Instance fields --------------------------------------------------------

    // DefaultObject of the inner class for updates and inserts etc. of the meta data base
    protected PersistenceManager objectPersistence;
    /** The Navigational structure of a localserver. */
    protected AbstractTree tree;
    /** Holds several connection to ths ls' db and other used dbs. */
    transient DBConnectionPool connectionPool;

    /** Holds and delivers a localsservers objects. */
    private ObjectFactory objects;
    /** Holds and delivers a localsservers classes. */
    private ClassCache classes;
    /** Holds and delivers a localsservers methods. */
    private MethodCache methods;
    /** To check user rights. */
    private UserStore userstore;
    /** To check policies. */
    private PolicyHolder policyHolder;
    /** Initial server-settings. */
    private ServerProperties properties;

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

        policyHolder = new PolicyHolder(connectionPool);
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

        objectPersistence = new PersistenceManager(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------
     *
     * @param   classID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass getClass(final int classID) throws Throwable {
        return new MetaClass(classes.getClass(classID), properties.getServerName());
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass[] getClasses() throws Throwable {
        final Vector tmpClasses = classes.getAllClasses();

        final MetaClass[] middleWareClasses = new MetaClass[tmpClasses.size()];

        for (int i = 0; i < tmpClasses.size(); i++) {
            middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class)tmpClasses.get(i),
                    properties.getServerName());
        }

        return middleWareClasses;
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @param   ug       DOCUMENT ME!
     * @param   classID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass getClass(final UserGroup ug, final int classID) throws Throwable {
        final Sirius.server.localserver._class.Class c = classes.getClass(ug, classID);
        if (c != null) {
            return new MetaClass(c, properties.getServerName());
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug         DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass getClassByTableName(final UserGroup ug, final String tableName) throws Throwable {
        final Sirius.server.localserver._class.Class c = classes.getClassNyTableName(ug, tableName);
        if (c != null) {
            return new MetaClass(c, properties.getServerName());
        } else {
            return null;
        }
    }
    /**
     * ---------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaClass[] getClasses(final UserGroup ug) throws Throwable {
        final Vector tmpClasses = classes.getAllClasses(ug);
        MetaClass[] middleWareClasses = null;

        if (tmpClasses != null) {
            middleWareClasses = new MetaClass[tmpClasses.size()];

            for (int i = 0; i < tmpClasses.size(); i++) {
                middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class)tmpClasses.get(i),
                        properties.getServerName());
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
     * @param   ug    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getChildren(final Node node, final UserGroup ug) throws Throwable {
        return tree.getChildren(node, ug);
    }
    /**
     * ----------------------------------------------------------------------------- public
     * Sirius.server.middleware.types.Node[] getParents(int nodeID,UserGroup ug) throws Throwable { return new
     * Sirius.server.middleware.types.NodeReferenceList(tree.getParents(nodeID,ug),this,ug).getNodes(); }
     * -------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getTops(final UserGroup ug) throws Throwable {
        return new NodeReferenceList(tree.getTopNodes(ug));
    }
    /**
     * -------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public NodeReferenceList getClassTreeNodes(final UserGroup ug) throws Throwable {
        return new NodeReferenceList(tree.getClassTreeNodes(ug));
    }
    /**
     * ----------------------------------------------------------------------------
     *
     * @param   objectID  DOCUMENT ME!
     * @param   ug        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaObject getObject(final String objectID, final UserGroup ug) throws Throwable {
        final int oId;
        final int cId;

        final String[] ids = objectID.split("@"); // NOI18N
        oId = new Integer(ids[0]).intValue();
        cId = new Integer(ids[1]).intValue();

        // An dieser Stelle wird die Referenz neu gesetzt. Deshalb funzt getParent() der ObjectAttributes nicht richtig
        // zusaetzlich erzeugt auch die filter Methode eine neue Adresse
        final Sirius.server.localserver.object.Object o = objects.getObject(oId, cId, ug);

        if (o != null) {
            final MetaObject mo = new DefaultMetaObject(o.filter(ug), properties.getServerName());
            // mo.setMetaClass(new MetaClass(classes.getClass(cId), properties.getServerName()));

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
     * @param   ug         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public MetaObject[] getObjects(final String[] objectIDs, final UserGroup ug) throws Throwable {
        final MetaObject[] obs = new MetaObject[objectIDs.length];

        for (int i = 0; i < objectIDs.length; i++) {
            obs[i] = getObject(objectIDs[i], ug);
        }

        return obs;
    }
    /**
     * //bugfix public Node getNode(Node node, UserGroup ug) throws Throwable { if(node instanceof
     * Sirius.server.localserver.tree.node.ObjectNode) { Sirius.server.localserver.tree.node.ObjectNode newNode =
     * (Sirius.server.localserver.tree.node.ObjectNode)node; return new MetaObjectNode(
     * newNode,properties.getServerName()); } else if (node instanceof Sirius.server.localserver.tree.node.ClassNode)
     * return new MetaClassNode((Sirius.server.localserver.tree.node.ClassNode)node,properties.getServerName()); return
     * new MetaNode(node,properties.getServerName()); }
     * -----------------------------------------------------------------
     *
     * @param   ids  DOCUMENT ME!
     * @param   ug   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public Sirius.server.middleware.types.Node[] getNodes(final int[] ids, final UserGroup ug) throws Throwable {
        final Sirius.server.middleware.types.Node[] n = new Sirius.server.middleware.types.Node[ids.length];

        for (int i = 0; i < ids.length; i++) {
            n[i] = tree.getNode(ids[i], ug);
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
    public final AbstractTree getTree() throws Throwable {
        return tree;
    }
    /**
     * -------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final DBConnection getActiveDBConnection() {
        return connectionPool.getConnection();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final DBConnectionPool getConnectionPool() {
        return connectionPool;
    }
    /**
     * public final Connection getTranslConnection(){return connectionPool.getConnection("transl").getConnection();}.
     *
     * @return  DOCUMENT ME!
     */
    public final ServerProperties getSystemProperties() {
        return properties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final MethodMap getMethods() {
        return methods.getMethods();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public final MethodMap getMethods(final UserGroup ug) throws Throwable {
        return methods.getMethods(ug);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final UserStore getUserStore() {
        return userstore;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final ObjectFactory getObjectFactory() {
        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final PersistenceManager getObjectPersitenceManager() {
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
} // end class DBServer
