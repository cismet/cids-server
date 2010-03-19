/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import java.sql.*;

import java.util.*;

import Sirius.server.property.*;

import java.util.LinkedList;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBConnectionPool {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private LinkedList cons;

    //~ Constructors -----------------------------------------------------------

    /**
     * ///////////////////////////////////////////////////////////////////////
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public DBConnectionPool(DBClassifier dbc) throws Throwable {
        cons = new LinkedList();

        for (int i = 0; i < dbc.noOfConnections; i++) {
            DBConnection con = new DBConnection(dbc);
            int maxCons = 1;

            try {
                maxCons = con.getConnection().getMetaData().getMaxConnections();
            } catch (Exception e) {
                logger.error(e);
            }

            cons.add(con);

            logger.info("Info :: " + dbc + " allows " + maxCons + " connections 0 means unlimited");

            if ((maxCons < dbc.noOfConnections) && (maxCons != 0)) // 0 means unlimited
            {
                dbc.setNoOfConnections(maxCons);
                logger.error(
                    "\n requested number of identical connections exceeds maxConnections of the db" + "\n"
                    + " or jdbcdriver and therefore ist set to maximum possible");
            }
        }
    }
    /**
     * ///////////////////////////////////////////////////////////////////////////
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public DBConnectionPool(ServerProperties properties) throws Throwable {
        this(extractDBClassifiersFromProperties(properties));
    }

    /////////////////////////////////////////////////////////////////////////////

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized DBConnection getConnection() {
        // synchronized von hell eingefügt

        // ring
        DBConnection c = (DBConnection)cons.removeLast();

        cons.addFirst(c);

        return c;
    }
    /**
     * ///////////////////////////////////////////////////////////////////////////
     *
     * @param   props  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static DBClassifier extractDBClassifiersFromProperties(ServerProperties props) {
        // return (DBClassifier[]) properties.getObjectList("DBClassifiers",new DBClassifier());

        return new DBClassifier(
                props.getDbConnectionString(),
                props.getDbUser(),
                props.getDbPassword(),
                props.getJDBCDriver(),
                props.getPoolSize(),
                props.getSQLDialect());
    }

    /**
     * DOCUMENT ME!
     */
    public void closeConnections() {
        Iterator<DBConnection> iter = cons.iterator();

        while (iter.hasNext()) {
            try {
                // close connection
                iter.next().getConnection().close();
            } catch (SQLException e) {
                logger.error("<LS> ERROR :: could not close connection - try to close the next one", e);
            }
        }

        cons = new LinkedList();
    }
} // end class
