package Sirius.server.localserver;

import Sirius.server.sql.*;
import Sirius.server.localserver.object.*;
import Sirius.server.localserver._class.*;
//import Sirius.server.localserver.tree.node.*;

import Sirius.server.localserver.tree.*;
import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.localserver.method.*;
import Sirius.server.localserver.user.*;
import Sirius.server.newuser.*;
import java.util.*;
import Sirius.server.property.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.permission.PolicyHolder;

public class DBServer implements java.io.Serializable {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /**Holds several connection to ths ls' db and other used dbs*/
    transient DBConnectionPool connectionPool;
    /**Holds and delivers a localsservers objects*/
    private ObjectFactory objects;
    //DefaultObject of the inner class for updates and inserts etc. of the meta data base
    protected PersistenceManager objectPersistence;
    /**Holds and delivers a localsservers classes*/
    private ClassCache classes;
    /**Holds and delivers a localsservers methods*/
    private MethodCache methods;
    /**The Navigational structure of a localserver*/
    protected AbstractTree tree;
    /**To check user rights*/
    private UserStore userstore;
    /**To check policies*/
    private PolicyHolder policyHolder;
    /**Initial server-settings*/
    private ServerProperties properties;

    //----------------------------------------------------------------------------
    public DBServer(ServerProperties properties) throws Throwable {
        this.properties = properties;

        logger.debug("Info :: DBServer instantiate connectionPool");

        connectionPool = new DBConnectionPool(properties);

        logger.debug("DBServer connectionPool instantiated :: Instantiate PolicyHolder ");

        policyHolder = new PolicyHolder(connectionPool);

        logger.debug("DBServer PolicyHolder instantiated :: Instantiate ClassCache ");
        // needs tob instaniated before the OcjectFactory
        classes = new ClassCache(connectionPool, properties, policyHolder);

        logger.debug("DBServer ClassCache instantiated :: Instantiate ObjectFactory ");

        objects = new ObjectFactory(connectionPool, classes);

        logger.debug("DBServerObjectFactory instantiated :: Instantiate Tree ");

        tree = new VirtualTree(connectionPool, properties, this, policyHolder, classes);

        logger.debug("DBServer Tree instantiated :: Instantiate methodCache ");

        methods = new MethodCache(connectionPool, properties);

        logger.debug("DBServer MethodCache instantiated :: Instantiate UserService ");

        userstore = new UserStore(connectionPool, properties);

        objectPersistence = new PersistenceManager(this);



    }

    //----------------------------------------------------------------------------
    public MetaClass getClass(int classID) throws Throwable {
        return new MetaClass(classes.getClass(classID), properties.getServerName());

    }

    //---------------------------------------------------------------------------
    public MetaClass[] getClasses() throws Throwable {
        Vector tmpClasses = classes.getAllClasses();

        MetaClass[] middleWareClasses = new MetaClass[tmpClasses.size()];

        for (int i = 0; i < tmpClasses.size(); i++) {
            middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class) tmpClasses.get(i), properties.getServerName());
        }

        return middleWareClasses;

    }

    //---------------------------------------------------------------------------
    public MetaClass getClass(UserGroup ug, int classID) throws Throwable {
        Sirius.server.localserver._class.Class c = classes.getClass(ug, classID);
        if (c != null) {
            return new MetaClass(c, properties.getServerName());
        } else {
            return null;
        }

    }

    public MetaClass getClassByTableName(UserGroup ug, String tableName) throws Throwable {
        Sirius.server.localserver._class.Class c =classes.getClassNyTableName(ug, tableName);
        if (c!=null)
            return new MetaClass( c,properties.getServerName());
        else
        return null;

    }

    //---------------------------------------------------------------------------
    public MetaClass[] getClasses(UserGroup ug) throws Throwable {
        Vector tmpClasses = classes.getAllClasses(ug);
        MetaClass[] middleWareClasses = null;

        if (tmpClasses != null) {
            middleWareClasses = new MetaClass[tmpClasses.size()];

            for (int i = 0; i < tmpClasses.size(); i++) {
                middleWareClasses[i] = new MetaClass((Sirius.server.localserver._class.Class) tmpClasses.get(i), properties.getServerName());
            }
        }

        return middleWareClasses;

    }

    //---------------------------------------------------------------------------
    public ClassCache getClassCache() {
        return classes;
    }

    //----------------------------------------------------------------------------
//    public NodeReferenceList getChildren(int nodeID,UserGroup ug) throws Throwable
//    {
//        return  tree.getChildren(nodeID,ug);
//    }
    public NodeReferenceList getChildren(Node node, UserGroup ug) throws Throwable {
        return tree.getChildren(node, ug);
    }

    //-----------------------------------------------------------------------------
//    public Sirius.server.middleware.types.Node[] getParents(int nodeID,UserGroup ug) throws Throwable
//    {
//        
//        return new Sirius.server.middleware.types.NodeReferenceList(tree.getParents(nodeID,ug),this,ug).getNodes();
//    }
//    
    //-------------------------------------------------------------------------
    public NodeReferenceList getTops(UserGroup ug) throws Throwable {

        return new NodeReferenceList(tree.getTopNodes(ug));
    }

    //-------------------------------------------------------------------------
    public NodeReferenceList getClassTreeNodes(UserGroup ug) throws Throwable {

        return new NodeReferenceList(tree.getClassTreeNodes(ug));
    }

    //----------------------------------------------------------------------------
    public MetaObject getObject(String objectID, UserGroup ug) throws Throwable {
        int oId;
        int cId;

        String[] ids = objectID.split("@");
        oId = new Integer(ids[0]).intValue();
        cId = new Integer(ids[1]).intValue();



        //An dieser Stelle wird die Referenz neu gesetzt. Deshalb funzt getParent() der ObjectAttributes nicht richtig
        //zus�tzlich erzeugt auch die filter Methode eine neue Adresse
        Sirius.server.localserver.object.Object o = objects.getObject(oId, cId, ug);

        if (o != null) {

            MetaObject mo = new DefaultMetaObject(o.filter(ug), properties.getServerName());
          //  mo.setMetaClass(new MetaClass(classes.getClass(cId), properties.getServerName()));
            mo.setAllClasses(classes.getClassHashMap());
            return mo;
        } else {
            return null;
        }

    }

    public MetaObject[] getObjects(String[] objectIDs, UserGroup ug) throws Throwable {
        MetaObject[] obs = new MetaObject[objectIDs.length];

        for (int i = 0; i < objectIDs.length; i++) {
            obs[i] = getObject(objectIDs[i], ug);
        }

        return obs;
    }

//    //bugfix
//    public Node getNode(Node node, UserGroup ug) throws Throwable
//    {
//        if(node instanceof Sirius.server.localserver.tree.node.ObjectNode)
//        {
//            
//            Sirius.server.localserver.tree.node.ObjectNode newNode = (Sirius.server.localserver.tree.node.ObjectNode)node;
//            return new MetaObjectNode( newNode,properties.getServerName());
//        }
//        else if (node instanceof Sirius.server.localserver.tree.node.ClassNode)
//            return new MetaClassNode((Sirius.server.localserver.tree.node.ClassNode)node,properties.getServerName());
//        return new MetaNode(node,properties.getServerName());
//        
//    }
//    
    //-----------------------------------------------------------------
    public Sirius.server.middleware.types.Node[] getNodes(int[] ids, UserGroup ug) throws Throwable {

        Sirius.server.middleware.types.Node[] n = new Sirius.server.middleware.types.Node[ids.length];

        for (int i = 0; i < ids.length; i++) {
            n[i] = tree.getNode(ids[i], ug);
        }

        return n;

    }

//    public  Sirius.server.localserver.tree.NodeReferenceList getObjectNodes(String[] objectIDs,UserGroup ug) throws Throwable
//    {
//        //estimated 2 Nodes per object in average !
//       java.util.ArrayList<Node>  v = new java.util.ArrayList<Node>(objectIDs.length * 2);
//        
//        for(int i =0;i< objectIDs.length;i++)
//        {
//            v.addAll(tree.getObjectNodes(objectIDs[i],ug));
//   
//        }
//        
//        return new Sirius.server.localserver.tree.NodeReferenceList(v);
//        
//        
//    }
    //----------------------------------------------------------------
    public final AbstractTree getTree() throws Throwable {
        return tree;
    }

    //-------------------------------------------------------
    public final DBConnection getActiveDBConnection() {
        return connectionPool.getConnection();
    }

    public final DBConnectionPool getConnectionPool() {
        return connectionPool;
    }

    //public final Connection getTranslConnection(){return  connectionPool.getConnection("transl").getConnection();}
    public final ServerProperties getSystemProperties() {
        return properties;
    }

    public final MethodMap getMethods() {
        return methods.getMethods();
    }

    public final MethodMap getMethods(UserGroup ug) throws Throwable {
        return methods.getMethods(ug);
    }

    public final UserStore getUserStore() {
        return userstore;
    }

    public final ObjectFactory getObjectFactory() {
        return objects;
    }

    public final PersistenceManager getObjectPersitenceManager() {
        return objectPersistence;
    }

    public ServerProperties getProperties() {
        return properties;
    }
}// end class DBServer
