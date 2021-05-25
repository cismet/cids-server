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
package de.cismet.cids.server;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DaqCacheRefreshServiceViewConfiguration {

    //~ Instance fields --------------------------------------------------------

    private String viewName;
    private Integer timeInSeconds;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the viewName
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  viewName  the viewName to set
     */
    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the timeInMinutes
     */
    public Integer getTimeInSeconds() {
        return timeInSeconds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  timeInSeconds  timeInMinutes the timeInMinutes to set
     */
    public void setTimeInSeconds(final Integer timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }
}
