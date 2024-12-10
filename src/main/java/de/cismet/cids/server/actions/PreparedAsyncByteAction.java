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
package de.cismet.cids.server.actions;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PreparedAsyncByteAction implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private String url;
    private long length;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PreparedAsyncByteAction object.
     *
     * @param  url  DOCUMENT ME!
     */
    public PreparedAsyncByteAction(final String url) {
        this(url, -1);
    }

    /**
     * Creates a new PreparedAsyncByteAction object.
     *
     * @param  url     DOCUMENT ME!
     * @param  length  DOCUMENT ME!
     */
    public PreparedAsyncByteAction(final String url, final long length) {
        this.url = url;
        this.length = length;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the length
     */
    public long getLength() {
        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  length  the length to set
     */
    public void setLength(final long length) {
        this.length = length;
    }
}
