/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TransactionHelper {

    //~ Instance fields --------------------------------------------------------

    private final transient Connection con;
    private boolean workBegun;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TransactionHelper.
     *
     * @param  dbcon       DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    TransactionHelper(final DBConnection dbcon, final ServerProperties properties) {
        this.con = dbcon.getConnection();
        workBegun = false;
    }

    /**
     * prohibit usage of standard constructor.
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    private TransactionHelper() {
        throw new UnsupportedOperationException("don't use this!"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  workBegun  DOCUMENT ME!
     */
    public void setWorkBegun(final boolean workBegun) {
        this.workBegun = workBegun;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getWorkBegun() {
        return workBegun;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Connection getConnection() {
        return con;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    void rollback() throws SQLException {
        if (workBegun) {
            con.rollback();
            con.setAutoCommit(true);
        }
        workBegun = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    void beginWork() throws SQLException {
        if (!workBegun) {
            con.setAutoCommit(false);
            con.createStatement().execute("begin"); // NOI18N
            workBegun = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    void commit() throws SQLException {
        if (workBegun) {
            con.commit();
            con.setAutoCommit(true);
        }
    }
}
