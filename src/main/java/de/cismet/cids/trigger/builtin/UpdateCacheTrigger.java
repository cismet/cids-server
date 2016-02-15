/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ***************************************************
 */
package de.cismet.cids.trigger.builtin;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.newuser.User;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import org.openide.util.lookup.ServiceProvider;

import java.sql.SQLException;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.trigger.AbstractDBAwareCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;
import org.openide.util.Lookup;

/**
 * DOCUMENT ME!
 *
 * @author thorsten
 * @version $Revision$, $Date$
 */
@ServiceProvider(service = CidsTrigger.class)
public class UpdateCacheTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------
    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateCacheTrigger.class);

    //~ Methods ----------------------------------------------------------------
    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        if (isAnyCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                @Override
                protected Integer doInBackground() throws Exception {
                    return getDbServer().getActiveDBConnection()
                            .submitInternalUpdate(
                                    DBConnection.DESC_DELETE_CACHEENTRY,
                                    cidsBean.getMetaObject().getClassID(),
                                    cidsBean.getMetaObject().getID());
                }

                @Override
                protected void done() {
                    try {
                        final Integer result = get();
                    } catch (Exception e) {
                        log.error("Exception in Background Thread: afterDelete", e);
                    }
                }
            });
        }
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
        if (isAnyCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                @Override
                protected Integer doInBackground() throws Exception {
                    return getDbServer().getActiveDBConnection()
                            .submitInternalUpdate(
                                    DBConnection.DESC_INSERT_CACHEENTRY,
                                    cidsBean.getMetaObject().getClassID(),
                                    cidsBean.getMetaObject().getID(),
                                    getToStringCacheValue(cidsBean),
                                    getGeometryDBObjectCacheValue(cidsBean, getDbServer().getActiveDBConnection()),
                                    getLightweightJsonCacheValue(cidsBean));

                }

                @Override
                protected void done() {
                    try {
                        final Integer result = get();
                    } catch (Exception e) {
                        log.error("Exception in Background Thread: afterInsert", e);
                    }
                }
            });
        }
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        if (isAnyCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                @Override
                protected Integer doInBackground() throws Exception {
                    try {

                        final String name = cidsBean.toString();
                        if ((name == null) || name.equals("")) {
                            getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                            DBConnection.DESC_DELETE_CACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID());
                            return 0;
                        } else {
                            return getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                            DBConnection.DESC_UPDATE_CACHEENTRY,
                                            getToStringCacheValue(cidsBean),
                                            getGeometryDBObjectCacheValue(cidsBean, getDbServer().getActiveDBConnection()), //Geometry
                                            getLightweightJsonCacheValue(cidsBean),//lightweight JSON
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID());
                        }
                    } catch (SQLException e) {

                        getDbServer().getActiveDBConnection()
                                .submitInternalUpdate(
                                        DBConnection.DESC_DELETE_CACHEENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());
                        return getDbServer().getActiveDBConnection()
                                .submitInternalUpdate(
                                        DBConnection.DESC_INSERT_CACHEENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID(),
                                        getToStringCacheValue(cidsBean),
                                        getGeometryDBObjectCacheValue(cidsBean, getDbServer().getActiveDBConnection()), //Geometry
                                        getLightweightJsonCacheValue(cidsBean));//lightweight JSON
                    }
                }

                @Override
                protected void done() {
                    try {
                        final Integer result = get();
                    } catch (Exception e) {
                        log.error("Exception in Background Thread: afterUpdate", e);
                    }
                }
            });
        }
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return CidsTriggerKey.FORALL;
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger o) {
        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cidsBean DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean isCacheEnabled(final CidsBean cidsBean) {
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.CACHE_ENABLED)
                != null);
    }

    private static boolean isToStringCacheEnabled(final CidsBean cidsBean) {
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.TO_STRING_CACHE_ENABLED)
                != null);
    }

    private static boolean isGeometryCacheEnabled(final CidsBean cidsBean) {
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.GEOMETRY_CACHE_ENABLED)
                != null);
    }

    private static boolean isLightweightJsonCacheEnabled(final CidsBean cidsBean) {
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.LIGHTWEIGHT_JSON_CACHE_ENABLED)
                != null);
    }

    private static boolean isAnyCacheEnabled(final CidsBean cidsBean) {
        return isCacheEnabled(cidsBean) || isToStringCacheEnabled(cidsBean) || isGeometryCacheEnabled(cidsBean) || isLightweightJsonCacheEnabled(cidsBean);
    }

    private static String getToStringCacheValue(final CidsBean cidsBean) {
        if (isToStringCacheEnabled(cidsBean)) {
            final String name = cidsBean.toString();
            if ((name != null) && !name.equals("")) {
                return name;
            }
        }
        return null;
    }

    private static Object getGeometryDBObjectCacheValue(final CidsBean cidsBean, DBConnection con) {
        if (isGeometryCacheEnabled(cidsBean)) {
            try {
                return SQLTools.getGeometryFactory(Lookup.getDefault().lookup(DialectProvider.class).getDialect()).getDbObject(cidsBean.getGeometry(), con.getConnection());
            } catch (SQLException e) {
                log.error("Exception in Background Thread: getGeometryDBObjectCacheValue", e);
            }
        }
        return null;
    }
    private static String getLightweightJsonCacheValue(final CidsBean cidsBean) {
        if (isLightweightJsonCacheEnabled(cidsBean)) {
            return cidsBean.getLightweightJsonRepresentation();
        }
        return null;
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean,
            final User user
    ) {
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean,
            final User user
    ) {
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean,
            final User user
    ) {
    }
}
