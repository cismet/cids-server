/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ServerExit.java
 *
 * Created on 1. M\u00E4rz 2004, 15:07
 */
package Sirius.server;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ServerExit extends java.lang.Throwable implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ServerExitError.
     */
    public ServerExit() {
        super();
    }

    /**
     * Creates a new ServerExit object.
     *
     * @param  message  DOCUMENT ME!
     */
    public ServerExit(String message) {
        super(message);
    }

    /**
     * Creates a new ServerExit object.
     *
     * @param  cause  DOCUMENT ME!
     */
    public ServerExit(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new ServerExit object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public ServerExit(String message, Throwable cause) {
        super(message, cause);
    }
}
