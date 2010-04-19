/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.method;

import Sirius.server.sql.*;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.PermissionHolder;

import java.sql.*;

import Sirius.server.property.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MethodCache {

    //~ Instance fields --------------------------------------------------------

    protected MethodMap methods;
    protected java.util.Vector methodArray;
    protected ServerProperties properties;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MethodCache object.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    public MethodCache(DBConnectionPool conPool, ServerProperties properties) {
        this.properties = properties;

        methodArray = new java.util.Vector(50);

        methods = new MethodMap(50, 0.7f); // allocation of the hashtable

        DBConnection con = conPool.getConnection();
        try {
            ResultSet methodTable = con.submitQuery("get_all_methods", new Object[0]);   // NOI18N

            while (methodTable.next()) // add all objects to the hashtable
            {
                Method tmp = new Method(
                        methodTable.getInt("id"),   // NOI18N
                        methodTable.getString("plugin_id").trim(),   // NOI18N
                        methodTable.getString("method_id").trim(),   // NOI18N
                        methodTable.getBoolean("class_mult"),   // NOI18N
                        methodTable.getBoolean("mult"),   // NOI18N
                        methodTable.getString("descr"),   // NOI18N
                        null);
                methods.add(properties.getServerName(), tmp);
                methodArray.addElement(tmp);
                if (logger.isDebugEnabled()) {
                    logger.debug("Methode " + tmp + "cached");   // NOI18N
                }
            }                          // end while

            methodTable.close();
            if (logger.isDebugEnabled()) {
                // methods.rehash(); MethodMap jetzt hashmap
                logger.debug("methodmap :" + methods);   // NOI18N
            }

            addMethodPermissions(conPool);

            addClassKeys(conPool);
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: when trying to submit get_all_methods statement", e);   // NOI18N
        }
    } // end of constructor

    //~ Methods ----------------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------------------
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addMethodPermissions(DBConnectionPool conPool) {
        try {
            DBConnection con = conPool.getConnection();

            ResultSet permTable = con.submitQuery("get_all_method_permissions", new Object[0]);   // NOI18N

            String lsName = properties.getServerName();

            while (permTable.next()) {
                String methodID = permTable.getString("method_id").trim();   // NOI18N
                String pluginID = permTable.getString("plugin_id").trim();   // NOI18N
                String ugLsHome = permTable.getString("ls").trim();   // NOI18N
                int ugID = permTable.getInt("ug_id");   // NOI18N

                String mkey = methodID + "@" + pluginID;   // NOI18N

                if (methods.containsMethod(mkey)) {
                    Method tmp = methods.getMethod(mkey);

                    if ((ugLsHome == null) || ugLsHome.equalsIgnoreCase("local")) {   // NOI18N
                        ugLsHome = new String(lsName);
                    }

                    tmp.addPermission(new UserGroup(ugID, "", ugLsHome));   // NOI18N
                } else {
                    logger.error("<LS> ERROR :: theres a method permission without method methodID " + mkey);   // NOI18N
                }
            }

            permTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);

            logger.error("<LS> ERROR :: addMethodPermissions", e);   // NOI18N
        }
    }

    /**
     * ----------------------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public final MethodMap getMethods() {
        if (logger.isDebugEnabled()) {
            logger.debug("getMethods called" + methods);   // NOI18N
        }
        return methods;
    }

    /**
     * ------------------------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public final MethodMap getMethods(UserGroup ug) throws Exception {
        MethodMap view = new MethodMap(methodArray.size(), 0.7f);

        for (int i = 0; i < methodArray.size(); i++) {
            Method m = (Method)methodArray.get(i);

            if (m.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
                // view.add(properties.getServerName(),m);
                view.add((String)m.getKey(), m);
            }
        }

        return view;
    }

    // ------------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  conPool  DOCUMENT ME!
     */
    public void addClassKeys(DBConnectionPool conPool) {
        try {
            DBConnection con = conPool.getConnection();

            String sql =
                "select c.id as c_id , m.plugin_id as p_id,m.method_id as m_id  from cs_class as c, cs_method as m, cs_method_class_assoc as assoc where c.id=assoc.class_id and m.id = assoc.method_id";   // NOI18N

            ResultSet table = con.getConnection().createStatement().executeQuery(sql);

            String lsName = properties.getServerName();

            while (table.next()) {
                String methodID = table.getString("m_id").trim();   // NOI18N
                String pluginID = table.getString("p_id").trim();   // NOI18N
                int classID = table.getInt("c_id");   // NOI18N

                String key = methodID + "@" + pluginID;   // NOI18N
                if (methods.containsMethod(key)) {
                    String cKey = classID + "@" + lsName;   // NOI18N
                    methods.getMethod(key).addClassKey(cKey);
                    if (logger.isDebugEnabled()) {
                        logger.debug("add class key " + cKey + "to mehtod " + key);   // NOI18N
                    }
                } else {
                    logger.error("no method key " + key);   // NOI18N
                }
            }

            table.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);

            logger.error("<LS> ERROR :: addMethodClassKeys", e);   // NOI18N
        }
    }
}
