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
public class BinaryServerResource extends ServerResource {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BinaryServerResource object.
     *
     * @param  value  DOCUMENT ME!
     */
    public BinaryServerResource(final String value) {
        super(value, Type.BINARY);
    }
}
