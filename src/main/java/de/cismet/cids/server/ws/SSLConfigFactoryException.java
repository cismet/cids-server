/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SSLConfigFactoryException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of <code>SSLConfigFactoryException</code> without detail message.
     */
    public SSLConfigFactoryException() {
    }

    /**
     * Constructs an instance of <code>SSLConfigFactoryException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public SSLConfigFactoryException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>SSLConfigFactoryException</code> with the specified detail message and the
     * specified cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public SSLConfigFactoryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
