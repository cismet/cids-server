/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ServerExitError.java
 *
 * Created on 1. M\u00E4rz 2004, 14:46
 */
package Sirius.server;

/**
 * Will be thrown when a server process (registry,domainserver,proxy terminates.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ServerExitError extends java.lang.Throwable implements java.io.Serializable {

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
    public ServerExitError(String message) {
        super(message);
    }

    /**
     * Creates a new ServerExitError object.
     *
     * @param  cause  DOCUMENT ME!
     */
    public ServerExitError(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new ServerExitError object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public ServerExitError(String message, Throwable cause) {
        super(message, cause);
    }
}
