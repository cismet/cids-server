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
package de.cismet.cids.server.connectioncontext;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ConnectionContextLogger {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  config  DOCUMENT ME!
     */
    void configure(final Object config);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<ConnectionContextFilterRuleSet> getFilterRuleSets();

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContextLog  DOCUMENT ME!
     */
    void log(ConnectionContextLog connectionContextLog);
}
