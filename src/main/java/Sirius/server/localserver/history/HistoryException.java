/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.history;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class HistoryException extends Exception implements Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of <code>HistoryException</code> without detail message.
     */
    public HistoryException() {
    }

    /**
     * Constructs an instance of <code>HistoryException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public HistoryException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>HistoryException</code> with the specified detail message and the specified
     * cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public HistoryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
