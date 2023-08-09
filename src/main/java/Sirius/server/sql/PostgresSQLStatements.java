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
 * @version  0.1
 */
@ServiceProvider(service = ServerSQLStatements.class)
public final class PostgresSQLStatements implements ServerSQLStatements {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getVirtualTreeAddNodeStatement(final int nodeId,
            final String nodeName,
            final String classKey,
            final int objectId,
            final char nodeType,
            final boolean root,
            final String policy) {
        return
            "insert into cs_cat_node (id, name, descr, class_key, object_id, node_type, is_root, org,dynamic_children,sql_sort,policy) values ( " // NOI18N
                    + nodeId
                    + ",'"                                                                                                                       // NOI18N
                    + nodeName
                    + "',1,"                                                                                                                     // NOI18N
                    + "'"+classKey+"'"
                    + ","                                                                                                                        // NOI18N
                    + objectId
                    + ",'"                                                                                                                       // NOI18N
                    + nodeType
                    + "','"                                                                                                                      // NOI18N
                    + (root ? "T" : "F")
                    + "', NULL,NULL,false,"                                                                                                      // NOI18N
                    + policy
                    + " )";
    }

    @Override
    public String getVirtualTreeNextNodeIdStatement() {
        return "SELECT NEXTVAL('cs_cat_node_sequence')";
    }

    @Override
    public String getVirtualTreeClassTreeNodesStatement(final String implodedUserGroupIds) {
        return "select "                                                                                                                                                                                        // NOI18N
                    + "y.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " // NOI18N
                    + "("                                                                                                                                                                                       // NOI18N
                    + "select "                                                                                                                                                                                 // NOI18N
                    + "n.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  "                // NOI18N
                    + "from "                                                                                                                                                                                   // NOI18N
                    + "cs_cat_node n left outer join url  on ( n.descr=url.id ) "                                                                                                                               // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                              // NOI18N
                    + "where "                                                                                                                                                                                  // NOI18N
                    + "is_root=true and node_type='C' "                                                                                                                                                         // NOI18N
                    + ") y "                                                                                                                                                                                    // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and ug_id IN ("                                                                                                              // NOI18N
                    + implodedUserGroupIds
                    + ") left outer join cs_permission pp on p.permission=pp.id ";
    }

    @Override
    public String getVirtualTreeTopNodesStatement(final boolean artificialIdSupported,
            final String implodedUserGroupIds) {
        return "select "                                                                                                                                                                                 // NOI18N
                    + "y.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class" // NOI18N
                    + ((artificialIdSupported) ? ",artificial_id" : "")
                    + " from ("                                                                                                                                                                          // NOI18N
                    + "select "                                                                                                                                                                          // NOI18N
                    + "n.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  "         // NOI18N
                    + ((artificialIdSupported) ? ",artificial_id" : "")
                    + " from "                                                                                                                                                                           // NOI18N
                    + "cs_cat_node n left outer join url  on ( n.descr=url.id ) "                                                                                                                        // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                       // NOI18N
                    + "where "                                                                                                                                                                           // NOI18N
                    + "is_root=true and node_type<>'C' "                                                                                                                                                 // NOI18N
                    + ") y "                                                                                                                                                                             // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and ug_id IN ("
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
                    + "class_key, "                                                                           // NOI18N
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
                        + "class_key, "                                                                       // NOI18N
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
                + "LEFT OUTER JOIN cs_permission pp ON (p.permission = pp.id AND ug_id IN (" + implodedUserGroupIds + ")) " // NOI18N
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
                    + "domain_to \"domain\" "                                                       // NOI18N
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
    public String getVirtualTreeHasNodesStmt(final String classKey, final String objId) {
        return "select count(id) from cs_cat_node where object_id = " // NOI18N
                    + objId
                    + " and class_key = "                              // NOI18N
                    + classKey;
    }

    @Override
    public String getVirtualTreeGetNodeStmt(final int nodeId, final String implodedUserGroupIds) {
        return
            "select  y.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort, url , p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  "                                                                 // NOI18N
                    + "from"                                                                                                                                                                                                                                            // NOI18N
                    + " (select n.id id,name,class_key,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  from cs_cat_node n left outer join url  on ( n.descr=url.id ) " // NOI18N
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
    public String getPolicyHolderServerPoliciesStmt() {
        return "SELECT "                                             // NOI18N
                    + "cs_policy.id as policyid, "                   // NOI18N
                    + "cs_policy.name,"                              // NOI18N
                    + "cs_permission.id as permissionid,"            // NOI18N
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
    public String getClassGetEmptyResultStmnt(final String tableName) {
        return String.format("SELECT * FROM %s WHERE false", tableName);
    }

    @Override
    public String getPersistenceHelperNextvalStmt(final String tableName) {
        return "SELECT NEXTVAL('"
                    + tableName
                    + "_SEQ')"; // NOI18N
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
    public String getPasswordSwitcherAdminActionSelectUserStmt(final String loginName) {
        return "SELECT * FROM cs_usr WHERE login_name = '"
                    + loginName
                    + "'";
    }

    @Override
    public String getPasswordSwitcherAdminActionChangeAndBackupStmt(final String loginNameToSwitch,
            final String loginNameToRead) {
        return "UPDATE cs_usr SET last_salt = salt, last_pw_hash=pw_hash, salt = "
                    + "(SELECT salt FROM cs_usr WHERE login_name = '"
                    + loginNameToRead
                    + "') , pw_hash = "
                    + "(SELECT pw_hash FROM cs_usr WHERE login_name = '"
                    + loginNameToRead
                    + "') "
                    + "WHERE login_name = '"
                    + loginNameToSwitch
                    + "'";
    }

    @Override
    public String getPasswordSwitcherAdminActionRecoveryStmt(final String loginName) {
        return
            "UPDATE cs_usr SET salt=last_salt, pw_hash=last_pw_hash, last_salt = NULL, last_pw_hash = NULL WHERE login_name = '"
                    + loginName
                    + "' and last_salt is not null and last_pw_hash is not null";
    }

    @Override
    public PreparableStatement getDefaultFullTextSearchStmt(final String searchText,
            final String classesIn,
            final PreparableStatement geoSql,
            final boolean caseSensitive) {
        final String sql = "SELECT DISTINCT "
                    + "  cs_class.id, "
                    + "  cs_attr_object_derived.object_id, "
                    + "  cs_cache.stringrep, "
                    + "  cs_cache.geometry, "
                    + "  cs_cache.lightweight_json "
                    + "FROM "
                    + "  cs_class, "
                    + "  cs_attr_string, "
                    + "  cs_attr_object_derived "
                    + "  LEFT OUTER JOIN cs_cache ON ( "
                    + "    cs_cache.class_key = cs_attr_object_derived.class_key "
                    + "    AND cs_cache.object_id = cs_attr_object_derived.object_id "
                    + "  ) "
                    + "WHERE "
                    + "  cs_class.table_name = cs_attr_string.class_key "
                    + "  cs_attr_object_derived.attr_class_key = cs_attr_string.class_key "
                    + "  AND cs_attr_object_derived.attr_object_id = cs_attr_string.object_id "
                    + "  AND cs_attr_string.string_val "
                    + (caseSensitive ? "like" : "ilike")
                    + " ? "
                    + "  AND cs_class.id IN "
                    + classesIn;

        Object[] objs = new Object[1];

        final PreparableStatement ps;
        if (geoSql == null) {
            ps = new PreparableStatement(sql, new int[] { Types.VARCHAR });
        } else {
            final int parameterCount = geoSql.getObjects().length;
            final int[] types = new int[parameterCount
                            + 1];

            objs = new Object[parameterCount
                            + 1];
            System.arraycopy(geoSql.getObjects(), 0, objs, 1, parameterCount);
            types[0] = Types.VARCHAR;
            System.arraycopy(geoSql.getTypes(), 0, types, 1, parameterCount);

            ps = new PreparableStatement(
                    "SELECT TXT_RESULTS.* FROM (\n"
                            + sql
                            + "\n) as TXT_RESULTS \n"
                            + "JOIN ("
                            + geoSql.getStatement()
                            + ") AS GEO_RESULTS \n"
                            + "ON (TXT_RESULTS.ocid=GEO_RESULTS.ocid and TXT_RESULTS.oid=GEO_RESULTS.oid)",
                    types);
        }

        objs[0] = "%"
                    + searchText
                    + "%";
        ps.setObjects(objs);

        return ps;
    }

    @Override
    public PreparableStatement getDefaultGeoSearchStmt(final String wkt, final String srid, final String classesIn) {
        final String geometryText = "SRID="
                    + srid
                    + ";"
                    + wkt;
        final PreparableStatement ps = new PreparableStatement(
                "SELECT DISTINCT "
                        + "  cs_class.id, "
                        + "  cs_attr_object_derived.object_id, "
                        + "  cs_cache.stringrep, "
                        + "  cs_cache.geometry, "
                        + "  cs_cache.lightweight_json "
                        + "FROM "
                        + "  geom, "
                        + "  cs_class, "
                        + "  cs_attr_object_derived "
                        + "  LEFT OUTER JOIN cs_cache ON ("
                        + "    cs_cache.class_key = cs_attr_object_derived.class_key "
                        + "    AND cs_cache.object_id = cs_attr_object_derived.object_id"
                        + "  ) "
                        + "WHERE "
                        + "  cs_class.table_name = cs_attr_object_derived.class_key"
                        + "  cs_attr_object_derived.attr_class_key::text ILIKE 'GEOM'::text "
                        + "  AND cs_attr_object_derived.attr_object_id = geom.id "
                        + "  AND cs_class.id IN "
                        + classesIn
                        + ""
                        + "  AND geom.geo_field && st_GeometryFromText(?) "
                        + "  AND st_intersects(geom.geo_field, st_GeometryFromText(?)) "
                        + "ORDER BY 1, 2, 3",
                new int[] { Types.VARCHAR, Types.VARCHAR });
        ps.setObjects(geometryText, geometryText);
        return ps;
    }

    @Override
    public String getDistinctValuesSearchStmt(final String metaClass, final String attribute) {
        return "SELECT DISTINCT "
                    + attribute
                    + " FROM "
                    + metaClass
                    + " order by "
                    + attribute
                    + " LIMIT 100";
    }

    @Override
    public String getQueryEditorSearchStmt(final String tableName, final int classId, final String whereClause) {
        return "SELECT "
                    + "  "
                    + classId
                    + " AS classid, "
                    + "  tbl.id AS objectid, "
                    + "  cs_cache.stringrep, "
                    + "  cs_cache.geometry, "
                    + "  cs_cache.lightweight_json "
                    + "FROM "
                    + "  "
                    + tableName
                    + " tbl, "
                    + "  cs_class"
                    + "  LEFT OUTER JOIN cs_cache ON ( "
                    + "    cs_cache.class_key = cs_class.table_name "
                    + "    AND cs_cache.object_id = tbl.id "
                    + ")"
                    + "WHERE "
                    + "  cs_class.id = "
                    + classId
                    + " "
                    + "  AND"
                    + whereClause;
    }

    @Override
    public String getQueryEditorSearchPaginationStmt(final String tableName,
            final int classId,
            final String whereClause,
            final int limit,
            final int offset) {
        return "SELECT * FROM ("
                    + "  SELECT "
                    + "    "
                    + classId
                    + ", "
                    + "    tbl.id, "
                    + "    cs_cache.stringrep, "
                    + "    cs_cache.geometry, "
                    + "    cs_cache.lightweight_json "
                    + "  FROM "
                    + "    "
                    + tableName
                    + " tbl, "
                    + "    cs_class"
                    + "    LEFT OUTER JOIN cs_cache ON (  "
                    + "      cs_cache.class_key = cs_class.table_name "
                    + "      AND cs_cache.object_id = tbl.id "
                    + "    ) "
                    + "  WHERE "
                    + "    cs_class.id = "
                    + classId
                    + " "
                    + "    AND "
                    + whereClause
                    + ") sub "
                    + "LIMIT "
                    + limit
                    + " "
                    + "OFFSET "
                    + offset;
    }

    @Override
    public String getIndexTriggerDeleteAttrStringObjectStmt() {
        return "DELETE FROM cs_attr_string "
                    + "WHERE "
                    + "  class_key = ? "
                    + "  AND object_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerDeleteAttrObjectObjectStmt() {
        return "DELETE FROM cs_attr_object "
                    + "WHERE "
                    + "  class_key = ? "
                    + "  AND object_id = ?"; // NOI18N
    }

    @Override
    public String getIndexTriggerInsertAttrStringStmt() {
        return "INSERT INTO cs_attr_string (class_key, object_id, attr_id, string_val) VALUES (?, ?, ?, ?)"; // NOI18N
    }

    @Override
    public String getIndexTriggerInsertAttrObjectStmt() {
        return
            "INSERT INTO cs_attr_object (class_key, object_id, attr_class_key, attr_object_id) VALUES (? , ?, ?, ?)"; // NOI18N
    }

    @Override
    public String getIndexTriggerDeleteAttrObjectDerivedStmt() {
        return "DELETE FROM cs_attr_object_derived "
                    + "WHERE "
                    + "  class_key = ? "
                    + "  AND object_id = ?";
    }

    @Override
    public String getIndexTriggerInsertAttrObjectDerivedStmt() {
        // instead of the union, the last condition (NOT (xockey = ackey AND xoid = aid)) can be removed,
        // to get the same result. But the postgres statement must have the same arguments as the oracle statement
        return "INSERT INTO cs_attr_object_derived ("
                    + "  ("
                    + "    WITH recursive derived_index(xockey, xoid, ockey, oid, ackey, aid, depth) AS ( "
                    + "      SELECT "
                    + "        cs_attr_object.class_key, "
                    + "        cs_attr_object.object_id, "
                    + "        cs_attr_object.class_key, "
                    + "        cs_attr_object.object_id, "
                    + "        cs_attr_object.class_key, "
                    + "        cs_attr_object.object_id, "
                    + "        0 "
                    + "      FROM "
                    + "        cs_attr_object "
                    + "      WHERE "
                    + "        cs_attr_object.class_key = ? "
                    + "        AND cs_attr_object.object_id = ? "
                    + "      UNION ALL "
                    + "      SELECT "
                    + "        derived_index.xockey, "
                    + "        derived_index.xoid, "
                    + "        cs_attr_object.class_key, "
                    + "        cs_attr_object.object_id, "
                    + "        cs_attr_object.class_key, "
                    + "        cs_attr_object.attr_object_id, "
                    + "        derived_index.depth + 1 "
                    + "      FROM"
                    + "        cs_attr_object, "
                    + "        derived_index "
                    + "      WHERE"
                    + "        cs_attr_object.class_key = derived_index.ackey "
                    + "        AND cs_attr_object.object_id = derived_index.aid "
                    + "    ) "
                    + "    SELECT DISTINCT "
                    + "      xockey, "
                    + "      xoid, "
                    + "      ackey, "
                    + "      aid "
                    + "    FROM  "
                    + "      derived_index "
                    + "    WHERE NOT (xockey = ackey AND xoid = aid)"
                    + "    ORDER BY 1, 2, 3, 4 LIMIT 1000000000"
                    + "  )"
                    + "  UNION "
                    + "  SELECT ?, ?, ?, ?"
                    + ")";
    }

    @Override
    public String getIndexTriggerSelectClassIdForeignKeyStmt(final String classKey, final boolean oneToMany) {
        return "SELECT "
                    + "  cs_class.table_name "
                    + "FROM "
                    + "  cs_attr, "
                    + "  cs_class, "
                    + "  cs_class AS foreign_class "
                    + "WHERE "
                    + "  cs_class.id = cs_attr.class_id "
                    + "  AND foreign_class.id = "
                    + (oneToMany ? "cs_attr.foreign_key_references_to * -1" : "cs_attr.foreign_key_references_to")
                    + " "
                    + "  AND foreign_class.table_name = '"
                    + classKey
                    + "'";
    }

    @Override
    public String getIndexTriggerSelectFieldStmt(final String classKey, final String refClassKey) {
        return "SELECT "
                    + "  field_name "
                    + "FROM cs_attr "
                    + "WHERE "
                    + "  class_id = (SELECT id FROM cs_class WHERE table_name = '"
                    + classKey
                    + "') "
                    + "  AND foreign_key_references_to = (SELECT id FROM cs_class WHERE table_name = '"
                    + refClassKey
                    + "')";
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
                    + " LIMIT 1";
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
    public String getIndexTriggerSelectReindexPureStmt(final String classKey, final int objectId) {
        return "select reindexpure("
                    + classKey
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
        return "select p.id as pid,p.key as key, u.ug_id as ug_id, u.attr_id as attr_id"
                    + " from cs_ug_attr_perm as u, cs_permission as p  where attr_id in "
                    + "(select id  from cs_attr where class_id ="
                    + classId
                    + ") and u.permission = p.id and ug_id IN ("
                    + implodedUserGroupIds
                    + ")";
    }

    @Override
    public String getDialect() {
        return "postgres_9";
    }
}
