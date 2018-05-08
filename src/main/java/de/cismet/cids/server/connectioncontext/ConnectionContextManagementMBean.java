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

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface ConnectionContextManagementMBean {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isConnectionContextBackendEnabled();

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContextBackendEnabled  DOCUMENT ME!
     */
    void setConnectionContextBackendEnabled(final boolean connectionContextBackendEnabled);

    /**
     * DOCUMENT ME!
     */
    void reloadConnectionContextConfig();
}
