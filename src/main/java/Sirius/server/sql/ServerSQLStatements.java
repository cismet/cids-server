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
public interface ServerSQLStatements extends DialectProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   nodeId    DOCUMENT ME!
     * @param   nodeName  DOCUMENT ME!
     * @param   classKey  DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   nodeType  DOCUMENT ME!
     * @param   root      DOCUMENT ME!
     * @param   policy    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeAddNodeStatement(int nodeId,
            String nodeName,
            String classKey,
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
     * @param   artificialIdSupported  DOCUMENT ME!
     * @param   implodedUserGroupIds   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVirtualTreeTopNodesStatement(boolean artificialIdSupported, String implodedUserGroupIds);

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
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getClassGetEmptyResultStmnt(String tableName);

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
    PreparableStatement getPersistenceManagerUpdateStmt(String tableName, String pkField, String... fieldNames);

    /**
     * DOCUMENT ME!
     *
     * @param   tableName   DOCUMENT ME!
     * @param   fieldNames  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PreparableStatement getPersistenceManagerInsertStmt(String tableName, String... fieldNames);

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
    PreparableStatement getDefaultFullTextSearchStmt(String searchText,
            String classesIn,
            PreparableStatement geoSql,
            boolean caseSensitive);

    /**
     * DOCUMENT ME!
     *
     * @param   wkt        DOCUMENT ME!
     * @param   srid       DOCUMENT ME!
     * @param   classesIn  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PreparableStatement getDefaultGeoSearchStmt(String wkt, String srid, String classesIn);

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
     * @param   tableName    DOCUMENT ME!
     * @param   classId      DOCUMENT ME!
     * @param   whereClause  DOCUMENT ME!
     * @param   limit        DOCUMENT ME!
     * @param   offset       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getQueryEditorSearchPaginationStmt(String tableName, int classId, String whereClause, int limit, int offset);

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
     * @param   classKey   DOCUMENT ME!
     * @param   oneToMany  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectClassIdForeignKeyStmt(String classKey, boolean oneToMany);

    /**
     * DOCUMENT ME!
     *
     * @param   classKey     DOCUMENT ME!
     * @param   refClassKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectFieldStmt(String classKey, String refClassKey);

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
     * @param   classKey  DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIndexTriggerSelectReindexPureStmt(String classKey, int objectId);

    /**
     * Creates the statement for selecting n-m and 1-n array elements.
     *
     * @param   tableName     DOCUMENT ME!
     * @param   fieldname     DOCUMENT ME!
     * @param   referenceKey  DOCUMENT ME!
     * @param   orderByField  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getObjectFactoryGetObjectStmt(String tableName, String fieldname, String referenceKey, String orderByField);

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   implodedUserGroupIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getObjectFactoryAttrPermStmt(int classId, String implodedUserGroupIds);
}
