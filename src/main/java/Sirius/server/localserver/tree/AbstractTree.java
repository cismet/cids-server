/*
 * AbstractTree.java
 *
 * Created on 29. Januar 2007, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.localserver.tree;


//import Sirius.server.localserver.tree.node.*;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.Policy;
import java.sql.SQLException;

/**
 *
 * @author schlob
 */
public interface AbstractTree
{
    
    public int getNextNodeID() throws SQLException;
    
   // public boolean copySubTree(Sirius.server.middleware.types.Node root, User user) throws Throwable;
    
    public boolean deleteLink(Sirius.server.middleware.types.Node from,Sirius.server.middleware.types.Node to,User user) throws Throwable;
    
    public boolean addLink(int father , int child) throws Throwable;
    
    public boolean addLink(Sirius.server.middleware.types.Node from,Sirius.server.middleware.types.Node to, User user)throws Throwable;
    
    public boolean deleteNode(Sirius.server.middleware.types.Node node, User user) throws Throwable;
    
    public Sirius.server.middleware.types.Node  addNode(Sirius.server.middleware.types.Node node, Sirius.server.middleware.types.Link parent, User user) throws Throwable;
    
    public NodeReferenceList getChildren(int nodeID,UserGroup ug,Policy parentPolicy) throws Throwable;
    
    public NodeReferenceList getChildren(Node node,UserGroup ug) throws Throwable;
    
    public Node[] getClassTreeNodes(UserGroup ug)throws Throwable;
    
    public void inheritNodePermission(int nodeId,int parentNodeId) throws Throwable;
    
    public boolean hasNodes(String objectID) throws Throwable;
    
    public Node[]  getTopNodes(UserGroup ug) throws Throwable;
    
   // public java.util.ArrayList<Node> getObjectNodes(String objectID,UserGroup ug) throws Throwable;
    
    public Node getNode(int nodeID,UserGroup ug) throws Throwable;
    
    public boolean nodeIsLeaf(int nodeID) throws Throwable;
    
}
