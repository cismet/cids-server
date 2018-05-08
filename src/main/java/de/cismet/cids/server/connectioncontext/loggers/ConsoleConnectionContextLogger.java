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

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cids.server.connectioncontext.ConnectionContextLog;
import de.cismet.cids.server.connectioncontext.ConnectionContextLogger;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ConnectionContextLogger.class)
public class ConsoleConnectionContextLogger extends AbstractConnectionContextLogger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConsoleConnectionContextLogger.class);
    public static final String NAME = "console";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void log(final ConnectionContextLog connectionContextLog) {
        if (isAnyFilterRuleSetSatisfied(connectionContextLog)) {
            System.out.println(connectionContextLog);
        }
    }
}
