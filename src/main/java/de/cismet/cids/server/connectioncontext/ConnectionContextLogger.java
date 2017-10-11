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

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ConnectionContextLogger {

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
        final ConnectionContextLog contextLog = new ConnectionContextLog(new Date(), user, context, methodName, params);
        System.out.println(contextLog);
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
