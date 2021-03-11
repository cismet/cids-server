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
public class UserServiceManagement implements UserServiceManagementMBean {

    //~ Instance fields --------------------------------------------------------

    private boolean enabled = true;

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isUserServiceBackendEnabled() {
        return enabled;
    }

    @Override
    public void setUserServiceBackendEnabled(final boolean userServiceBackendEnabled) {
        this.enabled = userServiceBackendEnabled;
    }

    @Override
    public void regenerateSecret() {
        UserServiceImpl.recreateRandomKey();
    }
}
