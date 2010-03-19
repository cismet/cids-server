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
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.MetaNode;

import java.rmi.*;

import Sirius.util.*;

import Sirius.server.newuser.*;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.permission.Policy;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class CatalogueServiceImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private java.util.Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CatalogueServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public CatalogueServiceImpl(java.util.Hashtable activeLocalServers) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node[] getChildren(Node node, User user) throws RemoteException {
        try {
            NodeReferenceList c =
                ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                        node.getDomain())).getChildren(node, user);

            Link[] links = c.getRemoteLinks();

            if (links.length == 0) {
                return c.getLocalNodes();
            }

            if (logger != null) {
                logger.info("<CS> Anzahl Remote links bei getchildren :" + links.length);
            }

            // group Links by lsName
            Group[] groupedLinks = this.group(links);

            // contains all nodes from links
            java.util.Vector nodesFromLinks = new java.util.Vector(links.length);

            // temporary used in the loop to keep nodes of one ls
            Node[] n = null;

            // get the corresponding Nodes from every different localServer
            for (int i = 0; i < groupedLinks.length; i++) {
                String lsName = groupedLinks[i].getGroup();

                if (logger != null) {
                    logger.info(lsName + " kriegt anfrage nach knoten");
                }

                Sirius.server.middleware.interfaces.domainserver.CatalogueService ls =
                    (Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(lsName);

                // nodes from links

                if (ls != null) {
                    n = ls.getNodes(user, groupedLinks[i].getIDs());
                } else // create dummy node to show that system is not available
                {
                    if (logger != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("System :" + ls + " not available");
                        }
                    }

                    MetaNode error = new MetaNode(
                            0,
                            lsName,
                            lsName + " not available!",
                            lsName + " not available!",
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
            Node[] lsNodes = c.getLocalNodes();

            // contains all nodes
            Node[] result = new Node[nodesFromLinks.size() + lsNodes.length];

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
                logger.error(e);
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
    public Node[] getRoots(User user, String localServerName) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getRoots called " + localServerName);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    localServerName)).getRoots(user).getLocalNodes();
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
    public Node[] getRoots(User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("<CS> getRoots user" + user);
        }

        java.util.Vector tops = new java.util.Vector(10, 10);
        java.util.Iterator iter = activeLocalServers.values().iterator();
        Node[] topNodes = new Node[0];

        int size = 0;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("<CS> iterator for active local servers in getRoots: " + iter);
            }

            while (iter.hasNext()) {
                NodeReferenceList children = (NodeReferenceList)
                    ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)iter.next()).getRoots(user);

                if ((children != null) && (children.getLocalNodes() != null)) {
                    Node[] tmp = children.getLocalNodes();
                    if (logger.isDebugEnabled()) {
                        logger.debug("<CS> found valid localserver delivers topnodes ::" + tmp.length);
                    }
                    size += tmp.length;
                    tops.addElement(tmp);
                }
            }

            topNodes = new Node[size];

            for (int i = 0; i < tops.size(); i++) {
                Node[] tmp = (Node[])tops.get(i);

                for (int j = 0; j < tmp.length; j++) {
                    --size;
                    topNodes[size] = tmp[j]; // wird von hinten nach vorne belegt
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("<CS> getTopNodes(user):", e);
            }

            throw new RemoteException("<CS> getTopNodes(user)", e);
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
    private Group[] group(Groupable[] toBeGrouped) {
        if (logger.isDebugEnabled()) {
            logger.debug("private function group called ");
        }
        java.util.Hashtable grouped = new java.util.Hashtable(100);

        try {
            for (int i = 0; i < toBeGrouped.length; i++) {
                String group = toBeGrouped[i].getGroup().trim();

                if (grouped.containsKey(group)) {
                    java.util.Vector v = (java.util.Vector)grouped.get(group);
                    v.addElement(toBeGrouped[i]);
                } else {
                    java.util.Vector v = new java.util.Vector(20, 20);
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
        java.util.Iterator iter = grouped.values().iterator();

        Group[] groups = new Group[grouped.size()];

        // counter for groups
        int j = 0;

        // get the corresponding "Groupables" from every different localServer
        while (iter.hasNext() && (j < groups.length)) // same conditions just to be shure ;-)
        {
            java.util.Vector v = (java.util.Vector)iter.next();

            groups[j] = new Group(((Groupable)v.get(0)).getGroup(), (Groupable[])v.toArray(new Groupable[v.size()]));
            j++;
        }

        return groups;
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
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Node addNode(Node node, Link parent, User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addNode called node:" + node + "parentLink ::" + parent + " user::" + user);
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    node.getDomain())).addNode(node, parent, user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean deleteNode(Node node, User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("delete Node called node:" + node + " user::" + user);
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    node.getDomain())).deleteNode(node, user);
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
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean addLink(Node from, Node to, User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addLink called nodeFrom:" + from + "nodeTo ::" + to + " user::" + user);
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    from.getDomain())).addLink(from, to, user);
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
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean deleteLink(Node from, Node to, User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteLink called nodeFrom:" + from + "nodeTo ::" + to + " user::" + user);
        }
        return
            ((Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(
                    from.getDomain())).deleteLink(from, to, user);
    }

//    public boolean copySubTree(Node root, User user) throws RemoteException
//    {
//         logger.debug("copysubtree called root::"+root+" user::"+user);
//        return ( (Sirius.server.middleware.interfaces.domainserver.CatalogueService)activeLocalServers.get(root.getDomain()) ).copySubTree(root,user);
//    }
//

}
