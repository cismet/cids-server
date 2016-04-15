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
public interface RESTManagementMBean {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getMaxThreads();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getMinThreads();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getRestThreadingStatus();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isThreadNamingEnabled();

    /**
     * DOCUMENT ME!
     *
     * @param  maxThreads  DOCUMENT ME!
     */
    void setMaxThreads(final int maxThreads);

    /**
     * DOCUMENT ME!
     *
     * @param  minThreads  DOCUMENT ME!
     */
    void setMinThreads(final int minThreads);

    /**
     * DOCUMENT ME!
     *
     * @param  threadNamingEnabled  DOCUMENT ME!
     */
    void setThreadNamingEnabled(final boolean threadNamingEnabled);
}
