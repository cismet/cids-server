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
public class TableNotFoundException extends Exception {

    //~ Instance fields --------------------------------------------------------

    private String table = null;
    private int classId = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TableNotFoundException object.
     *
     * @param  table  DOCUMENT ME!
     */
    public TableNotFoundException(final String table) {
        this.table = table;
    }

    /**
     * Creates a new TableNotFoundException object.
     *
     * @param  classId  DOCUMENT ME!
     */
    public TableNotFoundException(final int classId) {
        this.classId = classId;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the table
     */
    public String getTable() {
        return table;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the classId
     */
    public int getClassId() {
        return classId;
    }
}
