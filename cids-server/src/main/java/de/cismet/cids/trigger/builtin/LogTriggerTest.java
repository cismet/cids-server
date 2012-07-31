/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger.builtin;

import Sirius.server.newuser.User;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.trigger.AbstractCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
//@ServiceProvider(service = CidsTrigger.class)
public class LogTriggerTest extends AbstractCidsTrigger {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        log.fatal("afterDelete " + cidsBean.getMOString());
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
        log.fatal("afterInsert " + cidsBean.getMOString());
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
        log.fatal("afterUpdate " + cidsBean.getMOString());
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
        log.fatal("beforeDelete " + cidsBean.getMOString());
    }

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
        log.fatal("beforeInsert " + cidsBean.getMOString());
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
        log.fatal("beforeUpdate " + cidsBean.getMOString());
    }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return new CidsTriggerKey("wunda_blau", "thema_person");
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
        return 0;
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
