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
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
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
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.cismet.commons.utils.StringUtils;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * Klasse um auf den in der DB gespeicherten Graphen zuzugreifen.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class VirtualTree extends Shutdown implements AbstractTree, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(VirtualTree.class);

    //~ Instance fields --------------------------------------------------------

    private ServerProperties properties;
    private DBConnectionPool conPool;
    private HashSet<Integer> nonLeafs;
    private UserGroupIdentifiers idMap;
    private PolicyHolder policyHolder = null;
    private ClassCache classCache = null;

    private final ConnectionContext connectionContext = ConnectionContext.create(
            AbstractConnectionContext.Category.OTHER,
            getClass().getSimpleName());

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
        this.idMap = new UserGroupIdentifiers(conPool, Lookup.getDefault().lookup(DialectProvider.class).getDialect());
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
     * @param   user          DOCUMENT ME!
     * @param   parentPolicy  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public NodeReferenceList getChildren(final int nodeId, final User user, final Policy parentPolicy)
            throws SQLException {
        final String implodedUserGroupIds = implodedUserGroupIds(user);

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

        final String localChildren = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                            .getDialect())
                    .getVirtualTreeLocalChildrenStmt(artificialIdSupported, implodedUserGroupIds, nodeId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren for " + nodeId + "  called\nlocalChildren from:" + localChildren); // NOI18N
        }

        // select remote child links
        final String remoteChildren = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                            .getDialect())
                    .getVirtualTreeRemoteChildrenStmt(properties.getServerName().trim(), nodeId);

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(localChildren);

            // add local children to result (nodes)
            final NodeReferenceList result = new NodeReferenceList(
                    removeUnReadableNodes(nodesFromResult(rs, user, parentPolicy), user));

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
     * @param   u           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public NodeReferenceList getChildren(final Node parentNode, final User u) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("NodeReferenceList getChildren(Node node, UserGroup ug) "); // NOI18N
        }
        final String statement = parentNode.getDynamicChildrenStatement();

        if (statement == null) {
            return this.getChildren(parentNode.getId(), u, null);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildren called (dynamic children)\nstatement:" + statement); // NOI18N
        }

        Statement stmt = null;
        ResultSet rs = null;
        Connection con = null;

        try {
            con = conPool.getConnection(true);
            stmt = con.createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> result = nodesFromResult(rs, u);

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

            return new NodeReferenceList(removeUnReadableNodes(result, u));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);

            if (con != null) {
                conPool.releaseDbConnection(con);
            }
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

        String policy = "null"; // NOI18N

        if ((parent == null) && (parent.getNodeId() < 0)) {
            isRoot = true;
        } else {
            final Node parentNode = getNode(parent.getNodeId(), user);
            policy = parentNode.getPermissions().getPolicy().getDbID() + ""; // NOI18N
        }
        final int nodeId = getNextNodeID();

        final String addNode = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeAddNodeStatement(
                        nodeId,
                        node.getName(),
                        classId,
                        objectId,
                        nodeType,
                        isRoot,
                        policy);

        Statement stmt = null;
        try {
            stmt = conPool.getConnection().createStatement();

            final int count = stmt.executeUpdate(addNode);

            // \u00FCbertrage rechte des Vaterknotens
            inheritNodePermission(nodeId, parent.getNodeId());

            if (!isRoot) {
                addLink(getNode(parent.getNodeId(), user), getNode(nodeId, user), user);
            }

            final Node n = getNode(nodeId, user);

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

        final String deleteNode = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeDeleteNodeStmt(node.getId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("delete Node " + deleteNode); // NOI18N
        }

        final String delLink = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeDeleteNodeLinkStmt(node.getId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("delte Link in delete node " + delLink); // NOI18N
        }

        final String delPerm = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeDeleteNodePermStmt(node.getId());

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
        final String addLink = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeAddNodeLinkStmt(properties.getServerName().trim(), father, child);

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

        final String deleteLink = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeDeleteNodeLinkStmt(to.getDomain(), from.getId(), to.getId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteLink: " + deleteLink); // NOI18N
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
        final String query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeNextNodeIdStatement();

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
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node[] getClassTreeNodes(final User user) throws SQLException {
        final String implodedUserGroupIds = implodedUserGroupIds(user);

        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeClassTreeNodesStatement(
                        implodedUserGroupIds); // NOI18N

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> nodes = nodesFromResult(rs, user);

            for (final Node n : nodes) {
                n.setLeaf(nodeIsLeaf(n.getId()));
            }

            // TODO Remove classnodes if the class is not readable
            return removeUnReadableNodes(nodes, user);
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
        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeInheritNodePermStmt(parentNodeId, nodeId);

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
        final String[] ids = objectId.split("@"); // NOI18N
        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeHasNodesStmt(ids[1], ids[0]);

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
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String implodedUserGroupIds(final User user) {
        final UserGroup userGroup = user.getUserGroup();
        final Collection<Integer> userGroupIds = new ArrayList<Integer>();
        if (userGroup != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("get top nodes for UserGroup:" + userGroup.getName() + "@" + user.getDomain());               // NOI18N
            }
            userGroupIds.add(idMap.getLocalUgId(userGroup));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("get top nodes for UserGroups:");                                                             // NOI18N
            }
            for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("                            :" + potentialUserGroup.getName() + "@" + user.getDomain()); // NOI18N
                }
                userGroupIds.add(idMap.getLocalUgId(potentialUserGroup));
            }
        }

        final String implodedUserGroupIds;
        if (userGroupIds.isEmpty()) {
            implodedUserGroupIds = "";
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final int userGroupId : userGroupIds) {
                if (sb.length() > 0) { // is the first item ?
                    sb.append(", ");
                }
                sb.append(Integer.toString(userGroupId));
            }
            implodedUserGroupIds = sb.toString();
        }

        return implodedUserGroupIds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node[] getTopNodes(final User user) throws SQLException {
        final String implodedUserGroupIds = implodedUserGroupIds(user);

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

        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeTopNodesStatement(artificialIdSupported, implodedUserGroupIds);

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            return removeUnReadableNodes(nodesFromResult(rs, user), user);

            // Die Knoten die nicht angezeigt werden dürfen müssen noch rausgefiltert werden
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n  DOCUMENT ME!
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node[] removeUnReadableNodes(final List<Node> n, final User u) {
        final List<Node> v = new ArrayList<Node>();
        if (LOG.isInfoEnabled()) {
            LOG.info("removeUnReadableNodes " + n.size() + " Elements before"); // NOI18N
        }
        for (final Node node : n) {
            if (node.getPermissions().hasReadPermission(u)) {
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
     * @param   user    u DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Node getNode(final int nodeId, final User user) throws SQLException {
        final String implodedUserGroupIds = implodedUserGroupIds(user);

        Statement stmt = null;
        ResultSet rs = null;
        try {
            final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class)
                                .getDialect())
                        .getVirtualTreeGetNodeStmt(nodeId, implodedUserGroupIds);

            stmt = conPool.getConnection().createStatement();

            rs = stmt.executeQuery(statement);

            final List<Node> nodes = nodesFromResult(rs, user);
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
        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeNodeIsLeafStmt(nodeId);

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
     * @param   u          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  Throwable DOCUMENT ME!
     */
    private List<Node> nodesFromResult(final ResultSet nodeTable, final User u) throws SQLException {
        return nodesFromResult(nodeTable, u, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodeTable     DOCUMENT ME!
     * @param   user          DOCUMENT ME!
     * @param   parentPolicy  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException           Throwable DOCUMENT ME!
     * @throws  IllegalStateException  Exception DOCUMENT ME!
     */
    private List<Node> nodesFromResult(final ResultSet nodeTable, final User user, final Policy parentPolicy)
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
            String additionaltreepermissiontagString = null;
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

            try {
                if (nodeTable.getObject("additionaltreepermissiontag") != null) {                           // NOI18N
                    additionaltreepermissiontagString = nodeTable.getString("additionaltreepermissiontag"); // NOI18N
                }
            } catch (Exception skip) {
            }
            boolean additionaltreepermissiontag;
            try {
                additionaltreepermissiontag = (additionaltreepermissiontagString != null)
                    ? DomainServerImpl.getServerInstance()
                            .hasConfigAttr(
                                    user,
                                    additionaltreepermissiontagString,
                                    getConnectionContext()) : false;
            } catch (RemoteException ex) {
                additionaltreepermissiontag = false;
                LOG.error(ex.getMessage(), ex);
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

            // Cashed Geometry
            Geometry cashedGeometry = null;
            try {
                final Object cashedGeometryTester = nodeTable.getObject("cashedGeometry"); // NOI18N

                if (cashedGeometryTester != null) {
                    cashedGeometry = SQLTools.getGeometryFromResultSetObject(cashedGeometryTester);
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("cashedGeometry was not in the resultset. But this is normal for the most parts", e); // NOI18N
                }
            }

            // Lightweight Json
            String lightweightJson = null;
            try {
                if (nodeTable.getObject("lightweightJson") != null) {         // NOI18N
                    lightweightJson = nodeTable.getString("lightweightJson"); // NOI18N
                }
            } catch (Exception skip) {
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
                if ((metaclass != null) && metaclass.getPermissions().hasReadPermission(user)) {
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
                                artifical_id,
                                cashedGeometry,
                                lightweightJson);
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
                if ((!nodeHM.containsKey(nodeKey) || tmp.isDynamic()
                                || (tmp.getId() == -1))
                            && ((additionaltreepermissiontagString == null) || additionaltreepermissiontag)) {
                    nodeHM.put(nodeKey, tmp);
                    nodes.add(tmp);
                }

                if (tmp.isDerivePermissionsFromClass()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(tmp + "(" + c + "): Permissions derived from class."); // NOI18N
                    }
                    final Sirius.server.localserver._class.Class cc = classCache.getClass(tmp.getClassId());
                    if (cc != null) {
                        tmp.setPermissions(cc.getPermissions());
                    }
                } else {
                    try {
                        final Object permId = nodeTable.getObject("perm_id");            // NOI18N
                        final String permKey = nodeTable.getString("perm_key");          // NOI18N

                        if ((permId != null) && (permKey != null)) {
                            final Permission pp = new Permission(nodeTable.getInt("perm_id"), permKey); // NOI18N
                            final UserGroup userGroup = user.getUserGroup();
                            if (userGroup != null) {
                                nodeHM.get(nodeKey).getPermissions().addPermission(userGroup, pp);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "Permission "                                                   // NOI18N
                                                + pp.getKey()
                                                + " added to node"                                      // NOI18N
                                                + tmp.getId()
                                                + " for ug "                                            // NOI18N
                                                + userGroup.getKey().toString());
                                }
                            } else {
                                for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                                    nodeHM.get(nodeKey).getPermissions().addPermission(potentialUserGroup, pp);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(
                                            "Permission "                                               // NOI18N
                                                    + pp.getKey()
                                                    + " added to node"                                  // NOI18N
                                                    + tmp.getId()
                                                    + " for ug "                                        // NOI18N
                                                    + potentialUserGroup.getKey().toString());
                                    }
                                }
                            }
                        }
                    } catch (final Exception t) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("could not set permissions for node::" + id, t);                  // NOI18N
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
        final String statement = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getVirtualTreeInitNonLeafsStmt();

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

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
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
         * @param  dialect  DOCUMENT ME!
         */
        UserGroupIdentifiers(final DBConnectionPool conPool, final String dialect) {
            final String statement = SQLTools.getStatements(dialect).getUserGroupIdentifiersUgIdentifiersStmt();

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
