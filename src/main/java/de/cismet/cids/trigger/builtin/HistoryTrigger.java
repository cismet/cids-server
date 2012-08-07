/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger.builtin;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import org.openide.util.lookup.ServiceProvider;

import java.util.Date;

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
public class HistoryTrigger extends AbstractDBAwareCidsTrigger {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        createHistory(cidsBean.getMetaObject(), user);
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
        createHistory(cidsBean.getMetaObject(), user);
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        createHistory(cidsBean.getMetaObject(), user);
    }

    @Override
    public void beforeDelete(final CidsBean cidsbean, final User user) {
        initHistory(cidsbean.getMetaObject(), user);
    }

    @Override
    public void beforeInsert(final CidsBean cidsbean, final User user) {
        // nothing to do
    }

    @Override
    public void beforeUpdate(final CidsBean cidsbean, final User user) {
        initHistory(cidsbean.getMetaObject(), user);
    }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return CidsTriggerKey.FORALL;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   t  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger t) {
        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mo    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     */
    private void createHistory(final MetaObject mo, final User user) {
        try {
            // immediately returns
            dbServer.getHistoryServer().enqueueEntry(mo, user, new Date());
        } catch (final Exception e) {
            log.error("cannot enqueue mo for history creation", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mo    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     */
    private void initHistory(final MetaObject mo, final User user) {
        try {
            dbServer.getHistoryServer().initHistory(mo, user, new Date());
        } catch (final Exception e) {
            log.error("cannot initialise history", e); // NOI18N
        }
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
