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

import de.cismet.tools.Sorter;

/**
 * Datenbankverbindung.<BR>
 *
 * @author   Sascha Schlobinski
 * @version  1.0 erstellt am 05.10.1999
 * @since    DOCUMENT ME!
 */

public final class DBConnection {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DBConnection.class);

    public static final String DESC_VERIFY_USER_PW = "verify_user_password";                                   // NOI18N
    public static final String DESC_FETCH_DOMAIN_ID_FROM_DOMAIN_STRING = "fetch_domain_id_from_domain_string"; // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_KEY_ID = "fetch_config_attr_key_id";                     // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_USER_VALUE = "fetch_config_attr_user_value";             // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_UG_VALUE = "fetch_config_attr_ug_value";                 // NOI18N
    public static final String DESC_FETCH_CONFIG_ATTR_DOMAIN_VALUE = "fetch_config_attr_domain_value";         // NOI18N
    public static final String DESC_FETCH_HISTORY = "fetch_history";                                           // NOI18N
    public static final String DESC_FETCH_HISTORY_LIMIT = "fetch_history_limit";                               // NOI18N
    public static final String DESC_INSERT_HISTORY_ENTRY = "insert_history_entry";                             // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected final DBClassifier dbc;

    private Connection con;
    private StatementCache cache;
    private final Map<String, PreparedStatement> internalQueries;

    //~ Constructors -----------------------------------------------------------

    /**
     * /////////////////////////////////////
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected DBConnection(final DBClassifier dbc) throws Throwable {
        this.dbc = dbc;
        internalQueries = new HashMap<String, PreparedStatement>(7, 0.8f);

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
        } catch (java.lang.ClassNotFoundException e) {
            LOG.error("<LS> ERROR :: " + e.getMessage() + " Driver Not Found", e); // NOI18N
            throw new ServerExitError(" Driver Not Found", e);                     // NOI18N
        } catch (java.sql.SQLException e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: could not connect to " + dbc, e);             // NOI18N
            throw new ServerExitError(" could not connect to db", e);              // NOI18N
        } catch (java.lang.Exception e) {
            LOG.error("<LS> ERROR :: " + e.getMessage(), e);                       // NOI18N
            throw new ServerExitError(e);
        }
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
    public Connection getConnection() {
        return con;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException              DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public ResultSet submitInternalQuery(final String descriptor, final Object... parameters) throws SQLException {
        try {
            return prepareQuery(descriptor, parameters).executeQuery();
        } catch (final MissingResourceException e) {
            final String message = "invalid descriptor: " + descriptor; // NOI18N
            LOG.error(message, e);
            throw new IllegalArgumentException(message, e);
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
     * @throws  SQLException              DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public int submitInternalUpdate(final String descriptor, final Object... parameters) throws SQLException {
        try {
            return prepareQuery(descriptor, parameters).executeUpdate();
        } catch (final MissingResourceException e) {
            final String message = "invalid descriptor: " + descriptor; // NOI18N
            LOG.error(message, e);
            throw new IllegalArgumentException(message, e);
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
     * @throws  SQLException              DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private PreparedStatement prepareQuery(final String descriptor, final Object... parameters) throws SQLException {
        if (!internalQueries.containsKey(descriptor)) {
            final String stmt = NbBundle.getMessage(DBConnection.class, descriptor);
            final PreparedStatement ps = con.prepareStatement(stmt);
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
                throw new IllegalArgumentException(message);
            }
        }

        final PreparedStatement ps = internalQueries.get(descriptor);
        for (int i = 0; i < parameters.length; ++i) {
            ps.setObject(i + 1, parameters[i]);
        }

        return ps;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ResultSet submitQuery(final String descriptor, final java.lang.Object[] parameters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + descriptor); // NOI18N
        }

        try {
            String sqlStmnt = fetchStatement(descriptor);
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
            if (LOG.isDebugEnabled()) {
                LOG.debug("info :: " + sqlStmnt); // NOI18N
            }

            return (con.createStatement()).executeQuery(sqlStmnt);
        } catch (Exception e) {
            LOG.error(" Error in SubmitQuery()", e); // NOI18N
            ExceptionHandler.handle(e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sqlID       DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public ResultSet submitQuery(final int sqlID, final java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + sqlID); // NOI18N
        }

        String sqlStmnt = fetchStatement(sqlID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt); // NOI18N
        }

        try {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt); // NOI18N
        }
        return (con.createStatement()).executeQuery(sqlStmnt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   q  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public ResultSet submitQuery(final Query q) throws java.sql.SQLException, Exception {
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
            return submitQuery(q.getQueryIdentifier().getQueryId(), params);
        } else {
            return submitQuery(q.getQueryIdentifier().getName(), params);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   q  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdate(final Query q) throws java.sql.SQLException, Exception {
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
                return submitUpdate(q.getQueryIdentifier().getQueryId(), params);
            } else {
                return submitUpdate(q.getQueryIdentifier().getName(), params);
            }
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
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdate(final String descriptor, final java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + descriptor); // NOI18N
        }

        String sqlStmnt = fetchStatement(descriptor);

        try {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        return (con.createStatement()).executeUpdate(sqlStmnt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sqlID       DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdate(final int sqlID, final java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + sqlID); // NOI18N
        }

        String sqlStmnt = fetchStatement(sqlID);

        try {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        return (con.createStatement()).executeUpdate(sqlStmnt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   descriptor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public String fetchStatement(final String descriptor) throws java.sql.SQLException, Exception {
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
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public String fetchStatement(final int sqlID) throws java.sql.SQLException, Exception {
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
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public ResultSet executeQuery(final Query q) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeQuery: " + q.getKey() + ", batch: " + q.isBatch()); // NOI18N
        }

        if (q.getStatement() == null) { // sql aus dem cache
            return submitQuery(q);
        } else {
            String sqlStmnt = q.getStatement();

            try {
                final Collection tmp = q.getParameterList();
                final Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
                Sorter.quickSort(params);
                sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, params);
            } catch (Exception e) {
                LOG.error(e);
                throw e;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("INFO executeQuery :: " + sqlStmnt); // NOI18N
            }
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
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdateBatch(final int qid, final java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + qid); // NOI18N
        }

        String updateBatch = fetchStatement(qid);

        try {
            updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        final StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";"); // NOI18N

        final String[] updates = new String[tokenizer.countTokens()];

        for (int i = 0; i < updates.length; i++) {
            updates[i] = tokenizer.nextToken();
        }

        int rowsEffected = 0;

        for (int i = 0; i < updates.length; i++) {
            rowsEffected += (con.createStatement()).executeUpdate(updates[i]);
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
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdateBatch(final String queryname, final java.lang.Object[] parameters)
            throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + queryname); // NOI18N
        }

        String updateBatch = fetchStatement(queryname);

        try {
            updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        final StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";"); // NOI18N

        final String[] updates = new String[tokenizer.countTokens()];

        for (int i = 0; i < updates.length; i++) {
            updates[i] = tokenizer.nextToken();
        }

        int rowsEffected = 0;

        for (int i = 0; i < updates.length; i++) {
            rowsEffected += (con.createStatement()).executeUpdate(updates[i]);
        }

        return rowsEffected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sets  DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @param  stmts  DOCUMENT ME!
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
}
