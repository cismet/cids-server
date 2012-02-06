/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class PersistenceException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of <code>PersistenceException</code> without detail message.
     */
    public PersistenceException() {
    }

    /**
     * Constructs an instance of <code>PersistenceException</code> with the specified detail message.
     *
     * @param  msg  the detail message.
     */
    public PersistenceException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>PersistenceException</code> with the specified detail message and the specified
     * cause.
     *
     * @param  msg    the detail message.
     * @param  cause  the exception cause
     */
    public PersistenceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
