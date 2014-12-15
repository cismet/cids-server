/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.tree;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface VirtualTreeStatements {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId    DOCUMENT ME!
     * @param   nodeName  DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   nodeType  DOCUMENT ME!
     * @param   root      DOCUMENT ME!
     * @param   policy    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getAddNodeStatement(int nodeId,
            String nodeName,
            int classId,
            int objectId,
            char nodeType,
            boolean root,
            String policy);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getNextNodeIdStatement();
    /**
     * DOCUMENT ME!
     *
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getClassTreeNodesStatement(String implodedUserGroupIds);
    /**
     * DOCUMENT ME!
     *
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getTopNodesStatement(String implodedUserGroupIds);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDialect();
}
