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
package Sirius.server.localserver.object;

/**
 * This Exception should be used to throw Exception within the CustomDeletionProvider that should be shown to the user
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeletionProviderClientException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeletionProviderClientException object.
     *
     * @param  msg  DOCUMENT ME!
     */
    public DeletionProviderClientException(final String msg) {
        super(msg);
    }
}
