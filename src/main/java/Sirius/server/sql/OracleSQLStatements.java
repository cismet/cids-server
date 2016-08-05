/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import org.openide.util.lookup.ServiceProvider;

import java.sql.Types;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  0.2
 */
@ServiceProvider(service = ServerSQLStatements.class)
public final class OracleSQLStatements implements ServerSQLStatements {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getVirtualTreeAddNodeStatement(final int nodeId,
            final String nodeName,
            final int classId,
            final int objectId,
            final char nodeType,
            final boolean root,
            final String policy) {
        return
            "insert into cs_cat_node (id, name, descr, class_id, object_id, node_type, is_root, org,dynamic_children,sql_sort,policy) values ( " // NOI18N
                    + nodeId
                    + ",'"                                                                                                                       // NOI18N
                    + nodeName
                    + "',1,"                                                                                                                     // NOI18N
                    + classId
                    + ","                                                                                                                        // NOI18N
                    + objectId
                    + ",'"                                                                                                                       // NOI18N
                    + nodeType
                    + "','"                                                                                                                      // NOI18N
                    + (root ? "1" : "0")
                    + "', NULL,NULL,false,"                                                                                                      // NOI18N
                    + policy
                    + " )";
    }

    @Override
    public String getVirtualTreeNextNodeIdStatement() {
        return "SELECT cs_cat_node_sequence.nextval FROM DUAL";
    }

    @Override
    public String getVirtualTreeClassTreeNodesStatement(final String implodedUserGroupIds) {
        return "select "                                                                                                                                                                                        // NOI18N
                    + "y.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " // NOI18N
                    + "("                                                                                                                                                                                       // NOI18N
                    + "select "                                                                                                                                                                                 // NOI18N
                    + "n.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  "                // NOI18N
                    + "from "                                                                                                                                                                                   // NOI18N
                    + "cs_cat_node n left outer join url  on ( n.descr=url.id ) "                                                                                                                               // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                              // NOI18N
                    + "where "                                                                                                                                                                                  // NOI18N
                    + "is_root='1' and node_type='C' "                                                                                                                                                          // NOI18N
                    + ") y "                                                                                                                                                                                    // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and nvl(ug_id, '-1') IN ("                                                                                                   // NOI18N
                    + implodedUserGroupIds
                    + ") left outer join cs_permission pp on p.permission=pp.id ";
    }

    @Override
    public String getVirtualTreeTopNodesStatement(final boolean artificialIdSupported,
            final String implodedUserGroupIds) {
        return "select "                                                                                                                                                                                 // NOI18N
                    + "y.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class" // NOI18N
                    + ((artificialIdSupported) ? ",artificial_id" : "")
                    + " from ("                                                                                                                                                                          // NOI18N
                    + "select "                                                                                                                                                                          // NOI18N
                    + "n.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  "         // NOI18N
                    + ((artificialIdSupported) ? ",artificial_id" : "")
                    + " from "                                                                                                                                                                           // NOI18N
                    + "cs_cat_node n left outer join url  on ( n.descr=url.id ) "                                                                                                                        // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                       // NOI18N
                    + "where "                                                                                                                                                                           // NOI18N
                    + "is_root='1' and node_type<>'C' "                                                                                                                                                  // NOI18N
                    + ") y "                                                                                                                                                                             // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and nvl(ug_id, '-1') IN ("
                    + implodedUserGroupIds
                    + ") left outer join cs_permission pp on p.permission=pp.id ";
    }

    @Override
    public String getVirtualTreeLocalChildrenStmt(final boolean artificialIdSupported,
            final String implodedUserGroupIds,
            final int nodeId) {
        //J-
        return
                "SELECT "                                                                                    // NOI18N
                    + "y.id id, "                                                                         // NOI18N
                    + "name, "                                                                               // NOI18N
                    + "class_id, "                                                                           // NOI18N
                    + "object_id, "                                                                          // NOI18N
                    + "node_type, "                                                                          // NOI18N
                    + "dynamic_children, "                                                                   // NOI18N
                    + "sql_sort, "                                                                           // NOI18N
                    + "url, "                                                                                // NOI18N
                    + "p.permission perm_id, "                                                            // NOI18N
                    + "p.ug_id, "                                                                            // NOI18N
                    + "pp.key perm_key, "                                                                 // NOI18N
                    + "y.policy, "                                                                           // NOI18N
                    + "iconfactory, "                                                                        // NOI18N
                    + "icon, "                                                                               // NOI18N
                    + "derive_permissions_from_class"                                                        // NOI18N
                    + (artificialIdSupported ? ", artificial_id " : " ")                                     // NOI18N
                + "FROM "                                                                                    // NOI18N
                    + "("                                                                                    // NOI18N
                    + "SELECT "                                                                              // NOI18N
                        + "n.id id, "                                                                     // NOI18N
                        + "name, "                                                                           // NOI18N
                        + "class_id, "                                                                       // NOI18N
                        + "object_id, "                                                                      // NOI18N
                        + "node_type, "                                                                      // NOI18N
                        + "dynamic_children, "                                                               // NOI18N
                        + "sql_sort, "                                                                       // NOI18N
                        + "n.policy, "                                                                       // NOI18N
                        + "prot_prefix || server || path || object_name url, "                            // NOI18N
                        + "iconfactory, "                                                                    // NOI18N
                        + "icon, "                                                                           // NOI18N
                        + "derive_permissions_from_class"                                                    // NOI18N
                            + (artificialIdSupported ? ", artificial_id " : " ")                             // NOI18N
                    + "FROM "                                                                                // NOI18N
                        + "cs_cat_node n "                                                                // NOI18N
                    + "LEFT OUTER JOIN url ON (n.descr = url.id) "                                           // NOI18N
                    + "LEFT OUTER JOIN url_base ub ON (url.url_base_id = ub.id) "                         // NOI18N
                    + ") y "                                                                              // NOI18N
                + "LEFT OUTER JOIN cs_ug_cat_node_perm p ON (p.cat_node_id = y.id) "                      // NOI18N
                + "LEFT OUTER JOIN cs_permission pp ON (p.permission = pp.id AND nvl(ug_id, '-1') IN (" + implodedUserGroupIds + ")) " // NOI18N
                + "WHERE "                                                                                   // NOI18N
                    + "y.id IN (SELECT id_to FROM cs_cat_link WHERE id_from = " + nodeId + ") ";             // NOI18N
        //J+
    }

    @Override
    public String getVirtualTreeRemoteChildrenStmt(final String serverName, final int nodeId) {
        //J-
        return
                "SELECT "                                                                          // NOI18N
                    + "id_to id, "                                                              // NOI18N
                    + "domain_to domain "                                                       // NOI18N
                + "FROM "                                                                          // NOI18N
                    + "cs_cat_link "                                                               // NOI18N
                + "WHERE "                                                                         // NOI18N
                    + "domain_to NOT IN "                                                          // NOI18N
                        + "( "                                                                     // NOI18N
                        + "SELECT "                                                                // NOI18N
                            + "id "                                                                // NOI18N
                        + "FROM "                                                                  // NOI18N
                            + "cs_domain "                                                         // NOI18N
                        + "WHERE "                                                                 // NOI18N
                            + "lower(name)='local' "                                               // NOI18N
                            + "OR lower(name)= lower('" + serverName + "')" // NOI18N
                        + ") "                                                                     // NOI18N
                    + "AND id_from = " + nodeId;                                                   // NOI18N
        //J+
    }

    @Override
    public String getVirtualTreeDeleteNodeStmt(final int nodeId) {
        return "DELETE FROM cs_cat_node where id = "
                    + nodeId;
    }

    @Override
    public String getVirtualTreeDeleteNodeLinkStmt(final int nodeId) {
        return "DELETE FROM cs_cat_link where id_to = "
                    + nodeId;
    }

    @Override
    public String getVirtualTreeDeleteNodePermStmt(final int nodeId) {
        return "DELETE FROM cs_ug_cat_node_perm where cat_node_id = "
                    + nodeId;
    }

    @Override
    public String getVirtualTreeAddNodeLinkStmt(final String domain, final int fatherId, final int childId) {
        return "INSERT INTO cs_cat_link (id_from,id_to,org,domain_to) values(" // NOI18N
                    + fatherId
                    + ","                                                      // NOI18N
                    + childId
                    + ",null, (select id from cs_domain where name ='"         // NOI18N
                    + domain
                    + "'))";
    }

    @Override
    public String getVirtualTreeDeleteNodeLinkStmt(final String domain, final int fatherId, final int childId) {
        return "DELETE FROM cs_cat_link WHERE id_from = "                      // NOI18N
                    + fatherId
                    + " AND id_to =  "                                         // NOI18N
                    + childId
                    + " AND domain_to = "                                      // NOI18N
                    + "( SELECT id from cs_domain where lower(name) = lower('" // NOI18N
                    + domain
                    + "')  )";                                                 // NOI18N
    }

    @Override
    public String getVirtualTreeInheritNodePermStmt(final int fatherId, final int childId) {
        return "insert into cs_ug_cat_node_perm  (ug_id,cat_node_id,permission)  (select ug_id," // NOI18N
                    + childId
                    + ",permission  from cs_ug_cat_node_perm where cat_node_id="                 // NOI18N
                    + fatherId
                    + ")";                                                                       // NOI18N
    }

    @Override
    public String getVirtualTreeHasNodesStmt(final String classId, final String objId) {
        return "select count(id) from cs_cat_node where object_id = " // NOI18N
                    + objId
                    + " and class_id = "                              // NOI18N
                    + classId;
    }

    @Override
    public String getVirtualTreeGetNodeStmt(final int nodeId, final String implodedUserGroupIds) {
        return
            "select  y.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url , p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  "                                                                 // NOI18N
                    + "from"                                                                                                                                                                                                                                            // NOI18N
                    + " (select n.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  from cs_cat_node n left outer join url  on ( n.descr=url.id ) " // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                                                                                      // NOI18N
                    + "where n.id="                                                                                                                                                                                                                                     // NOI18N
                    + nodeId
                    + " ) y "                                                                                                                                                                                                                                           // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and ug_id IN ("                                                                                                                                                                      // NOI18N
                    + implodedUserGroupIds
                    + ") "                                                                                                                                                                                                                                              // NOI18N
                    + "left outer join cs_permission pp on p.permission=pp.id";
    }

    @Override
    public String getVirtualTreeNodeIsLeafStmt(final int nodeId) {
        return "select count(id_from) from cs_cat_link where id_from = "
                    + nodeId;
    }

    @Override
    public String getVirtualTreeInitNonLeafsStmt() {
        return
            "select distinct id_from from cs_cat_link union select distinct id id_from from cs_cat_node where dynamic_children is not null"; // NOI18N
    }

    @Override
    public String getUserGroupIdentifiersUgIdentifiersStmt() {
        return
            "select u.id, u.name||'@'||d.name ug_identifier  from cs_ug u , cs_domain d where u.domain=d.id and not (lower(d.name) = 'local')"; // NOI18N
    }

    @Override
    public String getClassGetInstanceStmnt(final String tableName, final String primaryKey) {
        return "Select * from "
                    + tableName
                    + " where "
                    + primaryKey
                    + " = ?";
    }

    @Override
    public String getClassGetDefaultInstanceStmnt(final String tableName, final String primaryKey) {
        return "Select * from "
                    + tableName
                    + " where "
                    + primaryKey         // NOI18N
                    + " = (select min( " // NOI18N
                    + primaryKey
                    + ") from "
                    + tableName
                    + ")";               // NOI18N
    }

    @Override
    public String getObjectHierarchyFatherStmt(final int father,
            final String pk,
            final String tableName,
            final String fieldName) {
        return "Select "
                    + father
                    + " class_id , "
                    + pk
                    + " object_id"
                    + " from "
                    + tableName
                    + " where "
                    + fieldName
                    + " = ";
    }

    @Override
    public String getObjectHierarchyFatherArrayStmt(final int father,
            final String fatherPk,
            final String fatherTable,
            final String attribute,
            final String arrayKey,
            final String childTable,
            final String childPk) {
        return "Select "
                    + father
                    + " class_id ,"
                    + fatherPk
                    + " object_id"
                    + " from " // NOI18N
                    + fatherTable
                    + " where "
                    + attribute
                    + " in "
                    + " (select "
                    + arrayKey
                    + " from "
                    + childTable
                    + " where  "
                    + childPk
                    + " = ";
    }

    @Override
    public String getPersistenceHelperNextvalStmt(final String tableName) {
        return "SELECT "
                    + tableName
                    + "_SEQ.NEXTVAL FROM DUAL"; // NOI18N
    }

    @Override
    public String getPersistenceManagerDeleteFromStmt(final String tableName, final String fieldName) {
        return "DELETE FROM "
                    + tableName
                    + " WHERE "
                    + fieldName
                    + " = ?"; // NOI18N
    }

    @Override
    public PreparableStatement getPersistenceManagerUpdateStmt(final String tableName,
            final String pkField,
            final String... fieldNames) {
        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ").append(tableName);
        sb.append(" SET ");

        for (final String fieldName : fieldNames) {
            sb.append(fieldName).append(" = ?, ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append(" WHERE ").append(pkField).append(" = ?");

        return new PreparableStatement(sb.toString());
    }

    @Override
    public PreparableStatement getPersistenceManagerInsertStmt(final String tableName, final String... fieldNames) {
        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(tableName);
        sb.append(" (");

        for (final String fieldName : fieldNames) {
            sb.append(fieldName).append(", ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append(") VALUES (");

        for (int i = 0; i < fieldNames.length; ++i) {
            sb.append("?, ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append(")");

        return new PreparableStatement(sb.toString());
    }

    @Override
    public String getPolicyHolderServerPoliciesStmt() {
        return "SELECT "                                             // NOI18N
                    + "cs_policy.id policyid, "                      // NOI18N
                    + "cs_policy.name,"                              // NOI18N
                    + "cs_permission.id permissionid,"               // NOI18N
                    + "cs_permission.key,"                           // NOI18N
                    + "default_value "                               // NOI18N
                    + "FROM "                                        // NOI18N
                    + "cs_permission,"                               // NOI18N
                    + "cs_policy,"                                   // NOI18N
                    + "cs_policy_rule "                              // NOI18N
                    + "WHERE "                                       // NOI18N
                    + "cs_policy_rule.permission= cs_permission.id " // NOI18N
                    + "and cs_policy_rule.policy= cs_policy.id ";    // NOI18N
    }

    @Override
    public String getPasswordSwitcherAdminActionSelectUserStmt(final String loginName) {
        return "SELECT * FROM cs_usr WHERE login_name = '"
                    + loginName
                    + "'";
    }

    @Override
    public String getPasswordSwitcherAdminActionChangeAndBackupStmt(final String loginNameToSwitch,
            final String loginNameToRead) {
        return "UPDATE cs_usr SET last_password = password, password = "
                    + "(SELECT password FROM cs_usr WHERE login_name = '"
                    + loginNameToRead
                    + "') "
                    + "WHERE login_name = '"
                    + loginNameToSwitch
                    + "'";
    }

    @Override
    public String getPasswordSwitcherAdminActionRecoveryStmt(final String loginName) {
        return "UPDATE cs_usr SET password = last_password, last_password = NULL WHERE login_name = '"
                    + loginName
                    + "'";
    }

    @Override
    public PreparableStatement getDefaultFullTextSearchStmt(final String searchText,
            final String classesIn,
            final PreparableStatement geoSql,
            final boolean caseSensitive) {
        // NOTE: lower of oracle and lower of java may behave differently
        final String sql = "SELECT ocid,\n"
                    + "  oid,\n"
                    + "  stringrep,\n"
                    + "  geometry,\n"
                    + "  lightweight_json\n"
                    + "FROM\n"
                    + "  (SELECT i.class_id ocid,\n"
                    + "    i.object_id oid,\n"
                    + "    c.stringrep,\n"
                    + "    c.geometry,\n"
                    + "    c.lightweight_json,\n"
                    + "    row_number() over (partition BY i.class_id, i.object_id order by rownum) rn\n"
                    + "FROM   cs_attr_string s, "                     // NOI18N
                    + "       cs_attr_object_derived i "              // NOI18N
                    + "       LEFT OUTER JOIN cs_cache c "            // NOI18N
                    + "       ON     ( "                              // NOI18N
                    + "                     c.class_id =i.class_id "  // NOI18N
                    + "              AND    c.object_id=i.object_id " // NOI18N
                    + "              ) "                              // NOI18N
                    + "WHERE  i.attr_class_id = s.class_id "          // NOI18N
                    + "AND    i.attr_object_id=s.object_id "          // NOI18N
                    + "AND    "
                    + (caseSensitive ? "" : "lower(")
                    + "s.string_val"
                    + (caseSensitive ? "" : ")")
                    + " like ? "                                      // NOI18N
                    + "AND i.class_id IN "
                    + classesIn
                    + "  )\n"
                    + "WHERE rn = 1";
        final PreparableStatement ps;

        final String param = "%"
                    + (caseSensitive ? searchText : searchText.toLowerCase())
                    + "%";
        if (geoSql == null) {
            ps = new PreparableStatement(sql, Types.CLOB);
            ps.setObjects(param);
        } else {
            final int[] types = new int[geoSql.getTypes().length
                            + 1];
            System.arraycopy(geoSql.getTypes(), 0, types, 1, geoSql.getTypes().length);
            types[0] = Types.CLOB;

            final Object[] objects = new Object[geoSql.getObjects().length
                            + 1];
            System.arraycopy(geoSql.getObjects(), 0, objects, 1, geoSql.getObjects().length);
            objects[0] = param;

            ps = new PreparableStatement("SELECT TXT_RESULTS.* FROM (\n"
                            + sql
                            + "\n) TXT_RESULTS \n"
                            + "JOIN (" + geoSql.getStatement() + ") GEO_RESULTS \n"
                            + "ON (TXT_RESULTS.ocid=GEO_RESULTS.ocid and TXT_RESULTS.oid=GEO_RESULTS.oid)",
                    types);

            ps.setObjects(objects);
        }

        return ps;
    }

    @Override
    public PreparableStatement getDefaultGeoSearchStmt(final String wkt, final String srid, final String classesIn) {
        // NOTE: does not narrow down matches using the bbox of the geometry like the postgis version via '&&' thus may
        // be too slow
        final PreparableStatement ps = new PreparableStatement(

                "SELECT ocid,\n"
                        + "  oid,\n"
                        + "  stringrep,\n"
                        + "  geometry,\n"
                        + "  lightweight_json\n"
                        + "FROM\n"
                        + "  (SELECT i.class_id ocid,\n"
                        + "    i.object_id oid,\n"
                        + "    c.stringrep,\n"
                        + "    c.geometry,\n"
                        + "    c.lightweight_json,\n"
                        + "    row_number() over (partition BY i.class_id, i.object_id order by rownum) rn\n"
                        + "  FROM geom g,\n"
                        + "    cs_attr_object_derived i\n"
                        + "  LEFT OUTER JOIN cs_cache c\n"
                        + "  ON ( c.class_id       =i.class_id\n"
                        + "  AND c.object_id       =i.object_id )\n"
                        + "  WHERE i.attr_class_id =\n"
                        + "    ( SELECT cs_class.id FROM cs_class WHERE cs_class.table_name = 'GEOM'\n"
                        + "    )\n"
                        + "  AND i.attr_object_id                                                                                                                                                                                                                   = g.id\n"
                        + "  AND i.class_id IN "
                        + classesIn
                        + " \n"
                        + "  AND sdo_relate(geo_field,sdo_geometry(?, "
                        + srid
                        + "), 'mask=anyinteract') = 'TRUE'\n"
                        + "  ORDER BY 1,2,3\n"
                        + "  )\n"
                        + "WHERE rn = 1",
                Types.CLOB); // NOI18N
        ps.setObjects(wkt);

        return ps;
    }

    @Override
    public String getDistinctValuesSearchStmt(final String metaClass, final String attribute) {
        return "SELECT DISTINCT "
                    + attribute
                    + " FROM "
                    + metaClass
                    + " where rownum <= 100 order by "
                    + attribute;
    }

    @Override
    public String getQueryEditorSearchStmt(final String tableName, final int classId, final String whereClause) {
        return "SELECT "
                    + classId
                    + " classid, tbl.id objectid, c.stringrep,c.geometry,c.lightweight_json FROM "
                    + tableName
                    + " tbl "
                    + "LEFT OUTER JOIN cs_cache c "              // NOI18N
                    + "       ON     ( "                         // NOI18N
                    + "                     c.class_id = "
                    + classId                                    // NOI18N
                    + "              AND    c.object_id=tbl.id " // NOI18N
                    + "              ) WHERE "
                    + whereClause;
    }

    @Override
    public String getQueryEditorSearchPaginationStmt(final String tableName,
            final int classId,
            final String whereClause,
            final int limit,
            final int offset) {
        return "SELECT * FROM (SELECT "
                    + classId
                    + " classid, tbl.id objectid, c.stringrep,c.geometry,c.lightweight_json FROM "
                    + tableName
                    + " tbl "
                    + "LEFT OUTER JOIN cs_cache c "              // NOI18N
                    + "       ON     ( "                         // NOI18N
                    + "                     c.class_id = "
                    + classId                                    // NOI18N
                    + "              AND    c.object_id=tbl.id " // NOI18N
                    + "              ) WHERE "
                    + whereClause
                    + ") sub WHERE rownum >= "
                    + offset
                    + " AND rownum <= "
                    + (offset + limit);
    }

    @Override
    public String getIndexTriggerDeleteAttrStringObjectStmt() {
        return "DELETE FROM cs_attr_string WHERE class_id = ? AND object_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerDeleteAttrObjectObjectStmt() {
        return "DELETE FROM cs_attr_object WHERE class_id = ? AND object_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerInsertAttrStringStmt() {
        return "INSERT INTO cs_attr_string (class_id, object_id, attr_id, string_val) VALUES (?, ?, ?, ?)"; // NOI18N
    }

    @Override
    public String getIndexTriggerInsertAttrObjectStmt() {
        return
            "INSERT INTO cs_attr_object (class_id, object_id, attr_class_id, attr_object_id) VALUES (?, ?, ?, ?)"; // NOI18N
    }

    @Override
    public String getIndexTriggerUpdateAttrStringStmt() {
        return "UPDATE cs_attr_string "                                       // NOI18N
                    + "SET string_val = ? "                                   // NOI18N
                    + "WHERE class_id = ? AND object_id = ? AND attr_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerUpdateAttrObjectStmt() {
        return "UPDATE cs_attr_object "                                             // NOI18N
                    + "SET attr_object_id = ? "                                     // NOI18N
                    + "WHERE class_id = ? AND object_id = ? AND attr_class_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerDeleteAttrObjectArrayStmt() {
        return "DELETE from cs_attr_object "                                        // NOI18N
                    + "WHERE class_id = ? AND object_id = ? AND attr_class_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerDeleteAttrObjectDerivedStmt() {
        return "delete from cs_attr_object_derived where class_id=? and object_id =?";
    }

    @Override
    public String getIndexTriggerInsertAttrObjectDerivedStmt() {
        return "insert into cs_attr_object_derived "
                    + "((SELECT "
                    + " CONNECT_BY_ROOT class_id xcid,"
                    + " CONNECT_BY_ROOT object_id xoid,"
                    + " attr_class_id acid,"
                    + " attr_object_id aoid"
                    + "    FROM cs_attr_object"
                    + "    START WITH class_id = ? and object_id = ?"
                    + "    CONNECT BY (PRIOR attr_class_id = class_id AND PRIOR attr_object_id = object_id AND level < 1000)"
                    + "    ORDER BY 1, 2, 3, 4)"
                    + " union"
                    + "  select ?, ?, ?, ?)";
    }

    @Override
    public String getIndexTriggerSelectClassIdForeignKeyStmt(final int classId) {
        return "SELECT class_id FROM cs_attr WHERE foreign_key_references_to = "
                    + classId;
    }

    @Override
    public String getIndexTriggerSelectFieldStmt(final int classId, final int refClassId) {
        return "select field_name from cs_attr where class_id = "
                    + classId
                    + " and foreign_key_references_to = "
                    + refClassId;
    }

    @Override
    public String getIndexTriggerSelectObjFieldStmt(final String fieldName, final String tableName, final int classId) {
        return "select "
                    + fieldName
                    + " from "
                    + tableName
                    + " where id = "
                    + classId;
    }

    @Override
    public String getIndexTriggerSelectBackReferenceStmt(final int classId, final int fk) {
        return "SELECT field_name FROM cs_attr WHERE class_id = "
                    + classId
                    + " AND foreign_key_references_to = "
                    + fk
                    + " AND rownum = 1";
    }

    @Override
    public String getIndexTriggerSelectTableNameByClassIdStmt(final int classId) {
        return "SELECT table_name FROM cs_class where id = "
                    + classId;
    }

    @Override
    public String getIndexTriggerSelectFKIdStmt(final String tableName, final String backref, final int classId) {
        return "SELECT id as id FROM "
                    + tableName
                    + " WHERE "
                    + backref
                    + " =  "
                    + classId;
    }

    @Override
    public String getIndexTriggerSelectReindexPureStmt(final int classId, final int objectId) {
        return "call reindexpure_obj("
                    + classId
                    + ","
                    + objectId
                    + ")";
    }

    @Override
    public String getObjectFactoryGetObjectStmt(final String tableName,
            final String fieldname,
            final String referenceKey,
            final String orderByField) {
        return "Select * from "
                    + tableName
                    + " where " // NOI18N
                    + fieldname
                    + " = "     // NOI18N
                    + referenceKey
                    + " order by "
                    + orderByField;
    }

    @Override
    public String getObjectFactoryAttrPermStmt(final int classId, final String implodedUserGroupIds) {
        return "select p.id pid,p.key key, u.ug_id ug_id, u.attr_id attr_id"
                    + " from cs_ug_attr_perm u, cs_permission p  where attr_id in "
                    + "(select id  from cs_attr where class_id ="
                    + classId
                    + ") and u.permission = p.id and nvl(ug_id, '-1') IN ("
                    + implodedUserGroupIds
                    + ")";
    }

    @Override
    public String getDialect() {
        return "oracle_11g";
    }
}
