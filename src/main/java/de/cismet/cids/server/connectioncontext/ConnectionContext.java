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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import java.util.Arrays;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class ConnectionContext implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient boolean SHOW_FULL_DEPRECATED_STACKTRACE = false;

    //~ Instance fields --------------------------------------------------------

    private final String content;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RESTFulSerialContext object.
     *
     * @param  content  DOCUMENT ME!
     */
    private ConnectionContext(final String content) {
        this.content = content;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return "context";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContext create(final String context) {
        return new ConnectionContext(context);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConnectionContext createDeprecated() {
        final StackTraceElement[] elements = new Exception().getStackTrace();
        return new ConnectionContext("DEPRECATED "
                        + (SHOW_FULL_DEPRECATED_STACKTRACE ? Arrays.toString(elements) : elements[1].toString()));
    }
}
