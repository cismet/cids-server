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
    private String schemaName;
    private Boolean isDaqView = true;
    private String indexFile;
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

    /**
     * DOCUMENT ME!
     *
     * @return  the schemaName
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  schemaName  the schemaName to set
     */
    public void setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the isDaqView
     */
    public Boolean isIsDaqView() {
        return isDaqView;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isDaqView  the isDaqView to set
     */
    public void setIsDaqView(final Boolean isDaqView) {
        this.isDaqView = isDaqView;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the indexFile
     */
    public String getIndexFile() {
        return indexFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  indexFile  the indexFile to set
     */
    public void setIndexFile(final String indexFile) {
        this.indexFile = indexFile;
    }
}
