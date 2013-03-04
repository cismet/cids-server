/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.tree;
import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.cismet.commons.utils.StringUtils;

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
     * @param  conPool       DOCUMENT ME!
     * @param  properties    DOCUMENT ME!
     * @param  policyHolder  DOCUMENT ME!
     * @param  classCache    DOCUMENT ME!
     */
    public VirtualTree(final DBConnectionPool conPool,
            final ServerProperties properties,
            final PolicyHolder policyHolder,
            final ClassCache classCache) {
        this.conPool = conPool;
        this.properties = properties;
        this.nonLeafs = initNonLeafs();
        this.idMap = new UserGroupIdentifiers(conPool);
        this.policyHolder = policyHolder;
        this.classCache = classCache;

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutting down VirtualTree"); // NOI18N
                    }

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
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public NodeReferenceList getChildren(final int nodeId, final UserGroup ug, final Policy parentPolicy)
            throws SQLException {
        final int ug_id = idMap.getLocalUgId(ug);

        boolean artificialIdSupported = false;
        ResultSet set = null;
        try {
            set = conPool.submitInternalQuery(DBConnection.DESC_TABLE_HAS_COLUMN, "cs_cat_node", "artificial_id"); // NOI18N
            artificialIdSupported = set.next();
        } catch (final SQLException e) {
            LOG.warn("cannot test for artificial id support, support disabled", e);                                // NOI18N
        } finally {
            DBConnection.closeResultSets(set);
        }

        //J-
        final String localChildren =
                "SELECT "                                                                                    // NOI18N
                    + "y.id AS id, "                                                                         // NOI18N
                    + "name, "                                                                               // NOI18N
                    + "class_id, "                                                                           // NOI18N
                    + "object_id, "                                                                          // NOI18N
                    + "node_type, "                                                                          // NOI18N
                    + "dynamic_children, "                                                                   // NOI18N
                    + "sql_sort, "                                                                           // NOI18N
                    + "url, "                                                                                // NOI18N
                    + "p.permission AS perm_id, "                                                            // NOI18N
                    + "p.ug_id, "                                                                            // NOI18N
                    + "pp.key AS perm_key, "                                                                 // NOI18N
                    + "y.policy, "                                                                           // NOI18N
                    + "iconfactory, "                                                                        // NOI18N
                    + "icon, "                                                                               // NOI18N
                    + "derive_permissions_from_class"                                                        // NOI18N
                    + (artificialIdSupported ? ", artificial_id " : " ")                                     // NOI18N
                + "FROM "                                                                                    // NOI18N
                    + "("                                                                                    // NOI18N
                    + "SELECT "                                                                              // NOI18N
                        + "n.id AS id, "                                                                     // NOI18N
                        + "name, "                                                                           // NOI18N
                        + "class_id, "                                                                       // NOI18N
                        + "object_id, "                                                                      // NOI18N
                        + "node_type, "                                                                      // NOI18N
                        + "dynamic_children, "                                                               // NOI18N
                        + "sql_sort, "                                                                       // NOI18N
                        + "n.policy, "                                                                       // NOI18N
                        + "prot_prefix || server || path || object_name AS url, "                            // NOI18N
                        + "iconfactory, "                                                                    // NOI18N
                        + "icon, "                                                                           // NOI18N
                        + "derive_permissions_from_class"                                                    // NOI18N
                            + (artificialIdSupported ? ", artificial_id " : " ")                             // NOI18N
                    + "FROM "                                                                                // NOI18N
                        + "cs_cat_node AS n "                                                                // NOI18N
                    + "LEFT OUTER JOIN url ON (n.descr = url.id) "                                           // NOI18N
                    + "LEFT OUTER JOIN url_base AS ub ON (url.url_base_id = ub.id) "                         // NOI18N
                    + ") AS y "                                                                              // NOI18N
                + "LEFT OUTER JOIN cs_ug_cat_node_perm AS p ON (p.cat_node_id = y.id) "                      // NOI18N
                + "LEFT OUTER JOIN cs_permission AS pp ON (p.permission = pp.id AND ug_id = " + ug_id + ") " // NOI18N
                + "WHERE "                                                                                   // NOI18N
                    + "y.id IN (SELECT id_to FROM cs_cat_link WHERE id_from = " + nodeId + ") ";             // NOI18N
        //J+

        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren for " + nodeId + "  called\nlocalChildren from:" + localChildren); // NOI18N
        }

        // select remote child links

        //J-
        final String remoteChildren =
                "SELECT "                                                                          // NOI18N
                    + "id_to AS id, "                                                              // NOI18N
                    + "domain_to AS domain "                                                       // NOI18N
                + "FROM "                                                                          // NOI18N
                    + "cs_cat_link "                                                               // NOI18N
                + "WHERE "                                                                         // NOI18N
                    + "domain_to NOT IN "                                                          // NOI18N
                        + "( "                                                                     // NOI18N
                        + "SELECT "                                                                // NOI18N
                            + "id "                                                                // NOI18N
                        + "FROM "                                                                  // NOI18N
                            + "cs_domain "                                                         // NOI18N
                        + "WHERE "                                                                 // NOI18N
                            + "lower(name)='local' "                                               // NOI18N
                            + "OR lower(name)= lower('" + properties.getServerName().trim() + "')" // NOI18N
                        + ") "                                                                     // NOI18N
                    + "AND id_from = " + nodeId;                                                   // NOI18N
        //J+

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(localChildren);

            // add local children to result (nodes)
            final NodeReferenceList result = new NodeReferenceList(
                    removeUnReadableNodes(nodesFromResult(rs, ug, parentPolicy), ug));

            DBConnection.closeResultSets(rs);

            rs = stmt.executeQuery(remoteChildren);

            // add remote children (links)
            result.setRemotes(linksFromResult(rs));

            return result;
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parentNode  DOCUMENT ME!
     * @param   ug          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public NodeReferenceList getChildren(final Node parentNode, final UserGroup ug) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("NodeReferenceList getChildren(Node node, UserGroup ug) "); // NOI18N
        }
        final String statement = parentNode.getDynamicChildrenStatement();

        if (statement == null) {
            return this.getChildren(parentNode.getId(), ug, null);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren called (dynamic children)\nstatement:" + statement); // NOI18N
        }

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> result = nodesFromResult(rs, ug);

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
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
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
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Sirius.server.middleware.types.Node addNode(final Node node, final Link parent, final User user)
            throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNode called :: " + node); // NOI18N
        }

        Integer classId = null;
        Integer objectId = null;
        char nodeType = 'N';

        if (node instanceof MetaObjectNode) {
            final MetaObjectNode oNode = (MetaObjectNode)node;
            objectId = new Integer(oNode.getObjectId());
            classId = new Integer(oNode.getClassId());
            nodeType = 'O';
        } else if (node instanceof MetaClassNode) {
            final MetaClassNode cNode = (MetaClassNode)node;
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
                    + " )";                                                                                                                      // NOI18N

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();

            final int count = stmt.executeUpdate(addNode);

            // \u00FCbertrage rechte des Vaterknotens
            inheritNodePermission(nodeId, parent.getNodeId());

            if (!isRoot) {
                addLink(getNode(parent.getNodeId(), user.getUserGroup()), getNode(nodeId, user.getUserGroup()), user);
            }

            final Node n = getNode(nodeId, user.getUserGroup());

            return n;
        } finally {
            DBConnection.closeStatements(stmt);
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
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user) throws SQLException {
        if (!nodeIsLeaf(node.getId())) {
            LOG.error("Node is no leaf, cannot delete!!!"); // NOI18N
            return false;
        }

        final String deleteNode = "DELETE FROM cs_cat_node where id = "
                    + node.getId(); // NOI18N

        if (LOG.isDebugEnabled()) {
            LOG.debug("delete Node " + deleteNode); // NOI18N
        }

        final String delLink = "DELETE FROM cs_cat_link where id_to = "
                    + node.getId(); // NOI18N

        if (LOG.isDebugEnabled()) {
            LOG.debug("delte Link in delete node " + delLink); // NOI18N
        }

        final String delPerm = "DELETE FROM cs_ug_cat_node_perm where cat_node_id = "
                    + node.getId(); // NOI18N

        if (LOG.isDebugEnabled()) {
            LOG.debug("delete permission statement; " + delPerm); // NOI18N
        }

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();

            stmt.executeUpdate(deleteNode);
            stmt.executeUpdate(delLink);
            stmt.executeUpdate(delPerm);
        } finally {
            DBConnection.closeStatements(stmt);
        }

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
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final int father, final int child) throws SQLException {
        final String addLink = "INSERT INTO cs_cat_link (id_from,id_to,org,domain_to) values(" // NOI18N
                    + father
                    + ","                                                                      // NOI18N
                    + child
                    + ",null, (select id from cs_domain where name ='"                         // NOI18N
                    + properties.getServerName().trim()
                    + "'))";                                                                   // NOI18N

        if (LOG.isDebugEnabled()) {
            LOG.debug("addLink " + addLink); // NOI18N
        }

        // update the noLeaf Hash as the Father is no Leaf anymore
        nonLeafs.add(new Integer(father));

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();
            return stmt.executeUpdate(addLink)
                        > 0;
        } finally {
            DBConnection.closeStatements(stmt);
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
     * @throws  SQLException  java.lang.Exception
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user) throws SQLException {
        if (LOG.isDebugEnabled()) {
            // user check notwendig ??
            LOG.debug("delete link from :" + from.toString() + " to :" + to.toString()); // NOI18N
        }

        final String deleteLink = "DELETE FROM cs_cat_link WHERE id_from = "   // NOI18N
                    + from.getId()
                    + " AND id_to =  "                                         // NOI18N
                    + to.getId()
                    + " AND domain_to = "                                      // NOI18N
                    + "( SELECT id from cs_domain where lower(name) = lower('" // NOI18N
                    + to.getDomain()
                    + "')  )";                                                 // NOI18N
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteLink: " + deleteLink);                            // NOI18N
        }

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();

            final int affected = stmt.executeUpdate(deleteLink);

            try {
                if (nodeIsLeaf(from.getId())) {
                    nonLeafs.remove(from.getId());
                }
            } catch (final SQLException ex) {
                LOG.error("could not update nonLeafCache", ex); // NOI18N
            }

            return affected
                        > 0;
        } finally {
            DBConnection.closeStatements(stmt);
        }
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

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();
            rs = stmt.executeQuery(query);

            if (rs.next()) {
                return (rs.getInt(1));
            } else {
                return 1;
            }
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
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
     * @throws  SQLException  DOCUMENT ME!
     */
    // TODO: why is the user needed if it is not used at all
    @Override
    public boolean addLink(final Node from, final Node to, final User user) throws SQLException {
        return addLink(from.getId(), to.getId());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final UserGroup ug) throws SQLException {
        final int ug_id = idMap.getLocalUgId(ug);

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

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> nodes = nodesFromResult(rs, ug);

            for (final Node n : nodes) {
                n.setLeaf(nodeIsLeaf(n.getId()));
            }

            // TODO Remove classnodes if the class is not readable
            return removeUnReadableNodes(nodes, ug);
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId        DOCUMENT ME!
     * @param   parentNodeId  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public void inheritNodePermission(final int nodeId, final int parentNodeId) throws SQLException {
        // precondition id is set with autokey (sequence)
        final String statement =
            "insert into cs_ug_cat_node_perm  (ug_id,cat_node_id,permission)  (select ug_id," // NOI18N
                    + nodeId
                    + ",permission  from cs_ug_cat_node_perm where cat_node_id="              // NOI18N
                    + parentNodeId
                    + ")";                                                                    // NOI18N

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();

            stmt.executeUpdate(statement);
        } finally {
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public boolean hasNodes(final String objectId) throws SQLException {
        // oId@cId
        final String[] ids = objectId.split("@");                                       // NOI18N
        final String statement = "select count(id) from cs_cat_node where object_id = " // NOI18N
                    + ids[0]
                    + " and class_id = "                                                // NOI18N
                    + ids[1];

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            if (rs.next()) {
                return (rs.getInt(1) == 0);
            }
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }

        assert false : "reached unreachable code"; // NOI18N

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node[] getTopNodes(final UserGroup ug) throws SQLException {
        LOG.info("get top nodes for UserGroup:" + ug.getName() + "@" + ug.getDomain()); // NOI18N
        final int ug_id = idMap.getLocalUgId(ug);

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

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            return removeUnReadableNodes(nodesFromResult(rs, ug), ug);

            // Die Knoten die nicht angezeigt werden dürfen müssen noch rausgefiltert werden
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
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
    private Node[] removeUnReadableNodes(final List<Node> n, final UserGroup ug) {
        final List<Node> v = new ArrayList<Node>();
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
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node getNode(final int nodeId, final UserGroup ug) throws SQLException {
        // beschaffe lokale ug_id
        final int ug_id = idMap.getLocalUgId(ug);

        Statement stmt = null;
        ResultSet rs = null;
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

            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> nodes = nodesFromResult(rs, ug);
            if (nodes.isEmpty()) {
                return null;
            } else {
                return nodes.get(0);
            }
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public boolean nodeIsLeaf(final int nodeId) throws SQLException {
        final String statement = "select count(id_from) from cs_cat_link where id_from = "
                    + nodeId; // NOI18N

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            rs.next();

            return (rs.getInt(1) == 0);
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeTable  DOCUMENT ME!
     * @param   ug         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  Throwable DOCUMENT ME!
     */
    private List<Node> nodesFromResult(final ResultSet nodeTable, final UserGroup ug) throws SQLException {
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
     * @throws  SQLException           Throwable DOCUMENT ME!
     * @throws  IllegalStateException  Exception DOCUMENT ME!
     */
    private List<Node> nodesFromResult(final ResultSet nodeTable, final UserGroup ug, final Policy parentPolicy)
            throws SQLException {
        final List<Node> nodes = new ArrayList<Node>();
        final Map<String, Node> nodeHM = new HashMap<String, Node>();
        char c = 'N';

        while (nodeTable.next()) // add all nodes to the hashtable
        {
            // current Node
            Node tmp = null;
            int id = 0;
            String name = "";                                                                       // NOI18N
            String descr = null;
            String dynamicChildren = null;
            String artifical_id = null;
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

            try {
                artifical_id = nodeTable.getString("artificial_id").trim(); // NOI18N
            } catch (final Exception e) {
                // artificial id not present, don't care
            }

            descr = nodeTable.getString("url"); // NOI18N
            descr = StringUtils.deleteWhitespaces(descr);

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
                    throw new IllegalStateException("Not a known node type : " + c + " ?"); // NOI18N
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
                        classId,
                        artifical_id);
            } else {
                try {
                    metaclass = classCache.getClass(classId);
                } catch (Exception e) {
                    // FIXME: doesn't
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
                                derivePermissionsFromClass,
                                artifical_id);
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
                                classId,
                                artifical_id);
                    } else {
                        throw new IllegalStateException("Nodetype not known : " + c + " ?"); // NOI18N
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
                    } catch (final Exception t) {
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
     * @throws  SQLException  Throwable DOCUMENT ME!
     */
    private java.util.ArrayList<Link> linksFromResult(final ResultSet linkTable) throws SQLException {
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
        // TODO: optimisation: probably a select count() would be useful here since the normal catalogue is not used
        // that much anymore resulting in far less nonleafs than before. Thus the set will probably far too large.
        // In addition to that we are in the init method. An additional select count would not be to bad here.
        final HashSet<Integer> nl = new HashSet<Integer>(12000);

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            while (rs.next()) {
                nl.add(rs.getInt("id_from"));                      // NOI18N
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("added # of leafentries::" + nl.size()); // NOI18N
            }
        } catch (SQLException ex) {
            LOG.error("Error while loading Leaf property", ex);    // NOI18N
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
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

        private static final transient Logger LOG = Logger.getLogger(UserGroupIdentifiers.class);

        //~ Instance fields ----------------------------------------------------

        private final Map<String, Integer> idsByUgIdentifier = new HashMap<String, Integer>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupIdentifiers object.
         *
         * @param  conPool  DOCUMENT ME!
         */
        UserGroupIdentifiers(final DBConnectionPool conPool) {
            final String statement =
                "select u.id, u.name||'@'||d.name as ug_identifier  from cs_ug as u , cs_domain as d where u.domain=d.id and not (lower(d.name) = 'local')"; // NOI18N

            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conPool.getConnection().createStatement();
                rs = stmt.executeQuery(statement);

                while (rs.next()) {
                    idsByUgIdentifier.put(rs.getString("ug_identifier"), rs.getInt("id")); // NOI18N
                }
            } catch (final SQLException ex) {
                LOG.error("Fehler beim Laden der UG Fremssystemfeferenzen", ex);           // NOI18N
            } finally {
                DBConnection.closeResultSets(rs);
                DBConnection.closeStatements(stmt);
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
