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
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface LoginRestriction {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isLoginAllowed();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getKey();
    /**
     * DOCUMENT ME!
     *
     * @param  config  DOCUMENT ME!
     */
    void configure(String config);
}
