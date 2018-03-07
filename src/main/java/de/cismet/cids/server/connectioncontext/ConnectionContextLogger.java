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

import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.util.Date;

import de.cismet.connectioncontext.AbstractConnectionContext;

import de.cismet.connectioncontext.AbstractConnectionContext.Category;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ConnectionContextLogger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConnectionContextLogger.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLogger object.
     */
    private ConnectionContextLogger() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContextLogger getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  context     DOCUMENT ME!
     * @param  user        DOCUMENT ME!
     * @param  methodName  DOCUMENT ME!
     * @param  params      DOCUMENT ME!
     */
    public void logConnectionContext(ConnectionContext context,
            final User user,
            final String methodName,
            final Object... params) {
        if (context == null) {
            context = ConnectionContext.createDeprecated();
        }
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(),
                user,
                context,
                methodName,
                params);
        final Exception ex = (Exception)context.getAdditionalFields()
                    .get(AbstractConnectionContext.ADDITIONAL_FIELD__STACKTRACE_EXCEPTION);
        if (Category.DEPRECATED.equals(context.getCategory())) {
            LOG.fatal(contextLog, ex);
        } else if (Category.DUMMY.equals(context.getCategory())) {
            LOG.error(contextLog, ex);
        } else {
            LOG.info(contextLog, ex);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final ConnectionContextLogger INSTANCE = new ConnectionContextLogger();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
