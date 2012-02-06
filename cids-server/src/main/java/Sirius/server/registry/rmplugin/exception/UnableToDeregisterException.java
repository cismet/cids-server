/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UnableToDeregister.java
 *
 * Created on 24. November 2006, 15:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin.exception;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class UnableToDeregisterException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UnableToDeregister.
     */
    public UnableToDeregisterException() {
        super();
    }

    /**
     * Creates a new UnableToDeregisterException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public UnableToDeregisterException(final String message) {
        super(message);
    }
}
