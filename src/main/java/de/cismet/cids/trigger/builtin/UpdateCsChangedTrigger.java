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
public class UpdateCsChangedTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            UpdateCsChangedTrigger.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private enum MonitoringLevel {

        //~ Enum constants -----------------------------------------------------

        OFF, CLASS, OBJECT
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        final MonitoringLevel level = getMonitoringLevel(cidsBean);

        if (level.equals(MonitoringLevel.CLASS)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        int count = getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.UPDATE_CS_CHANGED_CLASS_ENTRY,
                                        cidsBean.getMetaObject().getClassID());

                        if (count < 1) {
                            count = getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                                DBConnection.INSERT_CS_CHANGED_CLASS_ENTRY,
                                                cidsBean.getMetaObject().getClassID());
                        }
                        return count;
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
        } else if (level.equals(MonitoringLevel.OBJECT)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        int count = getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.UPDATE_CS_CHANGED_OBJECT_ENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());

                        if (count < 1) {
                            count = getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                                DBConnection.INSERT_CS_CHANGED_OBJECT_ENTRY,
                                                cidsBean.getMetaObject().getClassID(),
                                                cidsBean.getMetaObject().getID());
                        }

                        return count;
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
        final MonitoringLevel level = getMonitoringLevel(cidsBean);

        if (level.equals(MonitoringLevel.CLASS)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        int count = getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.UPDATE_CS_CHANGED_CLASS_ENTRY,
                                        cidsBean.getMetaObject().getClassID());

                        if (count < 1) {
                            count = getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                                DBConnection.INSERT_CS_CHANGED_CLASS_ENTRY,
                                                cidsBean.getMetaObject().getClassID());
                        }
                        return count;
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
        } else if (level.equals(MonitoringLevel.OBJECT)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        return getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.INSERT_CS_CHANGED_OBJECT_ENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());
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
        final MonitoringLevel level = getMonitoringLevel(cidsBean);

        if (level.equals(MonitoringLevel.CLASS)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        int count = getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.UPDATE_CS_CHANGED_CLASS_ENTRY,
                                        cidsBean.getMetaObject().getClassID());

                        if (count < 1) {
                            count = getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                                DBConnection.INSERT_CS_CHANGED_CLASS_ENTRY,
                                                cidsBean.getMetaObject().getClassID());
                        }

                        return count;
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
        } else if (level.equals(MonitoringLevel.OBJECT)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        int count = getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.UPDATE_CS_CHANGED_OBJECT_ENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());

                        if (count < 1) {
                            count = getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                                DBConnection.INSERT_CS_CHANGED_OBJECT_ENTRY,
                                                cidsBean.getMetaObject().getClassID(),
                                                cidsBean.getMetaObject().getID());
                        }

                        return count;
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
    private MonitoringLevel getMonitoringLevel(final CidsBean cidsBean) {
        final ClassAttribute attr = cidsBean.getMetaObject()
                    .getMetaClass()
                    .getClassAttribute("class_changed_monitoring_level");

        if (attr != null) {
            final Object monitoringLevel = attr.getValue();

            if (monitoringLevel instanceof String) {
                if (((String)monitoringLevel).equalsIgnoreCase("class")) {
                    return MonitoringLevel.CLASS;
                } else if (((String)monitoringLevel).equalsIgnoreCase("object")) {
                    return MonitoringLevel.OBJECT;
                }
            }
        }

        return MonitoringLevel.OFF;
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
