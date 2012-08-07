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
public class UpdateToStringCacheTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            UpdateToStringCacheTrigger.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        return getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.DESC_DELETE_STRINGREPCACHEENTRY,
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
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        final String name = cidsBean.toString();

                        if ((name != null) && !name.equals("")) {
                            return getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                            DBConnection.DESC_INSERT_STRINGREPCACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID(),
                                            name);
                        } else {
                            return 0;
                        }
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

            final String sql = "insert into cs_stringrepcache (class_id,object_id,stringrep) values("
                        + cidsBean.getMetaObject().getClassID() + "," + cidsBean
                        .getMetaObject().getID() + ",'" + cidsBean.toString() + "');";
        }
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        if (isCacheEnabled(cidsBean)) {
            de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        try {
                            final String name = cidsBean.toString();
                            if ((name == null) || name.equals("")) {
                                getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                            DBConnection.DESC_DELETE_STRINGREPCACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID());
                                return 0;
                            } else {
                                return getDbServer().getActiveDBConnection()
                                            .submitInternalUpdate(
                                                DBConnection.DESC_UPDATE_STRINGREPCACHEENTRY,
                                                name,
                                                cidsBean.getMetaObject().getClassID(),
                                                cidsBean.getMetaObject().getID());
                            }
                        } catch (SQLException e) {
                            getDbServer().getActiveDBConnection()
                                    .submitInternalUpdate(
                                        DBConnection.DESC_DELETE_STRINGREPCACHEENTRY,
                                        cidsBean.getMetaObject().getClassID(),
                                        cidsBean.getMetaObject().getID());
                            return getDbServer().getActiveDBConnection()
                                        .submitInternalUpdate(
                                            DBConnection.DESC_INSERT_STRINGREPCACHEENTRY,
                                            cidsBean.getMetaObject().getClassID(),
                                            cidsBean.getMetaObject().getID(),
                                            cidsBean.toString());
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
        return (cidsBean.getMetaObject().getMetaClass().getClassAttribute(ClassAttribute.TO_STRING_CACHE_ENABLED)
                        != null);
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean, final User user) {
    }
}
