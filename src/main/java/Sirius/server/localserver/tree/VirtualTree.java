/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.tree;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.Shutdownable;
import Sirius.server.localserver.*;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaClassNode;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.Permission;
import Sirius.server.newuser.permission.Policy;
import Sirius.server.newuser.permission.PolicyHolder;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import de.cismet.tools.StringTools;

/**
 * Klasse um auf den in der DB gespeicherten Graphen zuzugreifen.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class VirtualTree extends Shutdown implements AbstractTree {

    //~ Static fields/initializers ---------------------------------------------


    private static final transient Logger LOG = Logger.getLogger(VirtualTree.class);

    //~ Instance fields --------------------------------------------------------

    private ServerProperties properties;
    private DBConnectionPool conPool;
    private HashSet<Integer> nonLeafs;
    private UserGroupIdentifiers idMap;
    private PolicyHolder policyHolder = null;
    private ClassCache classCache = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new VirtualTree object.
     *
     * @param   conPool       DOCUMENT ME!
     * @param   properties    DOCUMENT ME!
     * @param   policyHolder  DOCUMENT ME!
     * @param   classCache    DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Throwable
     */
    public VirtualTree(final DBConnectionPool conPool,
            final ServerProperties properties,
            final PolicyHolder policyHolder,
            final ClassCache classCache) throws Throwable {
        // super(conPool,properties);
        this.conPool = conPool;

        final DBConnection con = conPool.getConnection();

        this.properties = properties;

        this.nonLeafs = initNonLeafs();

        this.idMap = new UserGroupIdentifiers(conPool);

        this.policyHolder = policyHolder;

        this.classCache = classCache;

        addShutdown(new Shutdownable() {

                @Override
                public void shutdown() throws ServerExitError {
                    idMap.idsByUgIdentifier.clear();
                    nonLeafs.clear();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId        DOCUMENT ME!
     * @param   ug            DOCUMENT ME!
     * @param   parentPolicy  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Exception
     */
    @Override
    public NodeReferenceList getChildren(final int nodeId, final UserGroup ug, final Policy parentPolicy)
            throws Throwable {
        final int ug_id = idMap.getLocalUgId(ug);

        final String localChildren = "select "                                                                                                                                                                           // NOI18N
                    + "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from  " // NOI18N
                    + "( "                                                                                                                                                                                               // NOI18N
                    + "select  "                                                                                                                                                                                         // NOI18N
                    + "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class   "                  // NOI18N
                    + "from  "                                                                                                                                                                                           // NOI18N
                    + "cs_cat_node as n left outer join url  on ( n.descr=url.id )  "                                                                                                                                    // NOI18N
                    + "left outer join url_base as ub  on (url.url_base_id=ub.id) "                                                                                                                                      // NOI18N
                    + ") as y  "                                                                                                                                                                                         // NOI18N
                    + "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id  "                                                                                                                                 // NOI18N
                    + "left outer join cs_permission as pp on p.permission=pp.id and ug_id=" + ug_id                                                                                                                     // NOI18N
                    + " where  "                                                                                                                                                                                         // NOI18N
                    + "y.id in ( select id_to  from cs_cat_link  where id_from =" + nodeId + ") ";                                                                                                                       // NOI18N
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren for " + nodeId + "  called\nlocalChildren from:" + localChildren);                                                                                                                    // NOI18N
        }

        // select remote child links
        final String remoteChildren = "select "                                              // NOI18N
                    + "id_to as id,domain_to as domain "                                     // NOI18N
                    + "from "                                                                // NOI18N
                    + "cs_cat_link "                                                         // NOI18N
                    + "where "                                                               // NOI18N
                    + "domain_to not in "                                                    // NOI18N
                    + "( select id from cs_domain "                                          // NOI18N
                    + "where lower(name)='local'  "                                          // NOI18N
                    + "or lower(name)= lower('" + properties.getServerName().trim() + "')) " // NOI18N
                    + "and  id_from = " + nodeId;                                            // NOI18N

        final Statement s = conPool.getConnection().getConnection().createStatement();

        ResultSet r = s.executeQuery(localChildren);

        // add local children to result (nodes)

        final NodeReferenceList result = new NodeReferenceList(
                removeUnReadableNodes(nodesFromResult(r, ug, parentPolicy), ug));

        r = s.executeQuery(remoteChildren);

        // add remote children (links)
        result.setRemotes(linksFromResult(r));

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parentNode  DOCUMENT ME!
     * @param   ug          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Override
    public NodeReferenceList getChildren(final Node parentNode, final UserGroup ug) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("NodeReferenceList getChildren(Node node, UserGroup ug) "); // NOI18N
        }
        final String statement = parentNode.getDynamicChildrenStatement();

        if (statement == null) {
            // return this.getChildren(parentNode.getId(), ug, parentNode.getPermissions().getPolicy());
            return this.getChildren(parentNode.getId(), ug, null);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren called (dynamic children)\nstatement:" + statement); // NOI18N
        }
        final Statement s = conPool.getConnection().getConnection().createStatement();

        final ResultSet r = s.executeQuery(statement);

        final ArrayList<Node> result = nodesFromResult(r, ug);

        for (final Node n : result) {
            if (!n.isDerivePermissionsFromClass()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(n + ": Assigning rights of the parent node."); // NOI18N
                }
                n.setPermissions(parentNode.getPermissions());
            }
            n.setDynamic(true);

            // if dynamic and has no children
            n.setLeaf(n.getDynamicChildrenStatement() == null);
        }

        return new NodeReferenceList(removeUnReadableNodes(result, ug));
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
     * @throws  Throwable  java.lang.Exception
     */
    @Override
    public Sirius.server.middleware.types.Node addNode(final Sirius.server.middleware.types.Node node,
            final Sirius.server.middleware.types.Link parent,
            final User user) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNode called :: " + node); // NOI18N
        }

        final DBConnection con = conPool.getConnection();

        Integer classId = null;
        Integer objectId = null;
        char nodeType = 'N';

        try {
            if (node instanceof Sirius.server.middleware.types.MetaObjectNode) {
                final Sirius.server.middleware.types.MetaObjectNode oNode =
                    (Sirius.server.middleware.types.MetaObjectNode)node;
                objectId = new Integer(oNode.getObjectId());
                classId = new Integer(oNode.getClassId());
                nodeType = 'O';
            } else if (node instanceof Sirius.server.middleware.types.MetaClassNode) {
                final Sirius.server.middleware.types.MetaClassNode cNode =
                    (Sirius.server.middleware.types.MetaClassNode)node;
                classId = new Integer(cNode.getClassId());
                nodeType = 'C';
            }

            boolean isRoot = false;
            char root = 'F';

            String policy = "null"; // NOI18N

            if ((parent == null) && (parent.getNodeId() < 0)) {
                isRoot = true;
                root = 'T';
            } else {
                final Node parentNode = getNode(parent.getNodeId(), user.getUserGroup());
                policy = parentNode.getPermissions().getPolicy().getDbID() + ""; // NOI18N
            }
            final int nodeId = getNextNodeID();

            final String addNode =
                "insert into cs_cat_node (id, name, descr, class_id, object_id, node_type, is_root, org,dynamic_children,sql_sort,policy) values ( " // NOI18N
                        + nodeId
                        + ",'"                                                                                                                       // NOI18N
                        + node.getName()
                        + "',1,"                                                                                                                     // NOI18N
                        + classId
                        + ","                                                                                                                        // NOI18N
                        + objectId
                        + ",'"                                                                                                                       // NOI18N
                        + nodeType
                        + "','"                                                                                                                      // NOI18N
                        + root
                        + "', NULL,NULL,false,"                                                                                                      // NOI18N
                        + policy
                        + " )";                                                                                                                      // , (select id from cs_domain where name //NOI18N
                                                                                                                                                     // ='"+properties.getServerName().trim()+"')

            final Statement add = con.getConnection().createStatement();

            final int count = add.executeUpdate(addNode);

            // \u00FCbertrage rechte des Vaterknotens
            inheritNodePermission(nodeId, parent.getNodeId());

            if (!isRoot) {
                addLink(getNode(parent.getNodeId(), user.getUserGroup()), getNode(nodeId, user.getUserGroup()), user);
            }

            final Sirius.server.middleware.types.Node n = getNode(nodeId, user.getUserGroup());

            return n;
        } catch (Exception e) {
            con.getConnection().rollback();
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Throwable
     */
    @Override
    public boolean deleteNode(final Sirius.server.middleware.types.Node node, final User user) throws Throwable {
        if (!nodeIsLeaf(node.getId())) {
            LOG.error("Node is no leaf, cannot delete!!!"); // NOI18N
            return false;
        }

        final String deleteNode = "DELETE FROM cs_cat_node where id = " // NOI18N
                    + node.getId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete Node " + deleteNode);                     // NOI18N
        }

        final String delLink = "DELETE FROM cs_cat_link where id_to = " // NOI18N
                    + node.getId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("delte Link in delete node " + delLink);          // NOI18N
        }

        final DBConnection con = conPool.getConnection();

        final Statement del = con.getConnection().createStatement();

        final String delPerm = "DELETE FROM cs_ug_cat_node_perm where cat_node_id = " // NOI18N
                    + node.getId();
        if (LOG.isDebugEnabled()) {
            LOG.debug(delPerm);
        }

        del.executeUpdate(deleteNode);
        del.executeUpdate(delLink);
        del.executeUpdate(delPerm);

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   father  DOCUMENT ME!
     * @param   child   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  java.lang.Exception
     */
    @Override
    public boolean addLink(final int father, final int child) throws Exception {
        final DBConnection con = conPool.getConnection();

        final String addLink = "INSERT INTO cs_cat_link (id_from,id_to,org,domain_to) values(" // NOI18N
                    + father
                    + ","                                                                      // NOI18N
                    + child
                    + ",null, (select id from cs_domain where name ='"                         // NOI18N
                    + properties.getServerName().trim()
                    + "'))";                                                                   // NOI18N

        final Statement s = con.getConnection().createStatement();
        if (LOG.isDebugEnabled()) {
            LOG.debug("addLink " + addLink); // NOI18N
        }

        // update the noLeaf Hash as the Father is no Leaf anymore
        nonLeafs.add(new Integer(father));

        return s.executeUpdate(addLink)
                    > 0;
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
     * @throws  Exception  java.lang.Exception
     */
    // TODO: implementation is not ok, invalid sql
    @Override
    public boolean deleteLink(final Sirius.server.middleware.types.Node from,
            final Sirius.server.middleware.types.Node to,
            final User user) throws Exception {
        if (LOG.isDebugEnabled()) {
            // user check notwendig ??
            LOG.debug("delete link from :" + from.toString() + " to :" + to.toString()); // NOI18N
        }

        final Statement del = conPool.getConnection().getConnection().createStatement();

        final String deleteLink = "DELETE FROM cs_cat_link WHERE id_from = "  // NOI18N
                    + from.getId()
                    + " AND id_to =  "                                        // NOI18N
                    + to.getId()
                    + "AND domain_to = "                                      // NOI18N
                    + "( SELECT id from cs_domain where lower(name) = lower(" // NOI18N
                    + to.getDomain()
                    + ")  )";                                                 // NOI18N
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteLink " + del);                                   // NOI18N
        }

        final int affected = del.executeUpdate(deleteLink);

        try {
            if (nodeIsLeaf(from.getId())) {
                nonLeafs.remove(new Integer(from.getId()));
            }
        } catch (Throwable ex) {
            LOG.error("could not update nonLeafCache", ex); // NOI18N
        }

        return affected
                    > 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public int getNextNodeID() throws SQLException {
        final String query = "SELECT NEXTVAL('cs_cat_node_sequence')"; // NOI18N

        final ResultSet rs = conPool.getConnection().getConnection().createStatement().executeQuery(query);

        if (rs.next()) {
            return (rs.getInt(1));
        } else {
            return 1;
        }
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
     * @throws  Exception  java.lang.Exception
     */
    @Override
    public boolean addLink(final Sirius.server.middleware.types.Node from,
            final Sirius.server.middleware.types.Node to,
            final User user) throws Exception {
        return addLink(from.getId(), to.getId());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Exception
     */
    @Override
    public Node[] getClassTreeNodes(final UserGroup ug) throws Throwable {
        final int ug_id = idMap.getLocalUgId(ug);

        try {
            final String statement = "select  distinct "                                                                                                                                                                     // NOI18N
                        + "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " // NOI18N
                        + "("                                                                                                                                                                                                // NOI18N
                        + "select "                                                                                                                                                                                          // NOI18N
                        + "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  "                   // NOI18N
                        + "from "                                                                                                                                                                                            // NOI18N
                        + "cs_cat_node as n left outer join url  on ( n.descr=url.id ) "                                                                                                                                     // NOI18N
                        + "left outer join url_base as ub  on (url.url_base_id=ub.id)   "                                                                                                                                    // NOI18N
                        + "where "                                                                                                                                                                                           // NOI18N
                        + "is_root=true and node_type='C' "                                                                                                                                                                  // NOI18N
                        + ") as y "                                                                                                                                                                                          // NOI18N
                        + "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id="                                                                                                                        // NOI18N
                        + ug_id
                        + " left outer join cs_permission as pp on p.permission=pp.id ";                                                                                                                                     // NOI18N

            final Statement s = conPool.getConnection().getConnection().createStatement();

            final ResultSet r = s.executeQuery(statement);

            final java.util.ArrayList<Node> nodes = nodesFromResult(r, ug);

            for (final Node n : nodes) {
                n.setLeaf(nodeIsLeaf(n.getId()));
            }

            // TODO Remove classnodes if the class is not readable

            return removeUnReadableNodes(nodes, ug);
        } catch (final Throwable t) {
            LOG.error("Error in getCLassTreeNodes", t); // NOI18N
            throw t;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId        DOCUMENT ME!
     * @param   parentNodeId  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Exception
     */
    @Override
    @Deprecated
    public void inheritNodePermission(final int nodeId, final int parentNodeId) throws Throwable {
        // precondition id is set with autokey (sequence)
        final String statement =
            "insert into cs_ug_cat_node_perm  (ug_id,cat_node_id,permission)  (select ug_id," // NOI18N
                    + nodeId
                    + ",permission  from cs_ug_cat_node_perm where cat_node_id="              // NOI18N
                    + parentNodeId
                    + ")";                                                                    // NOI18N

        try {
            final Connection c = conPool.getConnection().getConnection();

            final Statement s = c.createStatement();

            s.executeUpdate(statement);
        } catch (final Throwable t) {
            LOG.error("Error in inheritNodePermissions", t); // NOI18N
            throw t;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Throwable
     */
    @Override
    public boolean hasNodes(final String objectId) throws Throwable {
        // oId@cId
        final String[] ids = objectId.split("@"); // NOI18N

        try {
            final String statement = "select count(id) from cs_cat_node where object_id = " // NOI18N
                        + ids[0]
                        + " and class_id = "                                                // NOI18N
                        + ids[1];

            final Connection c = conPool.getConnection().getConnection();

            final Statement s = c.createStatement();

            final ResultSet r = s.executeQuery(statement);

            if (r.next()) {
                return (r.getInt(1) == 0);
            }
        } catch (Throwable t) {
            LOG.error("Error in hasNodes", t); // NOI18N
            throw t;
        }

        // TODO: never reached is not true, false would possibly more appropriate as return value
        // never reached
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Throwable
     */
    @Override
    public Node[] getTopNodes(final UserGroup ug) throws Throwable {
        LOG.info("get top nodes for UserGroup:" + ug.getName() + "@" + ug.getDomain());                                                                                                                                      // NOI18N
        final int ug_id = idMap.getLocalUgId(ug);
        try {
            final String statement = "select  distinct "                                                                                                                                                                     // NOI18N
                        + "y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " // NOI18N
                        + "("                                                                                                                                                                                                // NOI18N
                        + "select "                                                                                                                                                                                          // NOI18N
                        + "n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  "                   // NOI18N
                        + "from "                                                                                                                                                                                            // NOI18N
                        + "cs_cat_node as n left outer join url  on ( n.descr=url.id ) "                                                                                                                                     // NOI18N
                        + "left outer join url_base as ub  on (url.url_base_id=ub.id)   "                                                                                                                                    // NOI18N
                        + "where "                                                                                                                                                                                           // NOI18N
                        + "is_root=true and node_type<>'C' "                                                                                                                                                                 // NOI18N
                        + ") as y "                                                                                                                                                                                          // NOI18N
                        + "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id="                                                                                                                        // NOI18N
                        + ug_id
                        + " left outer join cs_permission as pp on p.permission=pp.id ";                                                                                                                                     // NOI18N

            final Statement s = conPool.getConnection().getConnection().createStatement();

            final ResultSet r = s.executeQuery(statement);

            return removeUnReadableNodes(nodesFromResult(r, ug), ug);

            // Die Knoten die nicht angezeigt werden dürfen müssen noch rausgefiltert werden
        } catch (Throwable t) {
            LOG.error("Error in getTopNodes", t); // NOI18N
            throw t;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n   DOCUMENT ME!
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node[] removeUnReadableNodes(final java.util.ArrayList<Node> n, final UserGroup ug) {
        final Vector<Node> v = new Vector<Node>();
        if (LOG.isInfoEnabled()) {
            LOG.info("removeUnReadableNodes " + n.size() + " Elements before"); // NOI18N
        }
        for (final Node node : n) {
            if (node.getPermissions().hasReadPermission(ug)) {
                v.add(node);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("removeUnReadableNodes " + v.size() + " Elements after");  // NOI18N
        }
        return v.toArray(new Node[v.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     * @param   ug      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Exception
     */
    @Override
    public Node getNode(final int nodeId, final UserGroup ug) throws Throwable {
        // beschaffe lokale ug_id
        final int ug_id = idMap.getLocalUgId(ug);

        try {
            final String statement =
                "select  y.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.permission as perm_id,p.ug_id,pp.key as perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  "                                                                 // NOI18N
                        + "from"                                                                                                                                                                                                                                                     // NOI18N
                        + " (select n.id as id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name as url,iconfactory,icon,derive_permissions_from_class  from cs_cat_node as n left outer join url  on ( n.descr=url.id ) " // NOI18N
                        + "left outer join url_base as ub  on (url.url_base_id=ub.id)   "                                                                                                                                                                                            // NOI18N
                        + "where n.id="                                                                                                                                                                                                                                              // NOI18N
                        + nodeId
                        + " ) as y "                                                                                                                                                                                                                                                 // NOI18N
                        + "left outer join cs_ug_cat_node_perm as p on p.cat_node_id=y.id and ug_id = "                                                                                                                                                                              // NOI18N
                        + ug_id
                        + " "                                                                                                                                                                                                                                                        // NOI18N
                        + "left outer join cs_permission as pp on p.permission=pp.id";                                                                                                                                                                                               // NOI18N

            final Statement s = conPool.getConnection().getConnection().createStatement();

            final ResultSet r = s.executeQuery(statement);

            return nodesFromResult(r, ug).get(0);
        } catch (Throwable t) {
            LOG.error("Error in getNode", t); // NOI18N
            throw t;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  java.lang.Throwable
     */
    @Override
    public boolean nodeIsLeaf(final int nodeId) throws Throwable {
        try {
            final String statement = "select count(id_from) from cs_cat_link where id_from = " // NOI18N
                        + nodeId;

            final Connection c = conPool.getConnection().getConnection();

            final Statement s = c.createStatement();

            final ResultSet r = s.executeQuery(statement);

            if (r.next()) {
                return (r.getInt(1) == 0);
            }
        } catch (Throwable t) {
            LOG.error("Error in nodeIsLeaf", t); // NOI18N
            throw t;
        }

        // never reached
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeTable  DOCUMENT ME!
     * @param   ug         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    private java.util.ArrayList<Node> nodesFromResult(final ResultSet nodeTable, final UserGroup ug) throws Throwable {
        return nodesFromResult(nodeTable, ug, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeTable     DOCUMENT ME!
     * @param   ug            DOCUMENT ME!
     * @param   parentPolicy  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     * @throws  Exception  DOCUMENT ME!
     */
    private java.util.ArrayList<Node> nodesFromResult(final ResultSet nodeTable,
            final UserGroup ug,
            final Policy parentPolicy) throws Throwable {
        final java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>();
        final java.util.HashMap<String, Node> nodeHM = new java.util.HashMap<String, Node>();
        char c = 'N';

        while (nodeTable.next()) // add all nodes to the hashtable
        {
            // current Node
            Node tmp = null;
            int id = 0;
            String name = "";                                                                       // NOI18N
            String descr = null;
            String dynamicChildren = null;
            int classId = -1;
            try {
                if (nodeTable.getObject("class_id") != null) {                                      // NOI18N
                    classId = nodeTable.getInt("class_id");                                         // NOI18N
                }
            } catch (Exception skip) {
            }
            boolean derivePermissionsFromClass = false;
            try {
                derivePermissionsFromClass = nodeTable.getBoolean("derive_permissions_from_class"); // NOI18N
            } catch (Exception skip) {
            }
            int iconFactory = -1;
            try {
                if (nodeTable.getObject("iconfactory") != null) {                                   // NOI18N
                    iconFactory = nodeTable.getInt("iconfactory");                                  // NOI18N
                }
            } catch (Exception skip) {
            }

            String icon = null;
            try {
                if (nodeTable.getObject("icon") != null) { // NOI18N
                    icon = nodeTable.getString("icon");    // NOI18N
                }
            } catch (Exception skip) {
            }

            c = nodeTable.getString("node_type").charAt(0); // alias for the leftmost character of the   // NOI18N

            try {
                name = nodeTable.getString("name").trim(); // NOI18N
            } catch (Exception skip) {
                name = null;
            }

            descr = nodeTable.getString("url"); // NOI18N
            descr = StringTools.deleteWhitespaces(descr);

            id = nodeTable.getInt("id");                               // NOI18N
            dynamicChildren = nodeTable.getString("dynamic_children"); // NOI18N

            if (dynamicChildren != null) {
                dynamicChildren = dynamicChildren.trim();
            }
            final String domain = properties.getServerName();

            final Boolean sqlSort = nodeTable.getBoolean("sql_sort"); // NOI18N
            Policy policy = null;

            try {
                final Object policytester = nodeTable.getObject("policy"); // NOI18N

                if (policytester != null) {
                    final int p = nodeTable.getInt("policy");                                                 // NOI18N
                    policy = policyHolder.getServerPolicy(p);
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Policy was not in resultset. But is normal at DynamicChildrenStatements ", e); // NOI18N
                }
            }

            if (policy == null) {
                if ((c == (byte)'N') || (c == (byte)'n')) {
                    // Purenode
                    // bekommt die Policy des Parent-Knotens
                    if (policy == null) {
                        policy = parentPolicy;
                    }

                    // pureNodePolicy in runtime.properties
                    if (policy == null) {
                        policy = policyHolder.getServerPolicy(properties.getPureNodePolicy());
                    }
                } else if ((c == (byte)'O') || (c == (byte)'o')) {
                    // Objectnode

                    // bekommt die Policy der Klasse
                    if (policy == null) {
                        try {
                            policy = classCache.getClass(classId).getPolicy();
                        } catch (Exception e) {
                            LOG.warn("Error at: classCache.getClass(nodeTable.getInt(\"class_id\")).getPolicy() ", e); // NOI18N
                        }
                    }
                } else if ((c == (byte)'C') || (c == (byte)'c')) {
                    // Classnode
                    // classNodePolicy in runtime.properties
                    if (policy == null) {
                        policy = policyHolder.getServerPolicy(properties.getClassNodePolicy());
                    }
                } else {
                    throw new Exception("Not a known node type : " + (char)c + " ?"); // NOI18N
                }

                // serverPolicy in runtime.properties
                if (policy == null) {
                    policy = policyHolder.getServerPolicy(properties.getServerPolicy());
                }

                // PARANOID Policy als Fallback
                if (policy == null) {
                    LOG.warn("WARNING: This should not be neccessary. Setting fallback policy: PARANOID"); // NOI18N
                    policy = Policy.createParanoidPolicy();
                }
            }

            final boolean leaf = !nonLeafs.contains(new Integer(id));

            Sirius.server.localserver._class.Class metaclass = null;
            // new Node according to node type
            if ((c == (byte)'N') || (c == (byte)'n')) {
                tmp = new MetaNode(
                        id,
                        domain,
                        name,
                        descr,
                        leaf,
                        policy,
                        iconFactory,
                        icon,
                        derivePermissionsFromClass,
                        classId);
            } else {
                try {
                    metaclass = classCache.getClass(classId);
                } catch (Exception e) {
                    LOG.warn("getClass failed. cannot create objekt/classnode", e); // NOI18N
                }
                if ((metaclass != null) && metaclass.getPermissions().hasReadPermission(ug)) {
                    if ((c == (byte)'O') || (c == (byte)'o')) {
                        tmp = new MetaObjectNode(
                                id,
                                name,
                                descr,
                                domain,
                                nodeTable.getInt("object_id"),                      // NOI18N
                                classId,
                                leaf,
                                policy,
                                iconFactory,
                                icon,
                                derivePermissionsFromClass);
                    } else if ((c == (byte)'C') || (c == (byte)'c')) {
                        tmp = new MetaClassNode(
                                id,
                                domain,
                                classId,
                                name,
                                descr,
                                leaf,
                                policy,
                                iconFactory,
                                icon,
                                derivePermissionsFromClass,
                                classId);
                    } else {
                        throw new Exception("Nodetype not known : " + (char)c + " ?"); // NOI18N
                    }
                }
            }
            if (tmp != null) {
                tmp.setDynamicChildrenStatement(dynamicChildren);

                if (sqlSort != null) {
                    tmp.setSqlSort(sqlSort);
                }
                final String nodeKey = "Node:" // NOI18N
                            + tmp.getId()
                            + "@"              // NOI18N
                            + tmp.getDomain();
                // Das hinzufügen zu nodes bzw. nodesHM auf doppelte Einträge braucht/kann nur gecheckt werden , wenn
                // die Nodes nicht dynamisch sind. Deshalb der Check auf isDynamic) bzw auf nodeId==-1 (Das soll wieder
                // raus wenn isDynamic() den richtigen Wert liefert)
                if (!nodeHM.containsKey(nodeKey) || tmp.isDynamic() || (tmp.getId() == -1)) {
                    nodeHM.put(nodeKey, tmp);
                    nodes.add(tmp);
                }

                if (tmp.isDerivePermissionsFromClass()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(tmp + "(" + c + "): Permissions derived from class."); // NOI18N
                    }
                    final Sirius.server.localserver._class.Class cc = classCache.getClass(tmp.getClassId());
                    if (cc != null) {
                        tmp.setPermissions(cc.getPermissions());
                    }
                } else {
                    try {
                        final Object permId = nodeTable.getObject("perm_id");           // NOI18N
                        final String permKey = nodeTable.getString("perm_key");         // NOI18N

                        if ((permId != null) && (permKey != null)) {
                            final Permission pp = new Permission(nodeTable.getInt("perm_id"), permKey); // NOI18N
                            nodeHM.get(nodeKey).getPermissions().addPermission(ug, pp);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(
                                    "Permission "                                                       // NOI18N
                                            + pp.getKey()
                                            + " added to node"                                          // NOI18N
                                            + tmp.getId()
                                            + " for ug "                                                // NOI18N
                                            + ug.getKey().toString());
                            }
                        }
                    } catch (Throwable t) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("could not set permissions for node::" + id, t);                   // NOI18N
                        }
                    }
                }
            }
        }                                                                                               // end while

        return nodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   linkTable  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    private java.util.ArrayList<Link> linksFromResult(final ResultSet linkTable) throws Throwable {
        final java.util.ArrayList<Link> result = new java.util.ArrayList<Link>();

        final String domain = properties.getServerName();

        while (linkTable.next()) {
            final int id = linkTable.getInt("id");           // NOI18N
            String toServer = linkTable.getString("domain"); // NOI18N

            if (toServer == null) {
                toServer = domain;
            }
            result.add(new Link(id, toServer));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private HashSet<Integer> initNonLeafs() {
        // find nodes that are not leafs (a link exists) String statement = "select distinct id_to  from cs_cat_link as
        // a where id_to  in (select distinct id_from from cs_cat_link) union select distinct id from cs_cat_node where
        // is_root=true ";
        final String statement =
            "select distinct id_from from cs_cat_link union select distinct id as id_from from cs_cat_node where dynamic_children is not null"; // NOI18N

        // the non leafs are of the order 10K in wundalive
        final HashSet<Integer> nl = new HashSet<Integer>(12000);

        try {
            final Statement s = conPool.getConnection().getConnection().createStatement();

            final ResultSet r = s.executeQuery(statement);

            while (r.next()) {
                nl.add(r.getInt("id_from"));                       // NOI18N
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("added # of leafentries::" + nl.size()); // NOI18N
            }
        } catch (SQLException ex) {
            LOG.error("Error while loading Leaf property", ex);    // NOI18N
        }

        return nl;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class UserGroupIdentifiers {

        //~ Static fields/initializers -----------------------------------------

        private static final transient Logger logger = Logger.getLogger(UserGroupIdentifiers.class);

        //~ Instance fields ----------------------------------------------------

        private Map<String, Integer> idsByUgIdentifier = new HashMap<String, Integer>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupIdentifiers object.
         *
         * @param  conPool  DOCUMENT ME!
         */
        UserGroupIdentifiers(final DBConnectionPool conPool) {
            final String statement =
                "select u.id, u.name||'@'||d.name as ug_identifier  from cs_ug as u , cs_domain as d where u.domain=d.id and not (lower(d.name) = 'local')"; // NOI18N
            try {
                final Statement s = conPool.getConnection().getConnection().createStatement();

                final ResultSet r = s.executeQuery(statement);

                while (r.next()) {
                    idsByUgIdentifier.put(r.getString("ug_identifier"), r.getInt("id")); // NOI18N
                }
            } catch (SQLException ex) {
                logger.error("Fehler beim Laden der UG Fremssystemfeferenzen", ex);      // NOI18N
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   ug  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        int getLocalUgId(final UserGroup ug) {
            // beschaffe lokale ug_id
            int ug_id = ug.getId();

            final Integer localId = idsByUgIdentifier.get(ug.getKey().toString());

            // falls mapping vorhanden ersetzten
            if (localId != null) {
                ug_id = localId;
            }
            return ug_id;
        }
    }
}
