/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
//------------------------------------------------------------------------------
//
// Project   : NamingServerMonitor
// File name : TableModel.java
// Author    : Rene Wendling
// Date      : 24.10.2000
//
//------------------------------------------------------------------------------
//
package Sirius.util.NamingServerMonitor;

import java.util.*;

import javax.swing.table.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class TableModel extends DefaultTableModel {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TableModel object.
     */
    public TableModel() {
        super();
    }
    /**
     * Creates a new TableModel object.
     *
     * @param  bounds  DOCUMENT ME!
     * @param  cnames  DOCUMENT ME!
     */
    public TableModel(final java.lang.Object[][] bounds, final java.lang.Object[] cnames) {
        super(bounds, cnames);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bounds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static java.lang.Object[][] convertToMatrix(final String[] bounds) {
        final java.lang.Object[][] matrix = new java.lang.Object[bounds.length][];
        for (int i = 0; i < bounds.length; i++) {
            final java.lang.Object[] columnVals = new java.lang.Object[1];
            columnVals[0] = bounds[i];

            matrix[i] = columnVals;
        }
        return matrix;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static java.util.Vector convertToVector(final java.lang.Object[] obs) {
        return DefaultTableModel.convertToVector(obs);
    }
}
