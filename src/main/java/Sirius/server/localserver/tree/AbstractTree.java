/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AbstractTree.java
 *
 * Created on 29. Januar 2007, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.localserver.tree;
import Sirius.server.middleware.types.Node;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.Policy;

import java.sql.SQLException;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface AbstractTree {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int getNextNodeID() throws SQLException;

    /**
     * public boolean copySubTree(Sirius.server.middleware.types.Node root, User user) throws Throwable;
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean deleteLink(Sirius.server.middleware.types.Node from, Sirius.server.middleware.types.Node to, User user)
            throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   father  DOCUMENT ME!
     * @param   child   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean addLink(int father, int child) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean addLink(Sirius.server.middleware.types.Node from, Sirius.server.middleware.types.Node to, User user)
            throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean deleteNode(Sirius.server.middleware.types.Node node, User user) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   node    DOCUMENT ME!
     * @param   parent  DOCUMENT ME!
     * @param   user    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    Sirius.server.middleware.types.Node addNode(
            Sirius.server.middleware.types.Node node,
            Sirius.server.middleware.types.Link parent,
            User user) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   nodeID        DOCUMENT ME!
     * @param   u             DOCUMENT ME!
     * @param   parentPolicy  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    NodeReferenceList getChildren(int nodeID, User u, Policy parentPolicy) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     * @param   u     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    NodeReferenceList getChildren(Node node, User u) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    Node[] getClassTreeNodes(User u) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId        DOCUMENT ME!
     * @param   parentNodeId  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    void inheritNodePermission(int nodeId, int parentNodeId) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   objectID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean hasNodes(String objectID) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    Node[] getTopNodes(User u) throws Throwable;

    /**
     * public java.util.ArrayList<Node> getObjectNodes(String objectID,UserGroup ug) throws Throwable;
     *
     * @param   nodeID  DOCUMENT ME!
     * @param   u       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    Node getNode(int nodeID, User u) throws Throwable;

    /**
     * DOCUMENT ME!
     *
     * @param   nodeID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    boolean nodeIsLeaf(int nodeID) throws Throwable;
}
