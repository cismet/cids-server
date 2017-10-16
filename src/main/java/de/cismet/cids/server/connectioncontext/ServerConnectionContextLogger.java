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
public class ServerConnectionContextLogger {

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

        private static final ServerConnectionContextLogger INSTANCE = new ServerConnectionContextLogger();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
