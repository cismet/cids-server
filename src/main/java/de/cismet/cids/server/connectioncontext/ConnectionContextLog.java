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

import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;

import java.util.Arrays;
import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class ConnectionContextLog {

    //~ Static fields/initializers ---------------------------------------------

    private static final String LOG_FORMAT = "[%s %s@%s] %s => %s %s";

    //~ Instance fields --------------------------------------------------------

    private final Date timestamp;
    private final User user;
    private final ConnectionContext context;
    private final String methodName;
    private final Object[] methodParams;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionContextLog object.
     *
     * @param  timestamp     DOCUMENT ME!
     * @param  user          DOCUMENT ME!
     * @param  context       DOCUMENT ME!
     * @param  methodName    DOCUMENT ME!
     * @param  methodParams  DOCUMENT ME!
     */
    public ConnectionContextLog(final Date timestamp,
            final User user,
            final ConnectionContext context,
            final String methodName,
            final Object[] methodParams) {
        this.timestamp = timestamp;
        this.user = user;
        this.context = context;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return String.format(
                LOG_FORMAT,
                DateFormat.getDateTimeInstance().format(timestamp),
                (user != null) ? user.getName() : null,
                (user != null) ? user.getDomain() : null,
                (context != null) ? context.getContent() : null,
                methodName,
                (methodParams != null) ? Arrays.toString(methodParams) : null);
    }
}
