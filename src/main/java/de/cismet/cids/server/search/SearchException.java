/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SearchException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of <code>SearchException</code> without detail message.
     */
    public SearchException() {
    }

    /**
     * Constructs an instance of <code>SearchException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public SearchException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>SearchException</code> with the specified detail message and the specified cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public SearchException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
