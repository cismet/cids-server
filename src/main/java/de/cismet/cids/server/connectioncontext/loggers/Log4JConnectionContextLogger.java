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

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

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
    public static final String NAME = "LOG4J";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void log(final ConnectionContextLog connectionContextLog) {
        for (final ConnectionContextFilterRuleSet filterRuleSet : getSatisfiedFilterRuleSets(connectionContextLog)) {
            final Map<String, Object> params = filterRuleSet.getLoggerParams();

            final ConnectionContext connectionContext = connectionContextLog.getConnectionContext();

            if (connectionContext.getAdditionalFields().containsKey(
                            AbstractConnectionContext.ADDITIONAL_FIELD__STACKTRACE_EXCEPTION)) {
                final Exception ex = (Exception)connectionContext.getAdditionalFields()
                            .get(AbstractConnectionContext.ADDITIONAL_FIELD__STACKTRACE_EXCEPTION);
                LOG.fatal(connectionContextLog, ex);
            } else {
                LOG.warn(connectionContextLog);
            }
        }
    }
}
