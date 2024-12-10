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

import java.io.InputStream;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class UploadableInputStream {

    //~ Instance fields --------------------------------------------------------

    private InputStream inputStream;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UploadableInputStream object.
     *
     * @param  inputStream  DOCUMENT ME!
     */
    public UploadableInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public InputStream getInputStream() {
        return inputStream;
    }
}
