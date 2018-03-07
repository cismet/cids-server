/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CatalogueServiceImpl.java
 *
 * Created on 25. September 2003, 10:13
 */
package Sirius.server.middleware.impls.proxy;

import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.interfaces.proxy.CatalogueService;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.*;

import java.rmi.*;

import de.cismet.connectioncontext.ConnectionContext;
/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class CatalogueServiceImpl implements CatalogueService {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private final java.util.Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CatalogueServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public CatalogueServiceImpl(final java.util.Hashtable activeLocalServers) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    @Deprecated
    public Node[] getChildren(final Node node, final User user) throws RemoteException {
        return getChildren(node, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getChildren(final Node node, final User user, final ConnectionContext context)
            throws RemoteException {
        try {
            final NodeReferenceList c =
                ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                        node.getDomain())).getChildren(node, user, context);

            final Link[] links = c.getRemoteLinks();

            if (links.length == 0) {
                return c.getLocalNodes();
            }

            if ((logger != null) && logger.isInfoEnabled()) {
                logger.info("<CS> Number of remote links at getchildren :" + links.length); // NOI18N
            }

            // group Links by lsName
            final Group[] groupedLinks = this.group(links);

            // contains all nodes from links
            final java.util.Vector nodesFromLinks = new java.util.Vector(links.length);

            // temporary used in the loop to keep nodes of one ls
            Node[] n = null;

            // get the corresponding Nodes from every different localServer
            for (int i = 0; i < groupedLinks.length; i++) {
                final String lsName = groupedLinks[i].getGroup();

                if ((logger != null) && logger.isInfoEnabled()) {
                    logger.info(lsName + " gets request for node"); // NOI18N
                }

                final Sirius.server.middleware.interfaces.domainserver.CatalogueService ls =
                    (Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(lsName);

                // nodes from links

                if (ls != null) {
                    n = ls.getNodes(user, groupedLinks[i].getIDs(), context);
                } else                                                        // create dummy node to show that system
                                                                              // is not available
                {
                    if (logger != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("System :" + ls + " not available"); // NOI18N
                        }
                    }

                    final MetaNode error = new MetaNode(
                            0,
                            lsName,
                            lsName
                                    + " not available!", // NOI18N
                            lsName
                                    + " not available!", // NOI18N
                            true,
                            Policy.createParanoidPolicy(),
                            -1,
                            null,
                            false,
                            -1);

                    error.validate(false);
                    n = new Node[1];
                    n[0] = error;
                }

                // copy nodes into vector
                for (int l = 0; l < n.length; l++) {
                    nodesFromLinks.add(n[l]);
                }
            }

            // directly availabe nodes
            final Node[] lsNodes = c.getLocalNodes();

            // contains all nodes
            final Node[] result = new Node[nodesFromLinks.size() + lsNodes.length];

            // put the nodes from the fathers ls into result
            for (int i = 0; i < lsNodes.length; i++) {
                result[i] = lsNodes[i];
            }

            int offset = lsNodes.length;

            // former links into result
            for (int i = 0; i < nodesFromLinks.size(); i++) {
                result[offset] = (Node)nodesFromLinks.get(i);
                offset++;
            }

            return result;
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e, e);
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public Node[] getRoots(final User user, final String localServerName) throws RemoteException {
        return getRoots(user, localServerName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user             DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final String localServerName, final ConnectionContext context)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getRoots called " + localServerName); // NOI18N
        }
        return ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    localServerName)).getRoots(user, context).getLocalNodes();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public Node[] getRoots(final User user) throws RemoteException {
        return getRoots(user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node[] getRoots(final User user, final ConnectionContext context) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<CS> getRoots user" + user); // NOI18N
        }

        final java.util.Vector tops = new java.util.Vector(10, 10);
        final java.util.Iterator iter = activeLocalServers.values().iterator();
        Node[] topNodes = new Node[0];

        int size = 0;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> iterator for active local servers in getRoots: " + iter); // NOI18N
            }

            while (iter.hasNext()) {
                final NodeReferenceList children = (NodeReferenceList)
                    ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)iter.next()).getRoots(
                        user,
                        context);

                if ((children != null) && (children.getLocalNodes() != null)) {
                    final Node[] tmp = children.getLocalNodes();
                    if (logger.isDebugEnabled()) {
                        logger.debug("<CS> found valid localserver delivers topnodes ::" + tmp.length); // NOI18N
                    }
                    size += tmp.length;
                    tops.addElement(tmp);
                }
            }

            topNodes = new Node[size];

            for (int i = 0; i < tops.size(); i++) {
                final Node[] tmp = (Node[])tops.get(i);

                for (int j = 0; j < tmp.length; j++) {
                    --size;
                    topNodes[size] = tmp[j]; // wird von hinten nach vorne belegt
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("<CS> getTopNodes(user):", e); // NOI18N
            }

            throw new RemoteException("<CS> getTopNodes(user)", e); // NOI18N
        }

        java.util.Arrays.sort(topNodes, new NodeComparator());

        return topNodes;
    }
    /**
     * //////////////////////// Private Fkt-en ////////////////////////////////////
     *
     * @param   toBeGrouped  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Group[] group(final Groupable[] toBeGrouped) {
        if (logger.isDebugEnabled()) {
            logger.debug("private function group called "); // NOI18N
        }
        final java.util.Hashtable grouped = new java.util.Hashtable(100);

        try {
            for (int i = 0; i < toBeGrouped.length; i++) {
                final String group = toBeGrouped[i].getGroup().trim();

                if (grouped.containsKey(group)) {
                    final java.util.Vector v = (java.util.Vector)grouped.get(group);
                    v.addElement(toBeGrouped[i]);
                } else {
                    final java.util.Vector v = new java.util.Vector(20, 20);
                    v.addElement(toBeGrouped[i]);
                    grouped.put(group, v);
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
            }
        }

        // iterator to obtain the vectors
        final java.util.Iterator iter = grouped.values().iterator();

        final Group[] groups = new Group[grouped.size()];

        // counter for groups
        int j = 0;

        // get the corresponding "Groupables" from every different localServer
        while (iter.hasNext() && (j < groups.length)) // same conditions just to be shure ;-)
        {
            final java.util.Vector v = (java.util.Vector)iter.next();

            groups[j] = new Group(((Groupable)v.get(0)).getGroup(), (Groupable[])v.toArray(new Groupable[v.size()]));
            j++;
        }

        return groups;
    }

    @Override
    @Deprecated
    public Node addNode(final Node node, final Link parent, final User user) throws RemoteException {
        return addNode(node, parent, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   parent   DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public Node addNode(final Node node, final Link parent, final User user, final ConnectionContext context)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addNode called node:" + node + "parentLink ::" + parent + " user::" + user); // NOI18N
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    node.getDomain())).addNode(node, parent, user, context);
    }

    @Override
    @Deprecated
    public boolean deleteNode(final Node node, final User user) throws RemoteException {
        return deleteNode(node, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node     DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteNode(final Node node, final User user, final ConnectionContext context)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete Node called node:" + node + " user::" + user); // NOI18N
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    node.getDomain())).deleteNode(node, user, context);
    }

    @Override
    @Deprecated
    public boolean addLink(final Node from, final Node to, final User user) throws RemoteException {
        return addLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean addLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addLink called nodeFrom:" + from + "nodeTo ::" + to + " user::" + user); // NOI18N
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    from.getDomain())).addLink(from, to, user, context);
    }

    @Override
    @Deprecated
    public boolean deleteLink(final Node from, final Node to, final User user) throws RemoteException {
        return deleteLink(from, to, user, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from     DOCUMENT ME!
     * @param   to       DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Override
    public boolean deleteLink(final Node from, final Node to, final User user, final ConnectionContext context)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteLink called nodeFrom:" + from + "nodeTo ::" + to + " user::" + user); // NOI18N
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    from.getDomain())).deleteLink(from, to, user, context);
    }

//    public boolean copySubTree(Node root, User user) throws RemoteException
//    {
//         logger.debug("copysubtree called root::"+root+" user::"+user);
//        return ( (Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(root.getDomain()) ).copySubTree(root,user);
//    }
//

}
