/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.property.ServerProperties;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import org.postgresql.core.TransactionState;
import org.postgresql.jdbc.PgConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class DBConnectionPool extends Shutdown implements DBBackend {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DBConnectionPool.class);

    private static final List<String> TRANSIENT_SQL_STATES = Arrays.asList(
            "08",    // Connection exceptions - refused, broken, etc    // NOI18N
            "53",    // Insufficient resources - disk full, etc         // NOI18N
            "57P0",  // Db server shutdown/restart                      // NOI18N
            "40001", // Serialization failure                           // NOI18N
            "40P01"  // Deadlock detected                               // NOI18N
            );

    //~ Instance fields --------------------------------------------------------

    public transient int retriesOnError;

    private final transient LinkedBlockingQueue<DBConnection> cons;
    private final transient LinkedBlockingQueue<DBConnection> usedCons;
    private final transient DBClassifier dbClassifier;
//    private final ReleaseConnectionThread rct;
    private Executor executor = CismetExecutors.newFixedThreadPool(
            10,
            CismetConcurrency.getInstance("connectionPool").createThreadFactory("ConnectionPool"));

    private List<DBConnection> longTermConnectionList = new ArrayList<DBConnection>();
    private int numberOfConnections = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBConnectionPool object.
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public DBConnectionPool(final DBClassifier dbc) {
        if (dbc == null) {
            final String message = "given dbclassifier is null"; // NOI18N
            LOG.fatal(message);
            throw new ServerExitError(message);
        }

        this.dbClassifier = dbc;
        this.numberOfConnections = dbc.noOfConnections;
        cons = new LinkedBlockingQueue<DBConnection>(dbc.noOfConnections);
        usedCons = new LinkedBlockingQueue<DBConnection>(dbc.noOfConnections);

        for (int i = 0; i < dbc.noOfConnections; i++) {
            final CheckConnection checker = new CheckConnection();
            final DBConnection con = new DBConnection(dbc, checker);
            checker.setDbCon(con);
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

        // we will perform one more retry as there are connections to ensure at least one time a new connection is used
        retriesOnError = cons.size() + 1;

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutting down DBConnectionPool"); // NOI18N
                    }

                    for (final DBBackend con : cons) {
                        con.shutdown();
                    }
                }
            });
    }
    /**
     * Creates a new DBConnectionPool object.
     *
     * @param  props  DOCUMENT ME!
     */
    public DBConnectionPool(final ServerProperties props) {
        this(new DBClassifier(
                props.getDbConnectionString(),
                props.getDbUser(),
                props.getDbPassword(),
                props.getJDBCDriver(),
                props.getPoolSize(),
                props.getSQLDialect(),
                props.getInternalDialect()));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  this operation is marked as deprecated as it is discouraged to use a {@link DBConnection} directly.
     *              Use a DBBackend instead. This method is subject to be refactored to private access.
     */
    @Deprecated
    public DBConnection getDBConnection() {
        return getDBConnection(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param       longTerm  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  this operation is marked as deprecated as it is discouraged to use a {@link DBConnection} directly.
     *              Use a DBBackend instead. This method is subject to be refactored to private access.
     */
    @Deprecated
    public DBConnection getDBConnection(final boolean longTerm) {
        return getDBConnection(longTerm, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param       longTerm  DOCUMENT ME!
     * @param       cleanup   DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  this operation is marked as deprecated as it is discouraged to use a {@link DBConnection} directly.
     *              Use a DBBackend instead. This method is subject to be refactored to private access.
     */
    @Deprecated
    public DBConnection getDBConnection(final boolean longTerm, final boolean cleanup) {
        // ring
        DBConnection c = null;

        do {
            try {
                if (cons.isEmpty() && (longTermConnectionList.size() > (numberOfConnections / 2))) {
                    LOG.warn("no free connections left and long term connections: " + longTermConnectionList.size());
                }
                final DBConnection old = cons.take();
                // throw the connection away if it is closed and create a new one instead
                if (old.isClosed()) {
                    old.setConnectionChecker(null);
                    final CheckConnection checker = new CheckConnection();
                    c = new DBConnection(dbClassifier, checker);
                    checker.setDbCon(c);
                    boolean valid = false;

                    do {
                        try {
                            valid = c.isValid();
                        } catch (Exception e) {
                            c = new DBConnection(dbClassifier, checker);
                            checker.setDbCon(c);
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException ex) {
                                // nothing to do
                            }
                        }
                    } while (!valid);
                } else {
                    c = old;
                }
            } catch (InterruptedException e) {
                // nothing to do
            }
        } while (c == null);

        if (longTerm) {
            longTermConnectionList.add(c);
        } else {
            usedCons.add(c);

            if (cleanup) {
                executor.execute(c.getConnectionChecker());
            }
        }

        c.setPoolLeftTime(System.currentTimeMillis());

        return c;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connection  DOCUMENT ME!
     */
    public void releaseDbConnection(final Connection connection) {
        DBConnection dbConnection = null;

        for (final DBConnection tmp : longTermConnectionList) {
            if (tmp.getConnection().equals(connection)) {
                dbConnection = tmp;
                break;
            }
        }

        if (dbConnection != null) {
            longTermConnectionList.remove(dbConnection);
            cons.add(dbConnection);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void closeConnections() {
        DBConnection con = null;

        while ((con = cons.poll()) != null) {
            con.close();
        }
    }

    @Override
    public void setRetriesOnError(final int noOfRetries) {
        if (noOfRetries < 0) {
            retriesOnError = 0;
        } else {
            retriesOnError = noOfRetries;
        }
    }

    @Override
    public int getRetriesOnError() {
        return retriesOnError;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   longTerm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException     DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    public Connection getConnection(final boolean longTerm) throws SQLException {
        Connection con = null;
        final int retryCount = 0;
        DBConnection dbcon = null;

        while (con == null) {
            dbcon = getDBConnection(longTerm, false);
            final Connection candidate = dbcon.getConnection();
            if (candidate.isClosed()) {
                dbcon.close();
            } else {
                con = candidate;
            }
        }

        if (con == null) {
            final String message = "cannot create connections to database anymore"; // NOI18N
            LOG.fatal(message);
            throw new ServerExitError(message);
        }

        if ((dbcon != null) && !longTerm) {
            executor.execute(dbcon.getConnectionChecker());
        }

        return con;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(false);
    }

    /**
     * Tries to find a connection that can execute the statement described by the given descriptor. The condition is
     * that the statement must not have a ResultSet open. This implementation cycles through the available connections
     * and returns a free connection if it is found. Not that the connection may not be free anymore by the time it is
     * returned by this method, so be sure to call the method from a synchronized block with scope on the connection
     * list only. Otherwise it is not guaranteed that the given connection is still free.
     *
     * @param       descriptor  a statement descriptor defined by {@link DBBackend}
     *
     * @return      a free connection
     *
     * @throws      SQLException                   if the maximal amount of cycles (10) is reached and no free
     *                                             connection could be obtained
     * @throws      UnsupportedOperationException  DOCUMENT ME!
     *
     * @deprecated  don't use
     */
    private DBConnection getFreeConnection(final String descriptor) throws SQLException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    // TODO: the overhead code should be done in a proxy or similar
    @Override
    public ResultSet submitInternalQuery(final String descriptor, final Object... parameters) throws SQLException {
        if (isDown()) {
            final String message = "called operation on an already shutdown object: " + this; // NOI18N
            LOG.error(message);
            throw new SQLException(message, DBConnection.SQL_CODE_ALREADY_CLOSED);
        }

        // TODO: refactor for easier understanding
        // retriesOnError can be 0, we want to execute at least once, so we check for '<='
        for (int i = 0; i <= retriesOnError; ++i) {
            final DBConnection con = getDBConnection(false, false);
            try {
                return con.submitInternalQuery(descriptor, parameters);
            } catch (final SQLException e) {
                // the connection is probably invalid, so it is closed
                con.close();

                final StringBuilder message = new StringBuilder("error submitting internal query");               // NOI18N
                if ((i < retriesOnError) && isDbErrorTransient(e)) {
                    message.append(", retrying");                                                                 // NOI18N
                    LOG.warn(message, e);
                } else {
                    message.append(", not retrying (anymore): retries is '").append(retriesOnError).append('\''); // NOI18N
                    LOG.error(message, e);
                    throw e;
                }
            } finally {
                executor.execute(con.getConnectionChecker());
            }
        }

        assert false : "this code shall never be reached"; // NOI18N

        return null;
    }

    @Override
    public int submitInternalUpdate(final String descriptor, final Object... parameters) throws SQLException {
        if (isDown()) {
            final String message = "called operation on an already shutdown object: " + this; // NOI18N
            LOG.error(message);
            throw new SQLException(message, DBConnection.SQL_CODE_ALREADY_CLOSED);
        }

        // TODO: refactor for easier understanding
        // retriesOnError can be 0, we want to execute at least once, so we check for '<='
        for (int i = 0; i <= retriesOnError; ++i) {
            final DBConnection con = getDBConnection(false, false);
            try {
                return con.submitInternalUpdate(descriptor, parameters);
            } catch (final SQLException e) {
                // the connection is probably invalid, so it is closed
                con.close();

                final StringBuilder message = new StringBuilder("error submitting internal update");              // NOI18N
                if ((i < retriesOnError) && isDbErrorTransient(e)) {
                    message.append(", retrying");                                                                 // NOI18N
                    LOG.warn(message, e);
                } else {
                    message.append(", not retrying (anymore): retries is '").append(retriesOnError).append('\''); // NOI18N
                    LOG.error(message, e);
                    throw e;
                }
            } finally {
                executor.execute(con.getConnectionChecker());
            }
        }

        assert false : "this code shall never be reached"; // NOI18N

        return -1;
    }

    @Override
    public ResultSet submitQuery(final String descriptor, final Object... parameters) throws SQLException {
        final DBConnection con = getDBConnection();
        try {
            return con.submitQuery(descriptor, parameters);
        } finally {
            executor.execute(con.getConnectionChecker());
        }
    }

    @Override
    public ResultSet submitQuery(final int sqlID, final Object... parameters) throws SQLException {
        final DBConnection con = getDBConnection();
        try {
            return con.submitQuery(sqlID, parameters);
        } finally {
            executor.execute(con.getConnectionChecker());
        }
    }

    @Override
    public int submitUpdate(final String descriptor, final Object... parameters) throws SQLException {
        final DBConnection con = getDBConnection();
        try {
            return con.submitUpdate(descriptor, parameters);
        } finally {
            executor.execute(con.getConnectionChecker());
        }
    }

    @Override
    public int submitUpdate(final int sqlID, final Object... parameters) throws SQLException {
        final DBConnection con = getDBConnection();
        try {
            return con.submitUpdate(sqlID, parameters);
        } finally {
            executor.execute(con.getConnectionChecker());
        }
    }

    /**
     * Attempt to figure out whether a given database error could be transient and might be worth a retry. Detects
     * things like network timeouts, transaction deadlocks, serialization failures, connection drops, etc.
     *
     * @param   se  Exception thrown by persistence provider
     *
     * @return  True if the error might be transient
     */
    public static boolean isDbErrorTransient(final SQLException se) {
        if (se != null) {
            final String sqlState = se.getSQLState();
            for (final String s : TRANSIENT_SQL_STATES) {
                if (sqlState.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class CheckConnection implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private DBConnection dbCon = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CheckConnection object.
         */
        public CheckConnection() {
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the dbCon
         */
        public DBConnection getDbCon() {
            return dbCon;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dbCon  the dbCon to set
         */
        public void setDbCon(final DBConnection dbCon) {
            this.dbCon = dbCon;
        }

        @Override
        public void run() {
            boolean done = false;
            Thread.yield();
            final PgConnection con = ((PgConnection)dbCon.getConnection());

            do {
                try {
                    final TransactionState state = con.getQueryExecutor().getTransactionState();
                    if (state.equals(TransactionState.IDLE)) {
                        usedCons.remove(dbCon);
                        cons.put(dbCon);
                    } else if (state.equals(TransactionState.FAILED)) {
                        LOG.error("Remove db connection with transactionState failed");
                        try {
                            con.close();
                            dbCon.setConnectionChecker(null);
                        } catch (SQLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        usedCons.remove(dbCon);
                        // todo add new one
                        cons.put(dbCon);
                    } else if (state.equals(TransactionState.OPEN)) {
                        Thread.sleep(5);
                    }

                    done = true;
                } catch (InterruptedException e) {
                    cons.remove(dbCon);
                } catch (Throwable th) {
                    LOG.error("DB error", th);
                }
            } while (!done);
        }
    }
}
