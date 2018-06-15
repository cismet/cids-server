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

import java.util.Map;

import de.cismet.cids.server.connectioncontext.ConnectionContextFilterRuleSet;
import de.cismet.cids.server.connectioncontext.ConnectionContextLog;
import de.cismet.cids.server.connectioncontext.ConnectionContextLogger;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ConnectionContextLogger.class)
public class Log4JConnectionContextLogger extends AbstractConnectionContextLogger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(Log4JConnectionContextLogger.class);
    public static final String TYPE = "LOG4J";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Log4JConnectionContextLogger object.
     */
    public Log4JConnectionContextLogger() {
    }

    /**
     * Creates a new Log4JConnectionContextLogger object.
     *
     * @param  name  DOCUMENT ME!
     */
    public Log4JConnectionContextLogger(final String name) {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public ConnectionContextLogger createNewLogger(final String name, final Object config) {
        return new Log4JConnectionContextLogger(name);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void log(final ConnectionContextLog connectionContextLog) {
        for (final ConnectionContextFilterRuleSet filterRuleSet : getSatisfiedFilterRuleSets(connectionContextLog)) {
            final Map<String, Object> params = filterRuleSet.getLoggerParams();

            final Exception ex = connectionContextLog.getStacktraceException();
            if (ex != null) {
                LOG.info(connectionContextLog, ex);
            } else {
                LOG.info(connectionContextLog);
            }
        }
    }
}
