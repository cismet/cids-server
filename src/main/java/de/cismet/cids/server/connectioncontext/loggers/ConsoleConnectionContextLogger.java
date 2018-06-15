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
    public static final String TYPE = "console";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConsoleConnectionContextLogger object.
     */
    public ConsoleConnectionContextLogger() {
    }

    /**
     * Creates a new ConsoleConnectionContextLogger object.
     *
     * @param  name  DOCUMENT ME!
     */
    public ConsoleConnectionContextLogger(final String name) {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public ConnectionContextLogger createNewLogger(final String name, final Object config) {
        return new ConsoleConnectionContextLogger(name);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void log(final ConnectionContextLog connectionContextLog) {
        if (isAnyFilterRuleSetSatisfied(connectionContextLog)) {
            System.out.println(connectionContextLog);
        }
    }
}
