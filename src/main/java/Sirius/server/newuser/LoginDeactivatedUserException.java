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
package Sirius.server.newuser;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class LoginDeactivatedUserException extends UserException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LoginRestrictionUserException object.
     *
     * @param  detailMessage  DOCUMENT ME!
     */
    public LoginDeactivatedUserException(final String detailMessage) {
        super(detailMessage);
    }

    /**
     * Creates a new LoginRestrictionUserException object.
     *
     * @param  detailMessage     DOCUMENT ME!
     * @param  wrongUserName     DOCUMENT ME!
     * @param  wrongPassword     DOCUMENT ME!
     * @param  wrongUserGroup    DOCUMENT ME!
     * @param  wrongLocalServer  DOCUMENT ME!
     */
    public LoginDeactivatedUserException(final String detailMessage,
            final boolean wrongUserName,
            final boolean wrongPassword,
            final boolean wrongUserGroup,
            final boolean wrongLocalServer) {
        super(detailMessage, wrongUserName, wrongPassword, wrongUserGroup, wrongLocalServer);
    }
}
