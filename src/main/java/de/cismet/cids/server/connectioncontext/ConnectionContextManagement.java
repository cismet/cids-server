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
package de.cismet.cids.server.connectioncontext;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class ConnectionContextManagement implements ConnectionContextManagementMBean {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isConnectionContextBackendEnabled() {
        return ConnectionContextBackend.getInstance().isEnabled();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContextBackendEnabled  DOCUMENT ME!
     */
    @Override
    public void setConnectionContextBackendEnabled(final boolean connectionContextBackendEnabled) {
        ConnectionContextBackend.getInstance().setEnabled(connectionContextBackendEnabled);
    }

    @Override
    public void reloadConnectionContextConfig() {
        final String configFilePath = DomainServerImpl.getServerProperties().getConnectionContextConfig();
        ConnectionContextBackend.getInstance().loadConfig(configFilePath);
    }
}
