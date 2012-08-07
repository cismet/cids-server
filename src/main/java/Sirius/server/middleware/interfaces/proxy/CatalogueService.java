/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;

import java.rmi.*;

/**
 * Interface for operations on the systems catalogue.
 *
 * @version  $Revision$, $Date$
 */

public interface CatalogueService extends Remote {

    //~ Methods ----------------------------------------------------------------

    // retrieves all by this user accessible rootNodes of one localserver
    /**
     * delivers root nodes of this server, if the server knows this user.
     *
     * @param   user        Usertoken 2 b checked by server, if the user exists on this server the roots are returned
     * @param   domainName  server name
     *
     * @return  all root nodes of a certain server
     *
     * @throws  RemoteException  server failure or wrong user token
     */
    Node[] getRoots(User user, String domainName) throws RemoteException;

    // retrieves all by this user accessible rootNodes
    /**
     * delivers root nodes of all servers online, if a server knows this user.
     *
     * @param   user  user token
     *
     * @return  all rootnodes available from servers online
     *
     * @throws  RemoteException  server error or wrong user token
     */
    Node[] getRoots(User user) throws RemoteException;

    // retrieves all children of node
    /**
     * delivers all nodes referenced from nodeId visible for usr.
     *
     * @param   node  id of the parent node
     * @param   usr   user token to be able to check permission and deliver user tailored views
     *
     * @return  all children of this parent visible for this user
     *
     * @throws  RemoteException  server error
     */
    // public Node[] getChildren(User usr, int nodeID, String domainName) throws RemoteException;

    Node[] getChildren(Node node, User usr) throws RemoteException;

    // retrieves all parents from the same domain note that this is not a complete set
    // of parents and as the navigation structure is a graph ther can be arbitrarily many
    /**
     * delivers a list of all nodes referencing this child node on this server.
     *
     * @param   node    usr user token
     * @param   parent  nodeID child node
     * @param   user    domain domain that hosts this child
     *
     * @return  a list of all nodes referencing this child node on this server
     *
     * @throws  RemoteException  server error
     */
    // public Node[] getParents(User usr, int nodeID, String domain) throws RemoteException;

    /**
     * enables User to add a node to the catalogue if User has sufficient permissions the new node will inherit all
     * permissions from the parent node referenced by the parent parameter.
     *
     * @param   node    new node
     * @param   parent  reference to the parent node in the catalogue
     * @param   user    user adding this node,
     *
     * @return  returns the node succesfully added
     *
     * @throws  RemoteException  server error
     */
    Node addNode(Node node, Link parent, User user) throws RemoteException;

    /**
     * deletes a certain node and all the references to it <B>on the domain where the node is hosted.</B>
     *
     * @param   node  node to be deleted
     * @param   user  the user deleting this node
     *
     * @return  whether the node was succesfully deleted
     *
     * @throws  RemoteException  server error eg unsufficient permissions
     */
    boolean deleteNode(Node node, User user) throws RemoteException;

    /**
     * links 2 existing nodes.
     *
     * @param   from  parent
     * @param   to    child
     * @param   user  user token 2 b checked for sufficient permissions for this action
     *
     * @return  whether the linking of the nodes worked
     *
     * @throws  RemoteException  server error eg one node does not exist
     */
    boolean addLink(Node from, Node to, User user) throws RemoteException;

    /**
     * removes the link between a parent and a child node.
     *
     * @param   from  parent
     * @param   to    child
     * @param   user  user token 2 b checked for sufficient permissions for this action
     *
     * @return  whether the action was completed successfully
     *
     * @throws  RemoteException  server error eg the link and/or the corresponding nodes do not exist
     */
    boolean deleteLink(Node from, Node to, User user) throws RemoteException;

    // public boolean copySubTree(Node root, User user) throws RemoteException;
}
