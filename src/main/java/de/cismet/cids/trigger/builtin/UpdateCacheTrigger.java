/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger.builtin;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.newuser.User;
import Sirius.server.sql.DBConnection;

import org.openide.util.lookup.ServiceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.trigger.AbstractDBAwareCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsTrigger.class)
public class UpdateCacheTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            UpdateCacheTrigger.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
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
        if (isCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<ResultSet, Void>() {

                    @Override
                    protected ResultSet doInBackground() throws Exception {
                        return getDbServer().getActiveDBConnection()
                                    .submitInternalQuery(
                                        DBConnection.DESC_INSERT_CACHEENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());
                    }

                    @Override
                    protected void done() {
                        try {
                            final ResultSet result = get();
                        } catch (Exception e) {
                            log.error("Exception in Background Thread: afterInsert", e);
                        }
                    }
                });
        }
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<ResultSet, Void>() {

                    @Override
                    protected ResultSet doInBackground() throws Exception {
                        try {
                            return getDbServer().getActiveDBConnection()
                                        .submitInternalQuery(
                                            DBConnection.DESC_UPDATE_CACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID());
                        } catch (SQLException e) {
                            getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.DESC_DELETE_CACHEENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());
                            return getDbServer().getActiveDBConnection()
                                        .submitInternalQuery(
                                            DBConnection.DESC_INSERT_CACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID());
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            final ResultSet result = get();
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
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger o) {
        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean isCacheEnabled(final CidsBean cidsBean) {
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.CACHE_ENABLED)
                        != null);
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean,
            final User user) {
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean,
            final User user) {
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean,
            final User user) {
    }
}
