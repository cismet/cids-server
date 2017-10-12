/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.types.Link;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.*;

import java.rmi.*;

import de.cismet.cids.server.connectioncontext.ConnectionContext;

/**
 * Interface for operations on the systems catalogue.
 *
 * @version  $Revision$, $Date$
 */

public interface CatalogueService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    NodeReferenceList getRoots(User user) throws RemoteException;

    /**
     * retrieves all by this user accessible rootNodes of one localserver.
     *
     * @param   user     DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    NodeReferenceList getRoots(User user, ConnectionContext context) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   usr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    NodeReferenceList getChildren(Node node, User usr) throws RemoteException;

    /**
     * retrieves all children of node public NodeReferenceList getChildren(User usr, int nodeID) throws RemoteException;
     *
     * @param   node     DOCUMENT ME!
     * @param   usr      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    NodeReferenceList getChildren(Node node, User usr, ConnectionContext context) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   ids   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    Node[] getNodes(User user, int[] ids) throws RemoteException;

    /**
     * retrieves all parents from the same domain note that this is not a complete set of parents and as the navigation
     * structure is a graph ther can be arbitrarily many public Node[] getParents(User usr, int nodeID) throws
     * RemoteException;
     *
     * @param   user     DOCUMENT ME!
     * @param   ids      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Node[] getNodes(User user, int[] ids, ConnectionContext context) throws RemoteException;

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
    @Deprecated
    Node addNode(Node node, Link parent, User user) throws RemoteException;

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
    Node addNode(Node node, Link parent, User user, ConnectionContext context) throws RemoteException;

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
    @Deprecated
    boolean deleteNode(Node node, User user) throws RemoteException;

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
    boolean deleteNode(Node node, User user, ConnectionContext context) throws RemoteException;

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
    @Deprecated
    boolean addLink(Node from, Node to, User user) throws RemoteException;

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
    boolean addLink(Node from, Node to, User user, ConnectionContext context) throws RemoteException;

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
    @Deprecated
    boolean deleteLink(Node from, Node to, User user) throws RemoteException;

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
    boolean deleteLink(Node from, Node to, User user, ConnectionContext context) throws RemoteException;

    // public boolean copySubTree(Node root, User user) throws RemoteException;
}
