/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.Shutdownable;
import Sirius.server.property.ServerProperties;

import org.apache.log4j.Logger;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBConnectionPool extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------


    private static final transient Logger LOG = Logger.getLogger(DBConnectionPool.class);

    //~ Instance fields --------------------------------------------------------

    private final transient LinkedList<DBConnection> cons;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBConnectionPool object.
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public DBConnectionPool(final DBClassifier dbc) throws Throwable {
        cons = new LinkedList<DBConnection>();

        for (int i = 0; i < dbc.noOfConnections; i++) {
            final DBConnection con = new DBConnection(dbc);
            int maxCons = 1;

            try {
                maxCons = con.getConnection().getMetaData().getMaxConnections();
            } catch (final Exception e) {
                LOG.warn("could not fetch max connections from connection metadata", e); // NOI18N
            }

            cons.add(con);

            if (LOG.isInfoEnabled()) {
                LOG.info("Info :: " + dbc + " allows " + maxCons + " connections, 0 means unlimited"); // NOI18N
            }

            if ((maxCons < dbc.noOfConnections) && (maxCons != 0))                                     // 0 means unlimited
            {
                dbc.setNoOfConnections(maxCons);
                LOG.warn("requested number of identical connections exceeds maxConnections of the db " // NOI18N
                            + "or jdbcdriver and is therefore set to maximum possible");               // NOI18N
            }
        }

        addShutdown(new Shutdownable() {

                @Override
                public void shutdown() throws ServerExitError {
                    closeConnections();
                }
            });
    }
    /**
     * Creates a new DBConnectionPool object.
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public DBConnectionPool(final ServerProperties properties) throws Throwable {
        this(extractDBClassifiersFromProperties(properties));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DBConnection getConnection() {
        // ring
        final DBConnection c;
        synchronized (cons) {
            c = cons.removeLast();
            cons.addFirst(c);
        }

        return c;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   props  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static DBClassifier extractDBClassifiersFromProperties(final ServerProperties props) {
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
        synchronized (cons) {
            final Iterator<DBConnection> iter = cons.iterator();

            while (iter.hasNext()) {
                try {
                    // close connection
                    iter.next().getConnection().close();
                } catch (SQLException e) {
                    LOG.error("<LS> ERROR :: could not close connection - try to close the next one", e); // NOI18N
                }
            }

            cons.clear();
        }
    }
}
