/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.tree;

import org.openide.util.lookup.ServiceProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  0.1
 */
@ServiceProvider(service = VirtualTreeStatements.class)
public final class OracleTreeStatements implements VirtualTreeStatements {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getAddNodeStatement(final int nodeId,
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
    public String getNextNodeIdStatement() {
        return "SELECT cs_cat_node_sequence.nextval FROM DUAL";
    }

    @Override
    public String getClassTreeNodesStatement(final String implodedUserGroupIds) {
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
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and ug_id IN ("                                                                                                              // NOI18N
                    + implodedUserGroupIds
                    + ") left outer join cs_permission pp on p.permission=pp.id ";
    }

    @Override
    public String getTopNodesStatement(final String implodedUserGroupIds) {
        return "select "                                                                                                                                                                                        // NOI18N
                    + "y.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort, url ,  p.permission perm_id,p.ug_id,pp.key perm_key,y.policy,iconfactory,icon,derive_permissions_from_class  from " // NOI18N
                    + "("                                                                                                                                                                                       // NOI18N
                    + "select "                                                                                                                                                                                 // NOI18N
                    + "n.id id,name,class_id,object_id,node_type,dynamic_children,sql_sort,n.policy,prot_prefix||server||path||object_name url,iconfactory,icon,derive_permissions_from_class  "                // NOI18N
                    + "from "                                                                                                                                                                                   // NOI18N
                    + "cs_cat_node n left outer join url  on ( n.descr=url.id ) "                                                                                                                               // NOI18N
                    + "left outer join url_base ub  on (url.url_base_id=ub.id)   "                                                                                                                              // NOI18N
                    + "where "                                                                                                                                                                                  // NOI18N
                    + "is_root='1' and node_type<>'C' "                                                                                                                                                         // NOI18N
                    + ") y "                                                                                                                                                                                    // NOI18N
                    + "left outer join cs_ug_cat_node_perm p on p.cat_node_id=y.id and ug_id IN ("
                    + implodedUserGroupIds
                    + ") left outer join cs_permission pp on p.permission=pp.id ";
    }

    @Override
    public String getDialect() {
        return "oracle_11g";
    }
}
