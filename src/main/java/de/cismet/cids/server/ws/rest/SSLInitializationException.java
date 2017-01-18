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
package de.cismet.cids.server.ws.rest;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class SSLInitializationException extends RuntimeException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SSLInitializationException object.
     */
    public SSLInitializationException() {
    }

    /**
     * Creates a new SSLInitializationException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public SSLInitializationException(final String message) {
        super(message);
    }

    /**
     * Creates a new SSLInitializationException object.
     *
     * @param  cause  DOCUMENT ME!
     */
    public SSLInitializationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new SSLInitializationException object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public SSLInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
