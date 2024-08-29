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
package Sirius.server.localserver.user;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PasswordCheckException extends Exception implements java.io.Serializable {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserException object.
     *
     * @param  detailMessage  DOCUMENT ME!
     */
    public PasswordCheckException(final String detailMessage) {
        super(detailMessage);
    }
}
