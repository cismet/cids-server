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
package de.cismet.cids.nodepermissions;

import Sirius.server.middleware.types.MetaObjectNode;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class NoNodePermissionProvidedException extends Exception {

    //~ Instance fields --------------------------------------------------------

    MetaObjectNode mon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NoNodePermissionProvidedException object.
     *
     * @param  mon  DOCUMENT ME!
     */
    public NoNodePermissionProvidedException(final MetaObjectNode mon) {
        this.mon = mon;
    }

    /**
     * Creates a new NoNodePermissionProvidedException object.
     *
     * @param  mon    DOCUMENT ME!
     * @param  cause  DOCUMENT ME!
     */
    public NoNodePermissionProvidedException(final MetaObjectNode mon, final Throwable cause) {
        super(cause);
        this.mon = mon;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaObjectNode getMon() {
        return mon;
    }
}
