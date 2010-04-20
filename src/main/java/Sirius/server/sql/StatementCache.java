/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StatementCache.java
 *
 * Created on 22. November 2003, 09:54
 */
package Sirius.server.sql;
import de.cismet.tools.collections.*;

import java.sql.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class StatementCache {

    //~ Instance fields --------------------------------------------------------

    /** contains all cached objects. */
    protected StatementMap statements;                                       // holds Statements referenced by their IDs
    protected HashMap<java.lang.String, java.lang.Integer> nameAssociatesID; // holds ids of statements referenced by
                                                                             // statement names

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * -----------------------------------
     *
     * @param  con  DOCUMENT ME!
     */
    StatementCache(Connection con) {
        statements = new StatementMap(50); // allocation of the hashtable
        nameAssociatesID = new HashMap();
        if (logger.isDebugEnabled()) {
            logger.debug("before load Queries");//NOI18N
        }
        try {
            String queriesThere = "select count(*) from cs_query";//NOI18N

            String queryStmnt = "SELECT * from cs_query"; // "SELECT * from cs_query q,cs_java_class c where result =//NOI18N
                                                          // c.id";

            String paramStmnt = "SELECT * from cs_query_parameter";//NOI18N

            ResultSet queryTest = (con.createStatement()).executeQuery(queriesThere);
            int queryNo = 0;

            if (queryTest.next()) {
                queryNo = queryTest.getInt(1);
            }

            if (queryNo == 0) {
                logger.error("<LS> ERROR :: no system statemnts in cs_query ");//NOI18N
                throw new Exception("<LS> ERROR :: no system statemnts in cs_query");//NOI18N
            }

            ResultSet stmntTable = (con.createStatement()).executeQuery(queryStmnt);

            while (stmntTable.next()) // add all objects to the hashtable
            {
                SystemStatement tmp = new SystemStatement(
                        stmntTable.getBoolean("is_root"),//NOI18N
                        stmntTable.getInt("id"),//NOI18N
                        stmntTable.getString("name").trim(),//NOI18N
                        stmntTable.getBoolean("is_update"),//NOI18N
                        stmntTable.getBoolean("is_batch"),//NOI18N
                        stmntTable.getInt("result"),//NOI18N
                        stmntTable.getString("statement").trim(),//NOI18N
                        stmntTable.getString("descr"));//NOI18N
                boolean conjunction = false;
                tmp.setUnion(stmntTable.getBoolean("is_union"));//NOI18N
                try {
                    // logger.debug("conjunction vom Typ "+stmntTable.getObject("conjunction").getClass());

                    conjunction = stmntTable.getBoolean("conjunction"); // getBoolean buggy??//NOI18N
                    if (logger.isDebugEnabled()) {
                        logger.debug("conjunction before the setting" + conjunction);//NOI18N
                    }
                    tmp.setConjunction(conjunction);
                } catch (SQLException ex) {
                    logger.error("is_conjunction not supported! Please update your  query schema!!", ex);//NOI18N
                    tmp.setConjunction(false);                          // standardverhalten
                }
                try {
                    tmp.setSearch(stmntTable.getBoolean("is_search"));//NOI18N
                } catch (SQLException ex) {
                    logger.error("is_search is not available -> update of the meta database", ex);//NOI18N
                }

                statements.add(tmp.getID(), tmp);
                nameAssociatesID.put(tmp.getName(), tmp.getID());
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "cached statement :" + tmp.getName() + " changes ?" + tmp.getStatement() + " conjuction ??"//NOI18N
                        + tmp.isConjunction() + "conjunctionresult" + conjunction);//NOI18N
                }
            } // end while
            if (logger.isDebugEnabled()) {
                logger.debug("statement hash elements #" + statements.size() + " elements" + statements);//NOI18N
            }

            ResultSet paramTable = (con.createStatement()).executeQuery(paramStmnt);

            int query_id = 0;

            while (paramTable.next()) {
                SystemStatement s = null;
                query_id = paramTable.getInt("query_id");//NOI18N
                s = statements.getStatement(query_id);
                // xxx new Searchparameter
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Queries loaded from the database");//NOI18N
            }
        } catch (java.lang.Exception e) {
            logger.error("Exception thile loading the query", e);//NOI18N
            ExceptionHandler.handle(e);
        }
    } // end of constructor

    //~ Methods ----------------------------------------------------------------

    /**
     * -----------------------------------------------------------------------------
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected SystemStatement getStatement(int id) throws Exception {
        return statements.getStatement(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SystemStatement getStatement(String name) throws Exception {
        return statements.getStatement(nameAssociatesID.get(name));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection values() {
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
    public boolean containsStatement(int id) {
        return statements.containsIntKey(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean containsStatement(String key) {
        return nameAssociatesID.containsKey(key);
    }
} // end of class statement cache
