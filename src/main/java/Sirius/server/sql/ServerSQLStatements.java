/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface ServerSQLStatements {

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
    String getVirtualTreeAddNodeStatement(int nodeId,
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
    String getVirtualTreeNextNodeIdStatement();

    /**
     * DOCUMENT ME!
     *
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeClassTreeNodesStatement(String implodedUserGroupIds);

    /**
     * DOCUMENT ME!
     *
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeTopNodesStatement(String implodedUserGroupIds);

    /**
     * DOCUMENT ME!
     *
     * @param   artificialIdSupported  DOCUMENT ME!
     * @param   implodedUserGroupIds   DOCUMENT ME!
     * @param   nodeId                 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeLocalChildrenStmt(boolean artificialIdSupported, String implodedUserGroupIds, int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @param   serverName  DOCUMENT ME!
     * @param   nodeId      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeRemoteChildrenStmt(String serverName, int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeDeleteNodeStmt(int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeDeleteNodeLinkStmt(int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeDeleteNodePermStmt(int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @param   domain    DOCUMENT ME!
     * @param   fatherId  DOCUMENT ME!
     * @param   childId   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeAddNodeLinkStmt(String domain, int fatherId, int childId);

    /**
     * DOCUMENT ME!
     *
     * @param   domain    DOCUMENT ME!
     * @param   fatherId  DOCUMENT ME!
     * @param   childId   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeDeleteNodeLinkStmt(String domain, int fatherId, int childId);

    /**
     * DOCUMENT ME!
     *
     * @param   fatherId  DOCUMENT ME!
     * @param   childId   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeInheritNodePermStmt(int fatherId, int childId);

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     * @param   objId    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeHasNodesStmt(String classId, String objId);

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId                DOCUMENT ME!
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeGetNodeStmt(int nodeId, String implodedUserGroupIds);

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeNodeIsLeafStmt(int nodeId);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeInitNonLeafsStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getUserGroupIdentifiersUgIdentifiersStmt();

    /**
     * DOCUMENT ME!
     *
     * @param   tableName   DOCUMENT ME!
     * @param   primaryKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getClassGetInstanceStmnt(String tableName, String primaryKey);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName   DOCUMENT ME!
     * @param   primaryKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getClassGetDefaultInstanceStmnt(String tableName, String primaryKey);

    /**
     * DOCUMENT ME!
     *
     * @param   father     DOCUMENT ME!
     * @param   pk         DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   fieldName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getObjectHierarchyFatherStmt(int father, String pk, String tableName, String fieldName);

    /**
     * DOCUMENT ME!
     *
     * @param   father       DOCUMENT ME!
     * @param   fatherPk     DOCUMENT ME!
     * @param   fatherTable  DOCUMENT ME!
     * @param   attribute    DOCUMENT ME!
     * @param   arrayKey     DOCUMENT ME!
     * @param   childTable   DOCUMENT ME!
     * @param   childPk      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getObjectHierarchyFatherArrayStmt(int father,
            String fatherPk,
            String fatherTable,
            String attribute,
            String arrayKey,
            String childTable,
            String childPk);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPersistenceHelperNextvalStmt(String tableName);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     * @param   fieldName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPersistenceManagerDeleteFromStmt(String tableName, String fieldName);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName   DOCUMENT ME!
     * @param   pkField     DOCUMENT ME!
     * @param   fieldNames  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPersistenceManagerUpdateStmt(String tableName, String pkField, String... fieldNames);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName   DOCUMENT ME!
     * @param   fieldNames  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPersistenceManagerInsertStmt(String tableName, String... fieldNames);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPolicyHolderServerPoliciesStmt();

    /**
     * DOCUMENT ME!
     *
     * @param   loginName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPasswordSwitcherAdminActionSelectUserStmt(String loginName);

    /**
     * DOCUMENT ME!
     *
     * @param   loginNameToSwitch  DOCUMENT ME!
     * @param   loginNameToRead    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPasswordSwitcherAdminActionChangeAndBackupStmt(String loginNameToSwitch, String loginNameToRead);

    /**
     * DOCUMENT ME!
     *
     * @param   loginName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPasswordSwitcherAdminActionRecoveryStmt(String loginName);

    /**
     * DOCUMENT ME!
     *
     * @param   searchText     DOCUMENT ME!
     * @param   classesIn      DOCUMENT ME!
     * @param   geoSql         DOCUMENT ME!
     * @param   caseSensitive  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDefaultFullTextSearchStmt(String searchText, String classesIn, String geoSql, boolean caseSensitive);

    /**
     * DOCUMENT ME!
     *
     * @param   wkt        DOCUMENT ME!
     * @param   srid       DOCUMENT ME!
     * @param   classesIn  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDefaultGeoSearchStmt(String wkt, String srid, String classesIn);

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   attribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDistinctValuesSearchStmt(String metaClass, String attribute);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName    DOCUMENT ME!
     * @param   classId      DOCUMENT ME!
     * @param   whereClause  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getQueryEditorSearchStmt(String tableName, int classId, String whereClause);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerDeleteAttrStringObjectStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerDeleteAttrObjectObjectStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerInsertAttrStringStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerInsertAttrObjectStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerUpdateAttrStringStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerUpdateAttrObjectStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerDeleteAttrObjectArrayStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerDeleteAttrObjectDerivedStmt();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerInsertAttrObjectDerivedStmt();

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectClassIdForeignKeyStmt(int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   classId     DOCUMENT ME!
     * @param   refClassId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectFieldStmt(int classId, int refClassId);

    /**
     * DOCUMENT ME!
     *
     * @param   fieldName  DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   classId    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectObjFieldStmt(String fieldName, String tableName, int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     * @param   fk       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectBackReferenceStmt(int classId, int fk);

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectTableNameByClassIdStmt(int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     * @param   backref    DOCUMENT ME!
     * @param   classId    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectFKIdStmt(String tableName, String backref, int classId);

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectReindexPureStmt(int classId, int objectId);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDialect();
}
