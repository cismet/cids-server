package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.localserver.tree.NodeReferenceList;
import Sirius.server.middleware.types.Link;
import java.rmi.*;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.*;

/**Interface for operations on the systems catalogue*/


public interface CatalogueService extends Remote
{
    
    
    
    // retrieves all by this user accessible rootNodes of one localserver
    public NodeReferenceList getRoots(User user) throws RemoteException;
    
    
    
    //retrieves all children of node
   // public NodeReferenceList getChildren(User usr, int nodeID) throws RemoteException;
    
   public NodeReferenceList getChildren(Node node,User usr) throws RemoteException;
    
    
// retrieves all parents from the same domain note that this is not a complete set
    // of parents and as the navigation structure is a graph ther can be arbitrarily many
    //public Node[] getParents(User usr, int nodeID) throws RemoteException;
    
    
    
    public Node[] getNodes(User user,int[] ids) throws RemoteException;
    
    
    public Node addNode(Node node, Link parent, User user) throws RemoteException;
    
    public boolean deleteNode(Node node, User user) throws RemoteException;
    
    public boolean addLink(Node from,Node to, User user) throws RemoteException;
    
    public boolean deleteLink(Node from,Node to,User user) throws RemoteException;
    
    //public boolean copySubTree(Node root, User user) throws RemoteException;
    
    
}