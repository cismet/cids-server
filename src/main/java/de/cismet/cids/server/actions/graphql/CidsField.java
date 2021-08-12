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
public class CidsField extends CidsDataSource {

    //~ Instance fields --------------------------------------------------------

    private CidsTable table;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsField object.
     *
     * @param  table  DOCUMENT ME!
     * @param  name   DOCUMENT ME!
     */
    public CidsField(final CidsTable table, final String name) {
        super(name);
        this.table = table;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the table
     */
    public CidsTable getTable() {
        return table;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  table  the table to set
     */
    public void setTable(final CidsTable table) {
        this.table = table;
    }
}
