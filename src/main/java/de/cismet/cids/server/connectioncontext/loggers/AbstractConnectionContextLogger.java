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
package de.cismet.cids.server.connectioncontext.loggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cids.server.connectioncontext.ConnectionContextFilterRuleSet;
import de.cismet.cids.server.connectioncontext.ConnectionContextLog;
import de.cismet.cids.server.connectioncontext.ConnectionContextLogger;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractConnectionContextLogger implements ConnectionContextLogger {

    //~ Instance fields --------------------------------------------------------

    private final List<ConnectionContextFilterRuleSet> filterRuleSets = new ArrayList<>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public List<ConnectionContextFilterRuleSet> getFilterRuleSets() {
        return filterRuleSets;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean isAnyFilterRuleSetSatisfied(final ConnectionContextLog connectionContextLog) {
        return !getSatisfiedFilterRuleSets(connectionContextLog, true).isEmpty();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContextLog  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Collection<ConnectionContextFilterRuleSet> getSatisfiedFilterRuleSets(
            final ConnectionContextLog connectionContextLog) {
        return AbstractConnectionContextLogger.this.getSatisfiedFilterRuleSets(connectionContextLog, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContextLog      DOCUMENT ME!
     * @param   abortOnFirstSatisfaction  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Collection<ConnectionContextFilterRuleSet> getSatisfiedFilterRuleSets(
            final ConnectionContextLog connectionContextLog,
            final boolean abortOnFirstSatisfaction) {
        final Collection<ConnectionContextFilterRuleSet> satisfiedRuleSets = new ArrayList<>();
        for (final ConnectionContextFilterRuleSet filterRuleSet : getFilterRuleSets()) {
            if (filterRuleSet.accepts(connectionContextLog)) {
                satisfiedRuleSets.add(filterRuleSet);
                if (abortOnFirstSatisfaction) {
                    break;
                }
            }
        }
        return satisfiedRuleSets;
    }
}
