/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry.monitor;

import Sirius.server.newuser.*;

import javax.swing.table.*;

import java.util.*;

import Sirius.server.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MonitorTableModel extends DefaultTableModel {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MonitorTableModel object.
     */
    public MonitorTableModel() {
        super();
    }
    /**
     * Creates a new MonitorTableModel object.
     *
     * @param  servers  DOCUMENT ME!
     * @param  cnames   DOCUMENT ME!
     */
    public MonitorTableModel(java.lang.Object[][] servers, java.lang.Object[] cnames) {
        super(servers, cnames);
    }

    //~ Methods ----------------------------------------------------------------

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   servers  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static java.lang.Object[][] convertToMatrix(Server[] servers) {
        java.lang.Object[][] matrix = new java.lang.Object[servers.length][];
        for (int i = 0; i < servers.length; i++) {
            java.lang.Object[] columnVals = new java.lang.Object[3];
            columnVals[0] = servers[i].getName();
            columnVals[1] = servers[i].getIP();
            columnVals[2] = servers[i].getPort();
            matrix[i] = columnVals;
        }
        return matrix;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   users  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static java.lang.Object[][] convertToMatrix(Vector users) {
        java.lang.Object[][] matrix = new java.lang.Object[users.size()][];
        for (int i = 0; i < users.size(); i++) {
            java.lang.Object[] columnVals = new java.lang.Object[6];

            columnVals[0] = new Integer(0); // new Integer(((User)users.get(i)).getID());
            columnVals[1] = ((User)users.get(i)).getName();
            columnVals[2] = ((User)users.get(i)).getDomain();
            columnVals[3] = ((User)users.get(i)).getUserGroup();
            columnVals[4] = new Boolean(((User)users.get(i)).isValid());
            columnVals[5] = new Boolean(((User)users.get(i)).isAdmin());
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
    public static java.util.Vector convertToVector(java.lang.Object[] obs) {
        return DefaultTableModel.convertToVector(obs);
    }
}
