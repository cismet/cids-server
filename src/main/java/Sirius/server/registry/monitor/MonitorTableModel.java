/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry.monitor;

import Sirius.server.*;
import Sirius.server.newuser.*;

import java.util.*;

import javax.swing.table.*;

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
    public MonitorTableModel(final java.lang.Object[][] servers, final java.lang.Object[] cnames) {
        super(servers, cnames);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   servers  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static java.lang.Object[][] convertToMatrix(final Server[] servers) {
        final java.lang.Object[][] matrix = new java.lang.Object[servers.length][];
        for (int i = 0; i < servers.length; i++) {
            final java.lang.Object[] columnVals = new java.lang.Object[3];
            columnVals[0] = servers[i].getName();
            columnVals[1] = servers[i].getIP();
            columnVals[2] = servers[i].getServerPort();
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
    public static java.lang.Object[][] convertToMatrix(final Vector users) {
        final java.lang.Object[][] matrix = new java.lang.Object[users.size()][];
        for (int i = 0; i < users.size(); i++) {
            final java.lang.Object[] columnVals = new java.lang.Object[6];

            final User user = ((User)users.get(i));
            // TODO check for all userGroups
            final UserGroup userGroup = user.getUserGroup();

            columnVals[0] = new Integer(0); // new Integer(((User)users.get(i)).getID());
            columnVals[1] = user.getName();
            columnVals[2] = user.getDomain();
            columnVals[3] = userGroup;
            columnVals[4] = new Boolean(user.isValid());
            columnVals[5] = new Boolean(user.isAdmin());
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
