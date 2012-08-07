/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @author   mscholl
 * @version  $Revision$, $Date$
 */

// TODO: the cache should be a singleton or at least an instance that is not initialised for every connection that is
// created, as query support may be discontinued in the near future, the refactoring may become redundant
public class StatementCache {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(StatementCache.class);

    //~ Instance fields --------------------------------------------------------

    /** contains all cached objects. */
    private Map<Integer, SystemStatement> statements; // holds Statements referenced by their IDs
    private Map<String, Integer> nameAssociatesID;    // holds ids of statements referenced by statement names

    //~ Constructors -----------------------------------------------------------

    /**
     * -----------------------------------
     *
     * @param  con  DOCUMENT ME!
     */
    StatementCache(final Connection con) {
        statements = new HashMap<Integer, SystemStatement>(30);
        nameAssociatesID = new HashMap<String, Integer>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("before load Queries");                            // NOI18N
        }
        try {
            final String queriesThere = "select count(*) from cs_query"; // NOI18N

            final String queryStmnt = "SELECT * from cs_query"; // NOI18N

            final String paramStmnt = "SELECT * from cs_query_parameter"; // NOI18N

            Statement stmt = null;
            ResultSet queryTest = null;

            int queryNo = 0;
            try {
                stmt = con.createStatement();
                queryTest = stmt.executeQuery(queriesThere);
                queryNo = 0;

                if (queryTest.next()) {
                    queryNo = queryTest.getInt(1);
                }
            } finally {
                DBConnection.closeResultSets(queryTest);
                DBConnection.closeStatements(stmt);
            }

            if (queryNo == 0) {
                final String message = "<LS> ERROR :: no system statemnts in cs_query"; // NOI18N
                LOG.error(message);
                throw new IllegalStateException(message);
            }

            stmt = null;
            ResultSet stmntTable = null;

            try {
                stmt = con.createStatement();
                stmntTable = stmt.executeQuery(queryStmnt);
                while (stmntTable.next())                             // add all objects to the hashtable
                {
                    final SystemStatement tmp = new SystemStatement(
                            stmntTable.getBoolean("is_root"),         // NOI18N
                            stmntTable.getInt("id"),                  // NOI18N
                            stmntTable.getString("name").trim(),      // NOI18N
                            stmntTable.getBoolean("is_update"),       // NOI18N
                            stmntTable.getBoolean("is_batch"),        // NOI18N
                            stmntTable.getInt("result"),              // NOI18N
                            stmntTable.getString("statement").trim(), // NOI18N
                            stmntTable.getString("descr"));           // NOI18N
                    boolean conjunction = false;
                    tmp.setUnion(stmntTable.getBoolean("is_union"));  // NOI18N
                    try {
                        // logger.debug("conjunction vom Typ "+stmntTable.getObject("conjunction").getClass());

                        conjunction = stmntTable.getBoolean("conjunction");                                // getBoolean buggy??//NOI18N
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("conjunction before the setting" + conjunction);                     // NOI18N
                        }
                        tmp.setConjunction(conjunction);
                    } catch (final SQLException ex) {
                        LOG.error("is_conjunction not supported! Please update your  query schema!!", ex); // NOI18N
                        tmp.setConjunction(false);                                                         // standardverhalten
                    }
                    try {
                        tmp.setSearch(stmntTable.getBoolean("is_search"));                                 // NOI18N
                    } catch (final SQLException ex) {
                        LOG.error("is_search is not available -> update of the meta database", ex);        // NOI18N
                        tmp.setSearch(false);
                    }

                    statements.put(tmp.getID(), tmp);
                    nameAssociatesID.put(tmp.getName(), tmp.getID());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            "cached statement :"          // NOI18N
                                    + tmp.getName()
                                    + " changes ?"        // NOI18N
                                    + tmp.getStatement()
                                    + " conjuction ??"    // NOI18N
                                    + tmp.isConjunction()
                                    + "conjunctionresult" // NOI18N
                                    + conjunction);
                    }
                }
            } finally {
                DBConnection.closeResultSets(stmntTable);
                DBConnection.closeStatements(stmt);
            }
            // end while
            if (LOG.isDebugEnabled()) {
                LOG.debug("statement hash elements #" + statements.size() + " elements" + statements); // NOI18N
            }

            stmt = null;
            ResultSet paramTable = null;

            try {
                stmt = con.createStatement();
                paramTable = stmt.executeQuery(paramStmnt);

                int query_id = 0;

                while (paramTable.next()) {
                    SystemStatement s = null;
                    query_id = paramTable.getInt("query_id"); // NOI18N
                    s = statements.get(query_id);
                    // xxx new Searchparameter
                }
            } finally {
                DBConnection.closeResultSets(paramTable);
                DBConnection.closeStatements(stmt);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Queries loaded from the database"); // NOI18N
            }
        } catch (final Exception e) {
            LOG.error("Exception thile loading the query", e); // NOI18N
            ExceptionHandler.handle(e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected SystemStatement getStatement(final int id) {
        return statements.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SystemStatement getStatement(final String name) {
        return statements.get(nameAssociatesID.get(name));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SystemStatement> values() {
        return statements.values();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int size() {
        return statements.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsStatement(final int id) {
        return statements.containsKey(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsStatement(final String key) {
        return nameAssociatesID.containsKey(key);
    }
} // end of class statement cache
