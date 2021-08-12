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
package de.cismet.cids.server.actions.graphql.exceptions;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FieldNotFoundException extends TableNotFoundException {

    //~ Instance fields --------------------------------------------------------

    private String field = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FieldNotFoundException object.
     *
     * @param  table  DOCUMENT ME!
     * @param  field  DOCUMENT ME!
     */
    public FieldNotFoundException(final String table, final String field) {
        super(table);
        this.field = field;
    }

    /**
     * Creates a new FieldNotFoundException object.
     *
     * @param  table  DOCUMENT ME!
     * @param  field  DOCUMENT ME!
     */
    public FieldNotFoundException(final int table, final String field) {
        super(table);
        this.field = field;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the field
     */
    public String getField() {
        return field;
    }
}
