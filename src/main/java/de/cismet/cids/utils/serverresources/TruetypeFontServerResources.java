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
package de.cismet.cids.utils.serverresources;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public enum TruetypeFontServerResources {

    //~ Enum constants ---------------------------------------------------------

    IMAGE_ANNOTATOR_FONT("/de/cismet/cids/custom/wunda_blau/search/actions/Calibri_Bold.ttf");

    //~ Instance fields --------------------------------------------------------

    private final String val;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  s  DOCUMENT ME!
     */
    TruetypeFontServerResources(final String s) {
        this.val = s;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getValue() {
        return val;
    }
}
