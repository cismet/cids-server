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
public enum TextServerResources {

    //~ Enum constants ---------------------------------------------------------

    BUTLER_PROPERTIES("/de/cismet/cids/custom/utils/butler/butler.properties"),
    NAS_SERVER_PROPERTIES("/de/cismet/cids/custom/utils/nas/nasServer_conf.properties"),
    PNR_PROPERTIES("/de/cismet/cids/custom/utils/pointnumberreservation/pointNumberRes_conf.properties"),
    NAS_PRODUCT_DESCRIPTION_JSON("/de/cismet/cids/custom/nas/nasProductDescription.json"),
    FS_TEST_XML("/de/cismet/cids/custom/wunda_blau/res/formsolutions/TEST_CISMET00.xml"),
    FS_IGNORE_TRANSID_TXT("/de/cismet/cids/custom/wunda_blau/res/formsolutions/ignoreTransids.txt"),
    FME_DB_CONN_PROPERTIES("/de/cismet/cids/custom/wunda_blau/search/actions/fme_db_conn.properties"),
    TIFFER_ACTION_CFG("/de/cismet/cids/custom/wunda_blau/search/actions/tifferAction.cfg");

    //~ Instance fields --------------------------------------------------------

    private final String val;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  s  DOCUMENT ME!
     */
    TextServerResources(final String s) {
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
