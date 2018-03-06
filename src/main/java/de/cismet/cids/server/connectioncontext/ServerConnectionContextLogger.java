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

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ServerConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ServerConnectionContextLogger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ServerConnectionContextLogger.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLogger object.
     */
    private ServerConnectionContextLogger() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ServerConnectionContextLogger getInstance() {
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
    public void logConnectionContext(ServerConnectionContext context,
            final User user,
            final String methodName,
            final Object... params) {
        if (context == null) {
            context = ServerConnectionContext.createDeprecated();
        }
        final ServerConnectionContextLog contextLog = new ServerConnectionContextLog(new Date(),
                user,
                context,
                methodName,
                params);
        if (ConnectionContext.Category.DEPRECATED.equals(context.getCategory())) {
            final Exception ex = (Exception)context.getAdditionalFields().get("EXCEPTION");
            if (ex != null) {
                LOG.fatal(contextLog, ex);
            } else {
                LOG.fatal(contextLog);
            }
        } else {
            LOG.info(contextLog);
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

        private static final ServerConnectionContextLogger INSTANCE = new ServerConnectionContextLogger();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
