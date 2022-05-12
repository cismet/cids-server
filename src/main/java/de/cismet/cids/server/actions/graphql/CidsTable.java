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
package de.cismet.cids.server.actions.graphql;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsTable extends CidsDataSource {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsTable object.
     *
     * @param  name  DOCUMENT ME!
     */
    public CidsTable(final String name) {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return getName();
    }
}
