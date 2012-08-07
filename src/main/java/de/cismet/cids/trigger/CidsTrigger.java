/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger;

import Sirius.server.newuser.User;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface CidsTrigger extends Comparable<CidsTrigger> {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void beforeInsert(final CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterInsert(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void beforeUpdate(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterUpdate(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void beforeDelete(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterDelete(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterCommittedInsert(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterCommittedUpdate(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    void afterCommittedDelete(CidsBean cidsBean, final User user);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsTriggerKey getTriggerKey();
}
