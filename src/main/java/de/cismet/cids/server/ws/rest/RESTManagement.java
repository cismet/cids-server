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
package de.cismet.cids.server.ws.rest;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class RESTManagement implements RESTManagementMBean {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getMaxThreads() {
        return RESTfulService.getMaxThreads();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  maxThreads  DOCUMENT ME!
     */
    @Override
    public void setMaxThreads(final int maxThreads) {
        RESTfulService.setMaxThreads(maxThreads);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getMinThreads() {
        return RESTfulService.getMinThreads();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  minThreads  DOCUMENT ME!
     */
    @Override
    public void setMinThreads(final int minThreads) {
        RESTfulService.setMinThreads(minThreads);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isThreadNamingEnabled() {
        return RESTfulService.isThreadNamingEnabled();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  threadNamingEnabled  DOCUMENT ME!
     */
    @Override
    public void setThreadNamingEnabled(final boolean threadNamingEnabled) {
        RESTfulService.setThreadNamingEnabled(threadNamingEnabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getRestThreadingStatus() {
        return RESTfulService.getThreadingStatus();
    }
}
