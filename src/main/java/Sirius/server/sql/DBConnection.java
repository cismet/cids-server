/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.ServerExitError;
import Sirius.server.search.Query;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import org.postgis.PGbox3d;
import org.postgis.PGgeometry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.cismet.tools.Sorter;

/**
 * DOCUMENT ME!
 *
 * @author   Sascha Schlobinski
 * @author   mscholl
 * @version  1.1 refactored on 2011/01/13
 */

public final class DBConnection implements DBBackend {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DBConnection.class);

    public static final String SQL_CODE_ALREADY_CLOSED = "FFFFF"; // NOI18N
    public static final String SQL_CODE_INVALID_DESC = "FFFF01";  // NOI18N

    // TODO: exchange simple descriptor string with small descriptor class that contains additional metadata regarding
    // the query specific information
    public static final String DESC_VERIFY_USER_PW = "verify_user_password";                                   // NOI18N
    public static final String DESC_FETCH_DOMAIN_ID_FROM_DOMAIN_STRING = "fetch_domain_id_from_domain_string"; // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_KEY_ID = "fetch_config_attr_key_id";                     // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_USER_VALUE = "fetch_config_attr_user_value";             // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_UG_VALUE = "fetch_config_attr_ug_value";                 // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_DOMAIN_VALUE = "fetch_config_attr_domain_value";         // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_EXEMPT_VALUE = "fetch_config_attr_exempt_value";         // NOI18N
    public static final String DESC_FETCH_HISTORY = "fetch_history";                                           // NOI18N
    public static final String DESC_FETCH_HISTORY_LIMIT = "fetch_history_limit";                               // NOI18N
    public static final String DESC_INSERT_HISTORY_ENTRY = "insert_history_entry";                             // NOI18N
    public static final String DESC_HAS_HISTORY = "has_history";                                               // NOI18N
    public static final String DESC_TABLE_HAS_COLUMN = "table_has_column";                                     // NOI18N
    public static final String DESC_DELETE_STRINGREPCACHEENTRY = "delete_stringrepcacheentry";                 // NOI18N
    public static final String DESC_INSERT_STRINGREPCACHEENTRY = "insert_stringrepcacheentry";                 // NOI18N
    public static final String DESC_UPDATE_STRINGREPCACHEENTRY = "update_stringrepcacheentry";                 // NOI18N

    public static final String DESC_GET_ALL_USERGROUPS = "get_all_usergroups";             // NOI18N
    public static final String DESC_GET_ALL_CLASSES = "get_all_classes";                   // NOI18N
    public static final String DESC_GET_ALL_CLASS_ATTRIBUTES = "get_all_class_attributes"; // NOI18N
    public static final String DESC_GET_ALL_METHODS = "get_all_methods";                   // NOI18N
    public static final String DESC_GET_ALL_IMAGES = "get_all_images";                     // NOI18N
    public static final String DESC_GET_ALL_USERS = "get_all_users";                       // NOI18N
    public static final String DESC_GET_ALL_MEMBERSHIPS = "get_all_memberships";           // NOI18N
    public static final String DESC_CHANGE_USER_PASSWORD = "change_user_password";         // NOI18N
    public static final String DESC_GET_ALL_CLASS_PERMS = "get_all_class_permissions";     // NOI18N
    public static final String DESC_GET_ALL_METHOD_PERMS = "get_all_method_permissions";   // NOI18N
    public static final String DESC_GET_ATTRIBUTE_INFO = "get_attribute_info";             // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected final DBClassifier dbc;

    private final transient ReentrantReadWriteLock rwLock;
    // isClosed must be "protected" by the rwLock thus no reads or writes shall be done to the variable without
    // acquiring an appropriate lock
    private transient boolean isClosed;

    private final Connection con;
    private final StatementCache cache;
    private final Map<String, PreparedStatement> internalQueries;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBConnection object.
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected DBConnection(final DBClassifier dbc) throws ServerExitError {
        this.dbc = dbc;
        internalQueries = new HashMap<String, PreparedStatement>(10, 0.8f);

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("driver  :" + dbc.driver); // NOI18N
            }

            Class.forName(dbc.driver); // can raise an ClassNotFoundExc.

            con = DriverManager.getConnection(dbc.url, dbc.login, dbc.pwd); // can raise an SQl EXc.

            if (dbc.driver.equals("org.postgresql.Driver")) {                                 // NOI18N
                ((org.postgresql.PGConnection)con).addDataType("geometry", PGgeometry.class); // NOI18N
                ((org.postgresql.PGConnection)con).addDataType("box3d", PGbox3d.class);       // NOI18N
                if (LOG.isDebugEnabled()) {
                    LOG.debug("postgis datatypes added to connection");                       // NOI18N
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("connection established to " + this.dbc);                           // NOI18N
            }

            cache = new StatementCache(con);
        } catch (final ClassNotFoundException e) {
            LOG.error("<LS> ERROR :: " + e.getMessage() + " Driver Not Found", e); // NOI18N
            throw new ServerExitError(" Driver Not Found", e);                     // NOI18N
        } catch (final SQLException e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: could not connect to " + dbc, e);             // NOI18N
            throw new ServerExitError(" could not connect to db", e);              // NOI18N
        } catch (final Exception e) {
            LOG.error("<LS> ERROR :: " + e.getMessage(), e);                       // NOI18N
            throw new ServerExitError(e);
        }

        isClosed = false;
        rwLock = new ReentrantReadWriteLock(true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   bool  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean charToBool(final char bool) {
        return ((bool == (byte)'T') || (bool == (byte)'t'));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bool  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean stringToBool(final String bool) {
        if ((bool == null) || (bool.length() == 0)) {
            return false;
        } else {
            return charToBool(bool.charAt(0));
        }
    }

    /**
     * The currently logged-in db username.
     *
     * @return  DB username
     */
    public String getUser() {
        return dbc.login;
    }

    /**
     * The password of the currently logged-in db user.
     *
     * @return  DB user password
     */
    public String getPassword() {
        return dbc.pwd;
    }

    /**
     * The URL of the jdbc driver connection.
     *
     * @return  DB jdbc connection url
     */
    public String getURL() {
        return dbc.url;
    }

    /**
     * The driver class of the jdbc driver.
     *
     * @return  driver class of jdbc driver
     */
    public String getDriver() {
        return dbc.driver;
    }

    /**
     * The {@link Connection} used by this <code>DBConnection.</code>
     *
     * @return  the {@link Connection} used by this <code>DBConnection</code>
     */
    @Override
    public Connection getConnection() {
        return con;
    }

    /**
     * DOCUMENT ME!
     */
    public void close() {
        try {
            rwLock.writeLock().lock();
            isClosed = true;

            closeStatements(internalQueries.values().toArray(new PreparedStatement[internalQueries.size()]));
            closeConnections(con);

            internalQueries.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public ResultSet submitInternalQuery(final String descriptor, final Object... parameters) throws SQLException {
        try {
            rwLock.readLock().lock();
            if (isClosed) {
                final String message = "called operation on an already closed object: " + this; // NOI18N
                LOG.error(message);
                throw new SQLException(message, SQL_CODE_ALREADY_CLOSED);
            }

            // synchronizing prevents that we return by accident a result set
            // that has been created/prepared by antoher thread.
            synchronized (this) {
                // we don't close the statement here as it is cached for further usage
                final PreparedStatement stmt = prepareQuery(descriptor, parameters);

                // TODO: does't care if there is an open resultset, must be refactored
                final ResultSet set = stmt.executeQuery();

                return set;
            }
        } catch (final MissingResourceException e) {
            final String message = "invalid descriptor: " + descriptor; // NOI18N
            LOG.error(message, e);
            throw new SQLException(message, SQL_CODE_INVALID_DESC, e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int submitInternalUpdate(final String descriptor, final Object... parameters) throws SQLException {
        try {
            rwLock.readLock().lock();
            if (isClosed) {
                final String message = "called operation on an already closed object: " + this; // NOI18N
                LOG.error(message);
                throw new SQLException(message, SQL_CODE_ALREADY_CLOSED);
            }

            // we don't close the statement here as it is cached for further usage
            return prepareQuery(descriptor, parameters).executeUpdate();
        } catch (final MissingResourceException e) {
            final String message = "invalid descriptor: " + descriptor; // NOI18N
            LOG.error(message, e);
            throw new SQLException(message, SQL_CODE_INVALID_DESC, e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private PreparedStatement prepareQuery(final String descriptor, final Object... parameters) throws SQLException {
        if (!internalQueries.containsKey(descriptor) || internalQueries.get(descriptor).isClosed()) {
            final String stmt = NbBundle.getMessage(DBConnection.class, descriptor);
            final PreparedStatement ps = con.prepareStatement(stmt);
            ps.setPoolable(true);
            if (parameters.length == ps.getParameterMetaData().getParameterCount()) {
                internalQueries.put(descriptor, ps);
            } else {
                final String message = "parameter count mismmatch for descriptor '" // NOI18N
                            + descriptor
                            + "', Statement: "                                      // NOI18N
                            + stmt
                            + ", Statement param count: "                           // NOI18N
                            + ps.getParameterMetaData().getParameterCount()
                            + ", given param count: "                               // NOI18N
                            + parameters.length;
                LOG.error(message);
                throw new SQLException(message, SQL_CODE_INVALID_DESC);
            }
        }

        final PreparedStatement ps = internalQueries.get(descriptor);
        for (int i = 0; i < parameters.length; ++i) {
            final Object toSet = parameters[i];
            if (toSet == null) {
                ps.setNull(i + 1, ps.getParameterMetaData().getParameterType(i + 1));
            } else {
                ps.setObject(i + 1, toSet);
            }
        }

        return ps;
    }

    @Override
    public ResultSet submitQuery(final String descriptor, final Object... parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + descriptor); // NOI18N
        }

        String sqlStmnt = fetchStatement(descriptor);
        sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("info :: " + sqlStmnt); // NOI18N
        }

        // TODO: how to deal with the opened resources
        return (con.createStatement()).executeQuery(sqlStmnt);
    }

    @Override
    public ResultSet submitQuery(final int sqlID, final Object... parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + sqlID); // NOI18N
        }

        String sqlStmnt = fetchStatement(sqlID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt); // NOI18N
        }

        sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt); // NOI18N
        }

        // TODO: how to deal with the opened resources
        return (con.createStatement()).executeQuery(sqlStmnt);
    }

    @Override
    public ResultSet submitQuery(final Query q) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + q.getKey() + ", batch: " + q.isBatch()); // NOI18N
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("query object :: " + q);                                   // NOI18N
        }
        final Collection tmp = q.getParameterList();

        final Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);

        Sorter.quickSort(params);

        if (q.getQueryIdentifier().getName().equals("")) { // NOI18N
            return submitQuery(q.getQueryIdentifier().getQueryId(), (Object[])params);
        } else {
            return submitQuery(q.getQueryIdentifier().getName(), (Object[])params);
        }
    }

    @Override
    public int submitUpdate(final Query q) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + q.getKey() + ", batch: " + q.isBatch()); // NOI18N
        }

        final Collection tmp = q.getParameterList();
        final Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);

        Sorter.quickSort(params);

        if (q.isBatch()) {
            if (q.getQueryIdentifier().getName().equals("")) { // NOI18N
                return submitUpdateBatch(q.getQueryIdentifier().getQueryId(), params);
            } else {
                return submitUpdateBatch(q.getQueryIdentifier().getName(), params);
            }
        } else {
            if (q.getQueryIdentifier().getName().equals("")) { // NOI18N
                return submitUpdate(q.getQueryIdentifier().getQueryId(), (Object[])params);
            } else {
                return submitUpdate(q.getQueryIdentifier().getName(), (Object[])params);
            }
        }
    }

    @Override
    public int submitUpdate(final String descriptor, final Object... parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + descriptor); // NOI18N
        }

        return internalSubmitUpdate(fetchStatement(descriptor), parameters);
    }

    @Override
    public int submitUpdate(final int sqlID, final Object... parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + sqlID); // NOI18N
        }

        return internalSubmitUpdate(fetchStatement(sqlID), parameters);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   statement   DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private int internalSubmitUpdate(final String statement, final Object... parameters) throws SQLException {
        final String sqlStmnt = QueryParametrizer.parametrize(statement, parameters);

        int result = 0;

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeUpdate(sqlStmnt);
        } finally {
            closeStatements(stmt);
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   descriptor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private String fetchStatement(final String descriptor) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchStatement: " + descriptor); // NOI18N
        }

        if (cache.containsStatement(descriptor)) {
            return cache.getStatement(descriptor).getStatement();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sqlID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private String fetchStatement(final int sqlID) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchStatement: " + sqlID); // NOI18N
        }

        if (cache.containsStatement(sqlID)) {
            return cache.getStatement(sqlID).getStatement();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public StatementCache getStatementCache() {
        return cache;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   q  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public ResultSet executeQuery(final Query q) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeQuery: " + q.getKey() + ", batch: " + q.isBatch()); // NOI18N
        }

        if (q.getStatement() == null) { // sql aus dem cache
            return submitQuery(q);
        } else {
            String sqlStmnt = q.getStatement();

            final Collection tmp = q.getParameterList();
            final Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
            Sorter.quickSort(params);
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, params);

            if (LOG.isDebugEnabled()) {
                LOG.debug("INFO executeQuery :: " + sqlStmnt); // NOI18N
            }

            // TODO: how to deal with the opened resources
            return (con.createStatement()).executeQuery(sqlStmnt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   qid         DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private int submitUpdateBatch(final int qid, final Object[] parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + qid); // NOI18N
        }

        String updateBatch = fetchStatement(qid);

        updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);

        final StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";"); // NOI18N

        final String[] updates = new String[tokenizer.countTokens()];

        for (int i = 0; i < updates.length; i++) {
            updates[i] = tokenizer.nextToken();
        }

        int rowsEffected = 0;

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < updates.length; i++) {
                rowsEffected += stmt.executeUpdate(updates[i]);
            }
        } finally {
            closeStatements(stmt);
        }

        return rowsEffected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   queryname   DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private int submitUpdateBatch(final String queryname, final Object[] parameters) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + queryname); // NOI18N
        }

        String updateBatch = fetchStatement(queryname);

        updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);

        final StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";"); // NOI18N

        final String[] updates = new String[tokenizer.countTokens()];

        for (int i = 0; i < updates.length; i++) {
            updates[i] = tokenizer.nextToken();
        }

        int rowsEffected = 0;

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < updates.length; i++) {
                rowsEffected += stmt.executeUpdate(updates[i]);
            }
        } finally {
            closeStatements(stmt);
        }

        return rowsEffected;
    }

    /**
     * Closes all given {@link Connection}s and logs a warning message if an error occurs while closing. <code>
     * null</code> objects are ignored.
     *
     * @param  cons  sets the statements to close
     */
    public static void closeConnections(final Connection... cons) {
        if (cons != null) {
            for (final Connection con : cons) {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    LOG.warn("could not close connection: " + con, e); // NOI18N
                }
            }
        }
    }

    /**
     * Closes all given {@link ResultSet}s and logs a warning message if an error occurs while closing. <code>
     * null</code> objects are ignored.
     *
     * @param  sets  the statements to close
     */
    public static void closeResultSets(final ResultSet... sets) {
        if (sets != null) {
            for (final ResultSet set : sets) {
                try {
                    if (set != null) {
                        set.close();
                    }
                } catch (final SQLException e) {
                    LOG.warn("could not close resultset: " + set, e); // NOI18N
                }
            }
        }
    }

    /**
     * Closes all given {@link Statement}s and logs a warning message if an error occurs while closing. <code>
     * null</code> objects are ignored.
     *
     * @param  stmts  the statements to close
     */
    public static void closeStatements(final Statement... stmts) {
        if (stmts != null) {
            for (final Statement stmt : stmts) {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    LOG.warn("could not close statement: " + stmt, e); // NOI18N
                }
            }
        }
    }

    /**
     * Calls to this operation will to nothing since retries are not supported.
     *
     * @param  noOfRetries  DOCUMENT ME!
     */
    @Override
    public void setRetriesOnError(final int noOfRetries) {
        // ignore
    }

    /**
     * Always returns <code>0</code> as retries are not supported.
     *
     * @return  always <code>0</code>
     */
    @Override
    public int getRetriesOnError() {
        return 0;
    }

    /**
     * Simply calls {@link #close()}.
     *
     * @throws  ServerExitError  never
     */
    @Override
    public void shutdown() throws ServerExitError {
        close();
    }

    /**
     * Returns the current closed state of the connection.
     *
     * @return  true if the connection is closed, false otherwise
     */
    public boolean isClosed() {
        try {
            rwLock.readLock().lock();

            return isClosed;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Simply calls {@link #isClosed()}.
     *
     * @return  true if the connection is closed, false otherwise
     *
     * @see     #isClosed()
     */
    @Override
    public boolean isDown() {
        return isClosed();
    }
}
