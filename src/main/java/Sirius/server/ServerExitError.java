/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import java.io.Serializable;

/**
 * Will be thrown when a server reaches a non-recoverable state.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ServerExitError extends RuntimeException implements Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ServerExitError.
     */
    public ServerExitError() {
        super();
    }

    /**
     * Creates a new ServerExitError object.
     *
     * @param  message  DOCUMENT ME!
     */
    public ServerExitError(final String message) {
        super(message);
    }

    /**
     * Creates a new ServerExitError object.
     *
     * @param  cause  DOCUMENT ME!
     */
    public ServerExitError(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new ServerExitError object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public ServerExitError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
