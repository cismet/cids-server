/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.sql.DBConnection;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class TransactionHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(TransactionHelper.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Connection con;
    private boolean workBegun;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TransactionHelper.
     *
     * @param   con  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    TransactionHelper(final Connection con) throws SQLException {
        this.con = con;
        con.setAutoCommit(false);
        workBegun = false;
    }

    //~ Methods ----------------------------------------------------------------

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
            workBegun = false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    void close() {
        DBConnection.closeConnections(con);
    }
}
