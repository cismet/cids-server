/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ScheduledServerAction extends JsonableServerAction {

    public static final String SSAPK_DELAY = "SSAPK_DELAY";
    public static final String SSAPK_RULE = "SSAPK_RULE";
    public static final String SSAPK_ABORT = "SSAPK_ABORT";
    
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String createKey(final ServerActionParameter... params);

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ServerActionParameter[] getNextParams(final ServerActionParameter... params);
}
