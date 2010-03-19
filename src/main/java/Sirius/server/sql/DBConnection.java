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

import de.cismet.tools.Sorter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

/**
 * Datenbankverbindung.<BR>
 *
 * @author   Sascha Schlobinski
 * @version  1.0 erstellt am 05.10.1999
 * @since    DOCUMENT ME!
 */

public class DBConnection {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DBConnection.class);

    //~ Instance fields --------------------------------------------------------

    protected final DBClassifier dbc;

    private Connection con;
    private StatementCache cache;

    //~ Constructors -----------------------------------------------------------

    /**
     * /////////////////////////////////////
     *
     * @param   dbc  DOCUMENT ME!
     *
     * @throws  Throwable        DOCUMENT ME!
     * @throws  ServerExitError  DOCUMENT ME!
     */
    protected DBConnection(DBClassifier dbc) throws Throwable {
        this.dbc = dbc;

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("driver  :" + dbc.driver);
            }

            Class.forName(dbc.driver); // can raise an ClassNotFoundExc.

            con = DriverManager.getConnection(dbc.url, dbc.login, dbc.pwd); // can raise an SQl EXc.

            if (dbc.driver.equals("org.postgresql.Driver")) {
                ((org.postgresql.PGConnection)con).addDataType("geometry", "org.postgis.PGgeometry");
                ((org.postgresql.PGConnection)con).addDataType("box3d", "org.postgis.PGbox3d");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("postgis datatypes added to connection");
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("connection established to " + this.dbc);
            }

            cache = new StatementCache(con);
        } catch (java.lang.ClassNotFoundException e) {
            LOG.error("<LS> ERROR :: " + e.getMessage() + " Driver Not Found", e);
            throw new ServerExitError(" Driver Not Found", e);
        } catch (java.sql.SQLException e) {
            ExceptionHandler.handle(e);
            LOG.error("<LS> ERROR :: could not connect to " + dbc, e);
            throw new ServerExitError(" could not connect to db", e);
        } catch (java.lang.Exception e) {
            LOG.error("<LS> ERROR :: " + e.getMessage(), e);
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
    public static boolean charToBool(char bool) {
        return ((bool == (byte)'T') || (bool == (byte)'t'));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bool  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean stringToBool(String bool) {
        if ((bool == null) || (bool.length() == 0)) {
            return false;
        } else {
            return charToBool(bool.charAt(0));
        }
    }

    //////////////////////////////////////

    /**
     * Der momentan angemeldete DBUser.<BR>
     *
     * @return    java.lang.String User
     *
     * @exeption  DOCUMENT ME!
     */

    public final String getUser() {
        return dbc.login;
    }

    ///////////////////////////////////////

    /**
     * Das Passwort des momentan angemeldete DBUsers.<BR>
     *
     * @return    java.lang.String passwd
     *
     * @exeption  DOCUMENT ME!
     */

    public final String getPassword() {
        return dbc.pwd;
    }

    /**
     * url des jdbc-Drivers.
     *
     * @return  DOCUMENT ME!
     */
    public final String getURL() {
        return dbc.url;
    }

    /**
     * Klasse des jdbc-Drivers.
     *
     * @return  DOCUMENT ME!
     */
    public final String getDriver() {
        return dbc.driver;
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
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException              DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public ResultSet submitInternalQuery(final String descriptor,
            final Object... parameters) throws SQLException {
        try {
            final String stmt = NbBundle.getMessage(DBConnection.class, descriptor);
            final PreparedStatement ps = con.prepareStatement(stmt);
            if (parameters.length
                        != ps.getParameterMetaData().getParameterCount()) {
                final String message = "parameter count mismmatch for descriptor '" // NOI18N
                    + descriptor
                    + "', Statement: "                                              // NOI18N
                    + stmt
                    + ", Statement param count: "                                   // NOI18N
                    + ps.getParameterMetaData().getParameterCount()
                    + ", given param count: "                                       // NOI18N
                    + parameters.length;
                LOG.error(message);
                throw new IllegalArgumentException(message);
            }
            for (int i = 0; i < parameters.length; ++i) {
                ps.setObject(i + 1, parameters[i]);
            }
            return ps.executeQuery();
        } catch (final MissingResourceException e) {
            final String message = "invalid descriptor: " + descriptor;             // NOI18N
            LOG.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    ////////////////////////////////////////

    /**
     * Setzt das descriptor zugeordnete Statement (Select) ab.<BR>
     *
     * @param     descriptor  java.lang.String descriptor
     * @param     parameters  DOCUMENT ME!
     *
     * @return    java.sql.ResultSet
     *
     * @exeption  java.sql.SQLException
     */

    // public ResultSet submitQuery(String descriptor)
    // {
    //
    // try
    // {
    // String sqlStmnt = fetchStatement(descriptor);
    //
    // return (con.createStatement()).executeQuery(sqlStmnt);
    // }
    // catch (Exception e)
    // {
    // ExceptionHandler.handle(e);
    // }
    //
    // return null;
    //
    // }
    //
    ///////////////////////////////////////////////////////////////////////////////////

    public ResultSet submitQuery(String descriptor, java.lang.Object[] parameters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + descriptor);
        }

        try {
            String sqlStmnt = fetchStatement(descriptor);
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
            if (LOG.isDebugEnabled()) {
                LOG.debug("info :: " + sqlStmnt);
            }

            return (con.createStatement()).executeQuery(sqlStmnt);
        } catch (Exception e) {
            LOG.error(" Fehler bei SubmitQuery", e);
            ExceptionHandler.handle(e);
        }

        return null;
    }

    ///////////////////////////////////////////////

    /**
     * Setzt das Statement mit der id SqlID (Select) ab.<BR>
     *
     * @param     sqlID       int sqlID
     * @param     parameters  DOCUMENT ME!
     *
     * @return    java.sql.ResultSet
     *
     * @throws    java.sql.SQLException  DOCUMENT ME!
     * @throws    Exception              DOCUMENT ME!
     *
     * @exeption  java.sql.SQLException
     */

    // public ResultSet submitQuery(int sqlID) throws java.sql.SQLException,Exception
    // {
    //
    // String sqlStmnt = fetchStatement(sqlID);
    //
    // //System.out.println(sqlStmnt);
    // return (con.createStatement()).executeQuery(sqlStmnt);
    //
    // }

    ////////////////////////////////////////////////////

    public ResultSet submitQuery(int sqlID, java.lang.Object[] parameters) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + sqlID);
        }

        String sqlStmnt = fetchStatement(sqlID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt);
        }

        try {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statement :" + sqlStmnt);
        }
        return (con.createStatement()).executeQuery(sqlStmnt);
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////
     *
     * @param   q  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public ResultSet submitQuery(Query q) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitQuery: " + q.getKey() + ", batch: " + q.isBatch());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("query object :: " + q);
        }
        Collection tmp = q.getParameterList();

        Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);

        Sorter.quickSort(params);

        if (q.getQueryIdentifier().getName().equals("")) {
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
    public int submitUpdate(Query q) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + q.getKey() + ", batch: " + q.isBatch());
        }

        Collection tmp = q.getParameterList();
        Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);

        Sorter.quickSort(params);

        if (q.isBatch()) {
            if (q.getQueryIdentifier().getName().equals("")) {
                return submitUpdateBatch(q.getQueryIdentifier().getQueryId(), params);
            } else {
                return submitUpdateBatch(q.getQueryIdentifier().getName(), params);
            }
        } else {
            if (q.getQueryIdentifier().getName().equals("")) {
                return submitUpdate(q.getQueryIdentifier().getQueryId(), params);
            } else {
                return submitUpdate(q.getQueryIdentifier().getName(), params);
            }
        }
    }

    ////////////////////////////////////////////

    /**
     * Setzt das descriptor zugeordnete Statement (Update|Insert|Delete) ab.<BR>
     *
     * @param     descriptor  java.lang.String descriptor
     * @param     parameters  DOCUMENT ME!
     *
     * @return    int
     *
     * @throws    java.sql.SQLException  DOCUMENT ME!
     * @throws    Exception              DOCUMENT ME!
     *
     * @exeption  java.sql.SQLException
     */

    // public int submitUpdate(String descriptor) throws java.sql.SQLException, Exception// returns abs(rows effected)
    // {
    //
    // String sqlStmnt = fetchStatement(descriptor);
    //
    //
    // return (con.createStatement()).executeUpdate(sqlStmnt);
    //
    //
    // }

    /////////////////////////////////////////////

    public int submitUpdate(String descriptor, java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception // returns abs(rows effected)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + descriptor);
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

    ///////////////////////////////////////////////////////////////////

    /**
     * Setzt das Statement mit der id sqlID (Update|Insert|Delete) ab.<BR>
     *
     * @param     sqlID       int sqlID
     * @param     parameters  DOCUMENT ME!
     *
     * @return    int rowsEffected
     *
     * @throws    java.sql.SQLException  DOCUMENT ME!
     * @throws    Exception              DOCUMENT ME!
     *
     * @exeption  java.sql.SQLException
     */

    // public int submitUpdate(int sqlID) throws java.sql.SQLException,Exception// returns abs(rows effected)
    // {
    //
    // String sqlStmnt = fetchStatement(sqlID);
    //
    // return (con.createStatement()).executeUpdate(sqlStmnt);
    //
    //
    // }

    ////////////////////////////////////////////////////////////

    public int submitUpdate(int sqlID, java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception // returns abs(rows effected)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdate: " + sqlID);
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

    /////////////////////////////////////////////////////////////////

    /**
     * Holt das descriptor zugeordnete Statement aus der Statement-Tabelle.<BR>
     *
     * @param     descriptor  java.lang.String descriptor
     *
     * @return    java.lang.String sqlStatement
     *
     * @throws    java.sql.SQLException  DOCUMENT ME!
     * @throws    Exception              DOCUMENT ME!
     *
     * @exeption  java.sql.SQLException
     */

    public String fetchStatement(String descriptor) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchStatement: " + descriptor);
        }
/*
if(!dbc.cacheStatements)
{
        char changes = 'F';

        if(effectsChanges)
        changes='T';


        String sqlStmnt = "SELECT statement from system_statement where id = " + cache.getStatement(descriptor).getID() +
                                   "and effect_Changes = '"+ changes +"' and number_of_param =" +parameterNum;

        ResultSet id = (con.createStatement()).executeQuery(sqlStmnt);

        id.next();
        return id.getString("Statement").trim();
}
 */

        if (cache.containsStatement(descriptor)) {
            return cache.getStatement(descriptor).getStatement();
        } else {
            return null;
        }

        // if(tmp.effectsChanges() == effectsChanges && tmp.getNumberOfParameters() == parameterNum)

        // throw new Exception("SystemStatement stimmt nicht mit den Aufrufoptionen \u00FCberein");
    }

    ///////////////////////////////////////////////////////////

    /**
     * Holt das Statement mit der id sqlID aus der Statement-Tabelle.<BR>
     *
     * @param     sqlID  int sqlID
     *
     * @return    java.lang.String sqlStatement
     *
     * @throws    java.sql.SQLException  DOCUMENT ME!
     * @throws    Exception              DOCUMENT ME!
     *
     * @exeption  java.sql.SQLException
     */

    public String fetchStatement(int sqlID) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchStatement: " + sqlID);
        }
/*
if(!dbc.cacheStatements)
{
        char changes = 'F';// Informix knows no boolean xxxxxxxxxxx

        if(effectsChanges)
        changes='T';


        String sqlStmnt = "SELECT statement from system_statement where (id = " + sqlID+ ") and (effect_Changes = '"+ changes +"') and (number_of_param =" +parameterNum+")";

        //System.out.println("Statement :" + sqlStmnt);


        ResultSet id = (con.createStatement()).executeQuery(sqlStmnt);

        id.next();
        return id.getString("Statement").trim();

}

 */
        if (cache.containsStatement(sqlID)) {
            return cache.getStatement(sqlID).getStatement();
        }

        // if(tmp.effectsChanges() == effectsChanges && tmp.getNumberOfParameters() == parameterNum)
        // return tmp.getStatement();
        //
        // throw new Exception("SystemStatement stimmt nicht mit den Aufrufoptionen \u00FCberein");
        return null;
    }
    ////////////////////////////////////////////

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
    public ResultSet executeQuery(Query q) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("executeQuery: " + q.getKey() + ", batch: " + q.isBatch());
        }

        if (q.getStatement() == null) { // sql aus dem cache
            return submitQuery(q);
        } else {
            // nimm statement as is
            String sqlStmnt = q.getStatement();

            try {
                Collection tmp = q.getParameterList();
                Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
                Sorter.quickSort(params);
                sqlStmnt = QueryParametrizer.parametrize(sqlStmnt, params);
            } catch (Exception e) {
                LOG.error(e);
                throw e;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("INFO executeQuery :: " + sqlStmnt);
            }
            return (con.createStatement()).executeQuery(sqlStmnt);
        }
    }

    /**
     * /////////////////////
     *
     * @param   qid         DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     * @throws  Exception              DOCUMENT ME!
     */
    public int submitUpdateBatch(int qid, java.lang.Object[] parameters) throws java.sql.SQLException, Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + qid);
        }

        String updateBatch = fetchStatement(qid);

        try {
            updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";");

        String[] updates = new String[tokenizer.countTokens()];

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
    public int submitUpdateBatch(String queryname, java.lang.Object[] parameters) throws java.sql.SQLException,
        Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("submitUpdateBatch: " + queryname);
        }

        String updateBatch = fetchStatement(queryname);

        try {
            updateBatch = QueryParametrizer.parametrize(updateBatch, parameters);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

        StringTokenizer tokenizer = new StringTokenizer(updateBatch, ";");

        String[] updates = new String[tokenizer.countTokens()];

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
                    LOG.warn("could not close resultset: " + set, e);
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
                    LOG.warn("could not close statement: " + stmt, e);
                }
            }
        }
    }
}
