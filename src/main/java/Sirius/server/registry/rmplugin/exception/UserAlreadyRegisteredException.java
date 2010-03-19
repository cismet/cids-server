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
public class UserAlreadyRegisteredException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UnableToDeregister.
     */
    public UserAlreadyRegisteredException() {
        super();
    }

    /**
     * Creates a new UserAlreadyRegisteredException object.
     *
     * @param  message  DOCUMENT ME!
     */
    public UserAlreadyRegisteredException(String message) {
        super(message);
    }
}
