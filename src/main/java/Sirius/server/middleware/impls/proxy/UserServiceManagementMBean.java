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
package Sirius.server.middleware.impls.proxy;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface UserServiceManagementMBean {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isUserServiceBackendEnabled();

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContextBackendEnabled  DOCUMENT ME!
     */
    void setUserServiceBackendEnabled(final boolean connectionContextBackendEnabled);

    /**
     * DOCUMENT ME!
     */
    void regenerateSecret();
}
