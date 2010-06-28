/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.method;

import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.Shutdownable;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.ExceptionHandler;

import org.apache.log4j.Logger;

import java.sql.ResultSet;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class MethodCache extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2253839379071352034L;

    private static final transient Logger LOG = Logger.getLogger(MethodCache.class);

    //~ Instance fields --------------------------------------------------------

    private final transient MethodMap methods;
    private final transient List<Method> methodArray;
    private final transient ServerProperties properties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MethodCache object.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    public MethodCache(final DBConnectionPool conPool, final ServerProperties properties) {
        this.properties = properties;

        methodArray = new java.util.Vector(50);

        methods = new MethodMap(50, 0.7f); // allocation of the hashtable

        final DBConnection con = conPool.getConnection();
        try {
            final ResultSet methodTable = con.submitQuery("get_all_methods", new Object[0]);

            while (methodTable.next()) // add all objects to the hashtable
            {
                final Method tmp = new Method(
                        methodTable.getInt("id"),
                        methodTable.getString("plugin_id").trim(),
                        methodTable.getString("method_id").trim(),
                        methodTable.getBoolean("class_mult"),
                        methodTable.getBoolean("mult"),
                        methodTable.getString("descr"),
                        null);
                methods.add(properties.getServerName(), tmp);
                methodArray.add(tmp);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Methode " + tmp + "gecacht");
                }
            }                          // end while

            methodTable.close();
            if (LOG.isDebugEnabled()) {
                // methods.rehash(); MethodMap jetzt hashmap
                LOG.debug("methodmap :" + methods);
            }

            addMethodPermissions(conPool);

            addClassKeys(conPool);

            addShutdown(new Shutdownable() {

                    @Override
                    public void shutdown() throws ServerExitError {
                        methods.clear();
                        methodArray.clear();
                    }
                });
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: when trying to submit get_all_methods statement", e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ----------------------------------------------------------------------------------------
     *
     * @param  conPool  DOCUMENT ME!
     */
    private void addMethodPermissions(final DBConnectionPool conPool) {
        try {
            final DBConnection con = conPool.getConnection();

            final ResultSet permTable = con.submitQuery("get_all_method_permissions", new Object[0]);

            final String lsName = properties.getServerName();

            while (permTable.next()) {
                final String methodID = permTable.getString("method_id").trim();
                final String pluginID = permTable.getString("plugin_id").trim();
                String ugLsHome = permTable.getString("ls").trim();
                final int ugID = permTable.getInt("ug_id");

                final String mkey = methodID + "@" + pluginID;

                if (methods.containsMethod(mkey)) {
                    final Method tmp = methods.getMethod(mkey);

                    if ((ugLsHome == null) || ugLsHome.equalsIgnoreCase("local")) {
                        ugLsHome = new String(lsName);
                    }

                    tmp.addPermission(new UserGroup(ugID, "", ugLsHome));
                } else {
                    LOG.error("<LS> ERROR :: theres a method permission without method methodID " + mkey);
                }
            }

            permTable.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);

            LOG.error("<LS> ERROR :: addMethodPermissions", e);
        }
    }

    /**
     * ----------------------------------------------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public MethodMap getMethods() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMethods gerufen" + methods);
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
    public MethodMap getMethods(final UserGroup ug) throws Exception {
        final MethodMap view = new MethodMap(methodArray.size(), 0.7f);

        for (int i = 0; i < methodArray.size(); i++) {
            final Method m = (Method)methodArray.get(i);

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
    public void addClassKeys(final DBConnectionPool conPool) {
        try {
            final DBConnection con = conPool.getConnection();

            final String sql =
                "select c.id as c_id , m.plugin_id as p_id,m.method_id as m_id  from cs_class as c, cs_method as m, cs_method_class_assoc as assoc where c.id=assoc.class_id and m.id = assoc.method_id";

            final ResultSet table = con.getConnection().createStatement().executeQuery(sql);

            final String lsName = properties.getServerName();

            while (table.next()) {
                final String methodID = table.getString("m_id").trim();
                final String pluginID = table.getString("p_id").trim();
                final int classID = table.getInt("c_id");

                final String key = methodID + "@" + pluginID;
                if (methods.containsMethod(key)) {
                    final String cKey = classID + "@" + lsName;
                    methods.getMethod(key).addClassKey(cKey);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("add class key " + cKey + "to mehtod " + key);
                    }
                } else {
                    LOG.error("no method key " + key);
                }
            }

            table.close();
        } catch (java.lang.Exception e) {
            ExceptionHandler.handle(e);

            LOG.error("<LS> ERROR :: addMethodClassKeys", e);
        }
    }
}
