package Sirius.server.localserver.tree;
//import Sirius.server.localserver.tree.node.*;
import Sirius.server.middleware.types.Node;
import Sirius.server.localserver.*;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClassNode;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObjectNode;
import java.sql.*;
import Sirius.server.sql.*;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.Permission;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.newuser.permission.Policy;
import Sirius.server.newuser.permission.PolicyHolder;
import Sirius.server.property.*;
import de.cismet.tools.StringTools;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

/**
 * Klasse um auf den in der DB gespeicherten Graphen zuzugreifen
 * @author schlob
 */
public class VirtualTree implements AbstractTree {

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ServerProperties properties;
    private DBConnectionPool conPool;
    DBServer dbServer;
    private HashSet<Integer> nonLeafs;
    private UserGroupIdentifiers idMap;
    private PolicyHolder policyHolder = null;
    private ClassCache classCache = null;

    /**
     *
     * @param conPool
     * @param properties
     * @param dbServer
     * @throws java.lang.Throwable
     */
    public VirtualTree(DBConnectionPool conPool, ServerProperties properties, DBServer dbServer, PolicyHolder policyHolder, ClassCache classCache) throws Throwable {

        //super(conPool,properties);
        this.conPool = conPool;

        DBConnection con = conPool.getConnection();

        this.properties = properties;

        this.dbServer = dbServer;

        this.nonLeafs = initNonLeafs();

        this.idMap = new UserGroupIdentifiers(conPool);

        this.policyHolder = policyHolder;

        this.classCache = classCache;

    }
    //--------------------------------------------------------------------------
    /**
     *
     * @param nodeID
     * @param ug
     * @throws java.lang.Exception
     * @return
     */
    public NodeReferenceList getChildren(int nodeId, UserGroup ug, Policy parentPolicy) throws Throwable {

        //beschaffe lokale ug_id
        int ug_id = idMap.getLocalUgId(ug);




        // String localChildren ="select n.id,name,class_id,object_id,node_type,dynamic_children,prot_prefix||server||path||object_name as url from (select  id,name,class_id,object_id,node_type,dynamic_children, descr  from cs_cat_node as x where x.id in ( select id_to  from cs_cat_link  where id_from ="+ nodeId+")) as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)";

//        String localChildren = //"select x.* ,p.id as perm_id from (select n.id,name,class_id,object_id,node_type,dynamic_children,prot_prefix||server||path||object_name as url from (select  id,name,class_id,object_id,node_type,dynamic_children, descr  from cs_cat_node as x where x.id in ( select id_to  from cs_cat_link  where id_from ="+ nodeId+")) as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)) as x left outer join cs_ug_cat_node_perm as p on p.cat_node_id=x.id and p.permission<>0 and ug_id = "+ug.getId();
//                "SELECT distinct x.* ,p.id*0 as perm_id from (select n.id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url from (select  id,name,class_id,object_id,node_type,dynamic_children,sql_sort, descr  from cs_cat_node as x where x.id in ( select id_to  from cs_cat_link  where id_from =" + nodeId + ")) as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)) as x left outer join cs_ug_cat_node_perm as p on p.cat_node_id=x.id and ug_id =" + ug_id +
//                "EXCEPT select distinct x.* ,p.id*0 as perm_id from (select n.id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url from (select  id,name,class_id,object_id,node_type,dynamic_children,sql_sort, descr  from cs_cat_node as x where x.id in ( select id_to  from cs_cat_link  where id_from =" + nodeId + ")) as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)) as x , cs_ug_cat_node_perm as p where  p.cat_node_id=x.id and p.permission=0  and ug_id = " + ug_id;
        //
        String localChildren = "select " +
                "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from  " +
                "( " +
                "select  " +
                "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class   " +
                "from  " +
                "cs_cat_node as n left outer join url  on ( n.descr=url.id )  " +
                "left outer join url_base as ub  on (url.url_base_id=ub.id) " +
                ") as y  " +
                "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id  " +
                "left outer join cs_permission as pp on p.permission=pp.id and ug_id=" + ug_id +
                " where  " +
                "y.id in ( select id_to  from cs_cat_link  where id_from =" + nodeId + ") ";


        log.debug("getChildren f\u00FCr " + nodeId + "  gerufen\nlocalChildren aus:" + localChildren);


        // select remote child links
        String remoteChildren = "select " +
                "id_to as id,domain_to as domain " +
                "from " +
                "cs_cat_link " +
                "where " +
                "domain_to not in " +
                "( select id from cs_domain " +
                "where lower(name)='local'  " +
                "or lower(name)= lower('" + properties.getServerName().trim() + "')) " +
                "and  id_from = " + nodeId;


        Statement s = conPool.getConnection().getConnection().createStatement();

        ResultSet r = s.executeQuery(localChildren);


        // add local children to result (nodes)



        NodeReferenceList result = new NodeReferenceList(removeUnReadableNodes(nodesFromResult(r, ug, parentPolicy), ug));

        r = s.executeQuery(remoteChildren);

        // add remote children (links)
        result.setRemotes(linksFromResult(r));

        return result;

    }

    public NodeReferenceList getChildren(Node parentNode, UserGroup ug) throws Throwable {
        log.debug("NodeReferenceList getChildren(Node node, UserGroup ug) ");
        String statement = parentNode.getDynamicChildrenStatement();

        if (statement == null) {
            //return this.getChildren(parentNode.getId(), ug, parentNode.getPermissions().getPolicy());
            return this.getChildren(parentNode.getId(), ug, null);
        }
        log.debug("getChildren aufgerufen (dynamische kinder)\nstatement:" + statement);
        Statement s = conPool.getConnection().getConnection().createStatement();

        ResultSet r = s.executeQuery(statement);

        ArrayList<Node> result = nodesFromResult(r, ug);

        for (Node n : result) {
            if (!n.isDerivePermissionsFromClass()) {
                log.debug(n + ": Weise die Rechte des Parentnodes zu.");
                n.setPermissions(parentNode.getPermissions());
            }
            n.setDynamic(true);

            // if dynamic and has no children
            n.setLeaf(n.getDynamicChildrenStatement() == null);

        }

        return new NodeReferenceList(removeUnReadableNodes(result, ug));

    }

    /**
     *
     * @param node
     * @param parent
     * @param user
     * @throws java.lang.Exception
     * @return
     */
    public Sirius.server.middleware.types.Node addNode(Sirius.server.middleware.types.Node node, Sirius.server.middleware.types.Link parent, User user) throws Throwable {
        log.debug("addNode gerufen :: " + node);

        DBConnection con = conPool.getConnection();

        Integer classId = null;
        Integer objectId = null;
        char nodeType = 'N';


        try {

            if (node instanceof Sirius.server.middleware.types.MetaObjectNode) {
                Sirius.server.middleware.types.MetaObjectNode oNode = (Sirius.server.middleware.types.MetaObjectNode) node;
                objectId = new Integer(oNode.getObjectId());
                classId = new Integer(oNode.getClassId());
                nodeType = 'O';
            } else if (node instanceof Sirius.server.middleware.types.MetaClassNode) {
                Sirius.server.middleware.types.MetaClassNode cNode = (Sirius.server.middleware.types.MetaClassNode) node;
                classId = new Integer(cNode.getClassId());
                nodeType = 'C';
            }

            boolean isRoot = false;
            char root = 'F';

            String policy = "null";

            if (parent == null && parent.getNodeId() < 0) {
                isRoot = true;
                root = 'T';
            } else {
                Node parentNode = getNode(parent.getNodeId(), user.getUserGroup());
                policy = parentNode.getPermissions().getPolicy().getDbID() + "";

            }
            int nodeId = getNextNodeID();



            String addNode = "insert into cs_cat_node (id, name, descr, class_id, object_id, node_type, is_root, org,dynamic_children,sql_sort,policy) values ( " + nodeId + ",'" + node.getName() + "',1," + classId + "," + objectId + ",'" + nodeType + "','" + root + "', NULL,NULL,false," + policy + " )";//, (select id from cs_domain where name ='"+properties.getServerName().trim()+"')

            Statement add = con.getConnection().createStatement();

            int count = add.executeUpdate(addNode);

            // \u00FCbertrage rechte des Vaterknotens
            inheritNodePermission(nodeId, parent.getNodeId());

            if (!isRoot) {
                addLink(getNode(parent.getNodeId(), user.getUserGroup()), getNode(nodeId, user.getUserGroup()), user);
            }

            Sirius.server.middleware.types.Node n = getNode(nodeId, user.getUserGroup());

            return n;

        } catch (Exception e) {
            con.getConnection().rollback();
            throw e;
        }
    }

    /**
     *
     * @param node
     * @param user
     * @throws java.lang.Throwable
     * @return
     */
    public boolean deleteNode(Sirius.server.middleware.types.Node node, User user) throws Throwable {



        if (!nodeIsLeaf(node.getId())) {
            log.error("Node is no leaf, cannot delete!!!");
            return false;
        }

        String deleteNode = "DELETE FROM cs_cat_node where id = " + node.getId();

        log.debug("delete Node " + deleteNode);


        String delLink = "DELETE FROM cs_cat_link where id_to = " + node.getId();

        log.debug("delte Link in delete node " + delLink);

        DBConnection con = conPool.getConnection();

        Statement del = con.getConnection().createStatement();

        String delPerm = "DELETE FROM cs_ug_cat_node_perm where cat_node_id = " + node.getId();

        log.debug(delPerm);

        del.executeUpdate(deleteNode);
        del.executeUpdate(delLink);
        del.executeUpdate(delPerm);



        return true; // wenn keine Exception

    }

    /**
     *
     * @param father
     * @param child
     * @throws java.lang.Exception
     * @return
     */
    public boolean addLink(int father, int child) throws Exception {

        DBConnection con = conPool.getConnection();

        String addLink = "INSERT INTO cs_cat_link (id_from,id_to,org,domain_to) values(" + father + "," + child + ",null, (select id from cs_domain where name ='" + properties.getServerName().trim() + "'))";

        Statement s = con.getConnection().createStatement();

        log.debug("addLink " + addLink);

        // update the noLeaf Hash as the Father is no Leaf anymore
        nonLeafs.add(new Integer(father));

        return s.executeUpdate(addLink) > 0;


    }

    /**
     *
     * @param from
     * @param to
     * @param user
     * @throws java.lang.Exception
     * @return
     */
    public boolean deleteLink(Sirius.server.middleware.types.Node from, Sirius.server.middleware.types.Node to, User user) throws Exception {

        // user check notwendig ??

        log.debug("delete link from :" + from.toString() + " to :" + to.toString());


        Statement del = conPool.getConnection().getConnection().createStatement();

        String deleteLink = "DELETE FROM cs_cat_link WHERE id_from = " + from.getId() + " AND id_to =  " + to.getId() + "AND domain_to = " +
                "( SELECT id from cs_domain where lower(name) = lower(" + to.getDomain() + ")  )";


        log.debug("deleteLink " + del);



        int affected = del.executeUpdate(deleteLink);

        try {

            if (nodeIsLeaf(from.getId())) {
                nonLeafs.remove(new Integer(from.getId()));
            }
        } catch (Throwable ex) {
            log.error("could not update nonLeafCache", ex);
        }

        return affected > 0;

    }

    /**
     *
     * @param root
     * @param user
     * @throws java.lang.Exception
     * @return
     *///    //-->>> check
//    public boolean copySubTree(Sirius.server.middleware.types.Node root, User user) throws Throwable
//    {
//        boolean retVal=false;
//        NodeReferenceList children = dbServer.getChildren(root,user.getUserGroup());
//
//        // Links auf andere ls werden vorerst misachtet
//
//        Node[] childNodes = children.getLocalNodes();
//
//        for(int i =0;i<childNodes.length;i++)
//        {
//
//            addNode(childNodes[i],new Sirius.server.middleware.types.Link( root.getId(),root.getDomain() ) ,user);
//
//            //            if(childNodes[i] instanceof Sirius.server.middleware.types.MetaObjectNode)
//            //                retVal &= dbServer.getObjectPersitenceManager().insertMetaObject(user,((Sirius.server.middleware.types.MetaObjectNode)childNodes[i]).getObject()) > 0 ;// add Object
//
//            // is leaf
//            if( getNode( childNodes[i].getId(),user.getUserGroup() ).isLeaf())
//                retVal &=  copySubTree(childNodes[i],user);
//
//        }
//
//
//
//        return retVal;
//    }
    /**
     *
     * @throws java.sql.SQLException
     * @return
     */
    public int getNextNodeID() throws SQLException {

        String query = "SELECT NEXTVAL('cs_cat_node_sequence')";

        ResultSet rs = conPool.getConnection().getConnection().createStatement().executeQuery(query);

        if (rs.next()) {
            return (rs.getInt(1));
        } else {
            return 1;
        }
    }
    //------------------------------------------------------------------------
    /**
     *
     * @param from
     * @param to
     * @param user
     * @throws java.lang.Exception
     * @return
     */
    public boolean addLink(Sirius.server.middleware.types.Node from, Sirius.server.middleware.types.Node to, User user) throws Exception {
        return addLink(from.getId(), to.getId());
    }

    /**
     *
     * @param ug
     * @throws java.lang.Exception
     * @return
     */
    public Node[] getClassTreeNodes(UserGroup ug) throws Throwable {

        //beschaffe lokale ug_id
        int ug_id = idMap.getLocalUgId(ug);



        try {
            //select rootnodes that are not classnodes and

            //Statement all nodes with or without permisisons minus nodes with read permissions
            //String statement = "select  distinct  y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.id*0 as perm_id  from (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)   where is_root=true and node_type='C' ) as y left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id = " + ug_id +
            //      "EXCEPT select  distinct y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.id*0  as perm_id  from (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)   where is_root=true and node_type='C' ) as y ,cs_ug_cat_node_perm as p where  p.cat_node_id=y.id and p.permission=0 and ug_id = " + ug_id;
            String statement = "select  distinct " +
                    "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " +
                    "(" +
                    "select " +
                    "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  " +
                    "from " +
                    "cs_cat_node as n left outer join url  on ( n.descr=url.id ) " +
                    "left outer join url_base as ub  on (url.url_base_id=ub.id)   " +
                    "where " +
                    "is_root=true and node_type='C' " +
                    ") as y " +
                    "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id=" + ug_id +
                    " left outer join cs_permission as pp on p.permission=pp.id ";

            Statement s = conPool.getConnection().getConnection().createStatement();

            ResultSet r = s.executeQuery(statement);

            java.util.ArrayList<Node> nodes = nodesFromResult(r, ug);


            for (Node n : nodes) {
                n.setLeaf(nodeIsLeaf(n.getId()));
            }

            //TODO Remove classnodes if the class is not readable


            return removeUnReadableNodes(nodes, ug);





        } catch (Throwable t) {
            log.error("Error in getCLassTreeNodes", t);
            throw t;
        }
    }

    /**
     *
     * @param nodeId
     * @param parentNodeId
     * @throws java.lang.Exception
     */
    @Deprecated
    public void inheritNodePermission(int nodeId, int parentNodeId) throws Throwable {

        // precondition id is set with autokey (sequence)
        String statement = "insert into cs_ug_cat_node_perm  (ug_id,cat_node_id,permission)  (select ug_id," + nodeId + ",permission  from cs_ug_cat_node_perm where cat_node_id=" + parentNodeId + ")";

        try {
            Connection c = conPool.getConnection().getConnection();

            Statement s = c.createStatement();

            s.executeUpdate(statement);
        } catch (Throwable t) {
            log.error("Error in inheritNodePermissions", t);
            throw t;
        }

    }

    /**
     *
     * @param objectId
     * @throws java.lang.Throwable
     * @return
     */
    public boolean hasNodes(String objectId) throws Throwable {


        // oId@cId
        String[] ids = objectId.split("@");

        try {
            String statement = "select count(id) from cs_cat_node where object_id = " + ids[0] + " and class_id = " + ids[1];

            Connection c = conPool.getConnection().getConnection();

            Statement s = c.createStatement();

            ResultSet r = s.executeQuery(statement);

            if (r.next()) {
                return (r.getInt(1) == 0);
            }
        } catch (Throwable t) {
            log.error("Error in hasNodes", t);
            throw t;
        }

        // never reached
        return true;
    }

    /**
     *
     * @param ug
     * @throws java.lang.Throwable
     * @return
     */
    public Node[] getTopNodes(UserGroup ug) throws Throwable {
        log.info("get top nodes for UserGroup:" + ug.getName() + "@" + ug.getDomain());
        // usercheck !! -> statement erweitern
        //beschaffe lokale ug_id
        int ug_id = idMap.getLocalUgId(ug);
        try {
            //select rootnodes that are not classnodes
//            String statement = "select  distinct  y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.id*0 as perm_id  from (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)   where is_root=true and node_type<>'C' ) as y left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id = " + ug_id +
//                    "EXCEPT select  distinct y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.id*0  as perm_id  from (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,prot_prefix||server||path||object_name as url  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) left outer join url_base as ub  on (url.url_base_id=ub.id)   where is_root=true and node_type<>'C' ) as y ,cs_ug_cat_node_perm as p where  p.cat_node_id=y.id and p.permission=0 and ug_id = " + ug_id;

            String statement = "select  distinct " +
                    "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " +
                    "(" +
                    "select " +
                    "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  " +
                    "from " +
                    "cs_cat_node as n left outer join url  on ( n.descr=url.id ) " +
                    "left outer join url_base as ub  on (url.url_base_id=ub.id)   " +
                    "where " +
                    "is_root=true and node_type<>'C' " +
                    ") as y " +
                    "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id=" + ug_id +
                    " left outer join cs_permission as pp on p.permission=pp.id ";



            Statement s = conPool.getConnection().getConnection().createStatement();

            ResultSet r = s.executeQuery(statement);

            return removeUnReadableNodes(nodesFromResult(r, ug), ug);


        // Die Knoten die nicht angezeigt werden dürfen müssen noch rausgefiltert werden



        } catch (Throwable t) {
            log.error("Error in getTopNodes", t);
            throw t;
        }
    }

    private Node[] removeUnReadableNodes(java.util.ArrayList<Node> n, UserGroup ug) {
        Vector<Node> v = new Vector<Node>();
        log.info("removeUnReadableNodes " + n.size() + " Elements before");
        for (Node node : n) {
            if (node.getPermissions().hasReadPermission(ug)) {

                v.add(node);
            }
        }
        log.info("removeUnReadableNodes " + v.size() + " Elements after");
        return v.toArray(new Node[v.size()]);
    }

    /**
     *
     * @param objectID
     * @param ug
     * @throws java.lang.Exception
     * @return
     */
//    public java.util.ArrayList<Node> getObjectNodes(String objectId, UserGroup ug) throws Throwable
//    {
//        //->> statement erweitern
//        // oId@cId
//        String[] ids = objectId.split("@");
//
//        try
//        {
//            //select objectnodes
//            String statement = getAllNodes+"where object_id="+ids[0] +" AND class_id="+ids[1];
//
//            Statement s = conPool.getConnection().getConnection().createStatement();
//
//            ResultSet r = s.executeQuery(statement);
//
//            return new java.util.ArrayList<Node>(nodesFromResult(r,ug));
//
//
//        }
//        catch(Throwable t)
//        {
//            logger.error("Error in getObjectNodes",t);
//            throw t;
//        }
//    }
    /**
     *
     * @param nodeId
     * @param ug
     * @throws java.lang.Exception
     * @return
     */
    public Node getNode(int nodeId, UserGroup ug) throws Throwable {
        //beschaffe lokale ug_id
        int ug_id = idMap.getLocalUgId(ug);


        try {
            String statement = "select  y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  " +
                    "from" +
                    " (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) " +
                    "left outer join url_base as ub  on (url.url_base_id=ub.id)   " +
                    "where n.id=" + nodeId + " ) as y " +
                    "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id = " + ug_id + " " +
                    "left outer join cs_permission as pp on p.permission=pp.id";

            Statement s = conPool.getConnection().getConnection().createStatement();

            ResultSet r = s.executeQuery(statement);

            return nodesFromResult(r, ug).get(0);

        } catch (Throwable t) {
            log.error("Error in getNode", t);
            throw t;
        }
    }

    /**
     *
     * @param nodeId
     * @throws java.lang.Throwable
     * @return
     */
    public boolean nodeIsLeaf(int nodeId) throws Throwable {
        try {
            String statement = "select count(id_from) from cs_cat_link where id_from = " + nodeId;

            Connection c = conPool.getConnection().getConnection();

            Statement s = c.createStatement();

            ResultSet r = s.executeQuery(statement);

            if (r.next()) {
                return (r.getInt(1) == 0);
            }
        } catch (Throwable t) {
            log.error("Error in nodeIsLeaf", t);
            throw t;
        }

        // never reached
        return true;

    }

    private java.util.ArrayList<Node> nodesFromResult(ResultSet nodeTable, UserGroup ug) throws Throwable {
        return nodesFromResult(nodeTable, ug, null);
    }

    private java.util.ArrayList<Node> nodesFromResult(ResultSet nodeTable, UserGroup ug, Policy parentPolicy) throws Throwable {

        java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>();
        java.util.HashMap<String, Node> nodeHM = new java.util.HashMap<String, Node>();
        char c = 'N';



        while (nodeTable.next())//add all nodes to the hashtable
        {
            //current Node
            Node tmp = null;
            int id = 0;
            String name = "";
            String descr = null;
            String dynamicChildren = null;
            int classId = -1;
            try {
                if (nodeTable.getObject("class_id") != null) {
                    classId = nodeTable.getInt("class_id");
                }
            } catch (Exception skip) {
            }
            boolean derivePermissionsFromClass = false;
            try {
                derivePermissionsFromClass = nodeTable.getBoolean("derive_permissions_from_class");
            } catch (Exception skip) {
            }
            int iconFactory = -1;
            try {

                if (nodeTable.getObject("iconfactory") != null) {
                    iconFactory = nodeTable.getInt("iconfactory");
                }
            } catch (Exception skip) {
            }

            String icon=null;
            try {

                if (nodeTable.getObject("icon") != null) {
                    icon = nodeTable.getString("icon");
                }
            } catch (Exception skip) {
            }



            c = nodeTable.getString("node_type").charAt(0);// alias for the leftmost character of the

            try {
                name = nodeTable.getString("name").trim();
            } catch (Exception skip) {
                name = null;
            }

            descr = nodeTable.getString("url");
            descr = StringTools.deleteWhitespaces(descr);

            id = nodeTable.getInt("id");
            dynamicChildren = nodeTable.getString("dynamic_children");

            if (dynamicChildren != null) {
                dynamicChildren = dynamicChildren.trim();
            }
            String domain = properties.getServerName();

            //tmp.setLeaf(this.nodeIsLeaf(tmp.getID()));
            //xyc

            Boolean sqlSort = nodeTable.getBoolean("sql_sort");
            Policy policy = null;



            try {
                Object policytester = nodeTable.getObject("policy");

                if (policytester != null) {
                    int p = nodeTable.getInt("policy");
                    policy = policyHolder.getServerPolicy(p);
                }
            } catch (Exception e) {
                log.debug("Policy war nicht im resultset. Ist aber bei DynamicChildrenStatements normal", e);
            }


            if (policy == null) {
                if (c == (byte) 'N' || c == (byte) 'n') {
                    //Purenode
                    // bekommt die Policy des Parent-Knotens
                    if (policy == null) {
                        policy = parentPolicy;
                    }

                    // pureNodePolicy in runtime.properties
                    if (policy == null) {
                        policy = policyHolder.getServerPolicy(properties.getPureNodePolicy());
                    }

                } else if (c == (byte) 'O' || c == (byte) 'o') {
                    //Objectnode

                    // bekommt die Policy der Klasse
                    if (policy == null) {
                        try {
                            policy = classCache.getClass(classId).getPolicy();
                        } catch (Exception e) {
                            log.warn("Fehler bei: classCache.getClass(nodeTable.getInt(\"class_id\")).getPolicy() ", e);
                        }
                    }

                } else if (c == (byte) 'C' || c == (byte) 'c') {
                    //Classnode
                    // classNodePolicy in runtime.properties
                    if (policy == null) {
                        policy = policyHolder.getServerPolicy(properties.getClassNodePolicy());
                    }

                } else {
                    throw new Exception("Kein bekannter Knotentyp : " + (char) c + " ?");
                }

                // serverPolicy in runtime.properties
                if (policy == null) {
                    policy = policyHolder.getServerPolicy(properties.getServerPolicy());
                }

                //PARANOID Policy als Fallback
                if (policy == null) {
                    log.warn("ACHTUNG: Das sollte eigentlich nicht noetig sein. Setze fallbackPolicy: PARANOID");
                    policy = Policy.createParanoidPolicy();
                }


            }

            boolean leaf = !nonLeafs.contains(new Integer(id));

            Sirius.server.localserver._class.Class metaclass = null;
            // new Node according to node type
            if (c == (byte) 'N' || c == (byte) 'n') {
                tmp = new MetaNode(id, domain, name, descr, leaf, policy, iconFactory,icon, derivePermissionsFromClass, classId);
            } else {  
                try {
                    metaclass = classCache.getClass(classId);
                } catch (Exception e) {
                    log.warn("getClass ist schiefgegangen. kann keinen objekt/klassenknoten erzeugen", e);
                }
                if (metaclass != null && metaclass.getPermissions().hasReadPermission(ug)) {
                    if (c == (byte) 'O' || c == (byte) 'o') {

                        tmp = new MetaObjectNode(id, name, descr, domain, nodeTable.getInt("object_id"), classId, leaf, policy, iconFactory,icon, derivePermissionsFromClass);

                    } else if (c == (byte) 'C' || c == (byte) 'c') {
                        tmp = new MetaClassNode(id, domain, classId, name, descr, leaf, policy, iconFactory,icon, derivePermissionsFromClass, classId);
                    } else {
                        throw new Exception("Kein bekannter Knotentyp : " + (char) c + " ?");
                    }
                }
            }
            if (tmp != null) {
                tmp.setDynamicChildrenStatement(dynamicChildren);

                if (sqlSort != null) {
                    tmp.setSqlSort(sqlSort);
                }
                String nodeKey = "Node:" + tmp.getId() + "@" + tmp.getDomain();
                //Das hinzufügen zu nodes bzw. nodesHM
                //auf doppelte Einträge braucht/kann nur gecheckt werden , wenn die Nodes nicht dynamisch sind. 
                //      Deshalb der Check auf isDynamic) bzw auf nodeId==-1 (Das soll wieder raus wenn isDynamic() den richtigen Wert liefert)
                if (!nodeHM.containsKey(nodeKey) || tmp.isDynamic() || tmp.getId() == -1) {
                    nodeHM.put(nodeKey, tmp);
                    nodes.add(tmp);
                }


                if (tmp.isDerivePermissionsFromClass()) {
                    log.info(tmp + "(" + c + "): Permissions derived from class.");
                    Sirius.server.localserver._class.Class cc = classCache.getClass(tmp.getClassId());
                    if (cc != null) {
                        tmp.setPermissions(cc.getPermissions());
                    }
                } else {
                    try {
                        Object permId = nodeTable.getObject("perm_id");
                        String permKey = nodeTable.getString("perm_key");

                        if (permId != null && permKey != null) {
                            Permission pp = new Permission(nodeTable.getInt("perm_id"), permKey);
                            nodeHM.get(nodeKey).getPermissions().addPermission(ug, pp);
                            log.debug("Permission " + pp.getKey() + " added to node" + tmp.getId() + " for ug " + ug.getKey().toString());
                        }
                    } catch (Throwable t) {
                        log.info("could not set permissions for node::" + id, t);
                    }
                }
            }

        }// end while


        return nodes;
    }

    private java.util.ArrayList<Link> linksFromResult(ResultSet linkTable) throws Throwable {

        java.util.ArrayList<Link> result = new java.util.ArrayList<Link>();

        String domain = properties.getServerName();


        while (linkTable.next()) {
            int id = linkTable.getInt("id");
            String toServer = linkTable.getString("domain");

            if (toServer == null) {
                toServer = domain;
            }
            result.add(new Link(id, toServer));


        }



        return result;
    }

    private HashSet<Integer> initNonLeafs() {
        // find nodes that are not leafs (a link exists)
        //String statement = "select distinct id_to  from cs_cat_link as a where id_to  in (select distinct id_from from cs_cat_link) union select distinct id from cs_cat_node where is_root=true ";
        String statement = "select distinct id_from from cs_cat_link union select distinct id as id_from from cs_cat_node where dynamic_children is not null";

        // the non leafs are of the order 10K in wundalive
        HashSet<Integer> nl = new HashSet<Integer>(12000);


        try {
            Statement s = conPool.getConnection().getConnection().createStatement();

            ResultSet r = s.executeQuery(statement);

            while (r.next()) {
                nl.add(r.getInt("id_from"));
            }
            log.debug("added # of leafentries::" + nl.size());

        } catch (SQLException ex) {
            log.error("Fehler beim Laden der Leaf Eigenschaft", ex);
        }



        return nl;


    }

    public static void main(String[] args) {        // VirtualTree t = new VirtualTree();
    }
}

class UserGroupIdentifiers {

    private java.util.HashMap<String, Integer> idsByUgIdentifier = new java.util.HashMap<String, Integer>();
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    UserGroupIdentifiers(DBConnectionPool conPool) {
        String statement = "select u.id, u.name||'@'||d.name as ug_identifier  from cs_ug as u , cs_domain as d where u.domain=d.id and not (lower(d.name) = 'local')";
        try {

            Statement s = conPool.getConnection().getConnection().createStatement();

            ResultSet r = s.executeQuery(statement);

            while (r.next()) {
                idsByUgIdentifier.put(r.getString("ug_identifier"), r.getInt("id"));
            }
        } catch (SQLException ex) {
            logger.error("Fehler beim Laden der UG Fremssystemfeferenzen", ex);
        }


    }

    int getLocalUgId(UserGroup ug) {

        //beschaffe lokale ug_id
        int ug_id = ug.getId();

        Integer localId = idsByUgIdentifier.get(ug.getKey().toString());

        // falls mapping vorhanden ersetzten
        if (localId != null) {
            ug_id = localId;
        }
        return ug_id;
    }
}