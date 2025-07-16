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
package Sirius.server.search;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SearchRuntimeException extends RuntimeException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchRuntimeException object.
     */
    public SearchRuntimeException() {
        super();
    }

    /**
     * Creates a new SearchRuntimeException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public SearchRuntimeException(final String message) {
        super(message);
    }

    /**
     * Creates a new SearchRuntimeException object.
     *
     * @param  cause  DOCUMENT ME!
     */
    public SearchRuntimeException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new SearchRuntimeException object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public SearchRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
