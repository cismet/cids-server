/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryCache.java
 *
 * Created on 22. November 2003, 11:06
 */
package Sirius.server.localserver.query;
import Sirius.server.search.*;
import Sirius.server.search.searchparameter.*;
import Sirius.server.sql.*;

import java.sql.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryCache {

    //~ Instance fields --------------------------------------------------------

    String domain;

    HashMap queries;

    // querie + permission Hashsets
    HashMap searchOptions;

    Connection con;

    PreparedStatement maxQueryId;

    PreparedStatement maxParameterId;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryCache object.
     *
     * @param  dbcon   DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public QueryCache(final DBConnection dbcon, final String domain) {
        this(dbcon.getConnection(), dbcon.getStatementCache(), domain);
    }

    /**
     * Creates a new instance of QueryCache.
     *
     * @param  con     DOCUMENT ME!
     * @param  cache   DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public QueryCache(final Connection con, final StatementCache cache, final String domain) {
        this.con = con;
        queries = new HashMap(cache.size());

        // to large but doesn't matter
        searchOptions = new HashMap(cache.size());

        final Collection qs = cache.values();

        final Iterator iter = qs.iterator();

        while (iter.hasNext()) {
            final Query q = new Query((SystemStatement)iter.next(), domain);

            queries.put(q.getKey(), q);

            if (logger.isDebugEnabled()) {
                logger.debug("query added " + q);   // NOI18N
            }

            if (q.isRoot() && q.isSearch()) {
                searchOptions.put(q.getKey(), new SearchOption(q));
                if (logger.isDebugEnabled()) {
                    logger.debug("query added " + q + " ROOT");   // NOI18N
                }
            }
//            if(logger.isDebugEnabled())
//                logger.debug("searchOptions constructed :: "+new Vector(searchOptions.values()));
        }

        this.domain = domain;

        setSearchParameters(domain);

        // build Query Graph
        linkIt(domain);

        // add class permissions to search options
        setClassPermissions(domain);

        // add ug permissions to search options
        setUserGroupPermissions(domain);

        try {
            maxQueryId = con.prepareStatement("select max(id) from cs_query");   // NOI18N

            maxParameterId = con.prepareStatement("select max(id) from cs_query_parameter");   // NOI18N
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }
    }

    ////////////////////////////////////////////////////////////////

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    private void setClassPermissions(final String domain) {
        // verkn\u00FCpfung der Queries
        try {
            final Statement getQueryClassAssoc = con.createStatement();

            final ResultSet rs = getQueryClassAssoc.executeQuery("SELECT * from cs_query_class_assoc");//NOI18N

            int q_id = 0;
            int c_id = 0;
            while (rs.next()) {
                q_id = rs.getInt("query_id");   // NOI18N
                c_id = rs.getInt("class_id");   // NOI18N

                final Object option = searchOptions.get(q_id + "@" + domain);//NOI18N

                if (option != null) {
                    ((SearchOption)option).addClass(c_id + "@" + domain);   // NOI18N
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
    ////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    private void setUserGroupPermissions(final String domain) {
        // verkn\u00FCpfung der Queries
        try {
            final Statement getQueryClassAssoc = con.createStatement();

            final ResultSet rs = getQueryClassAssoc.executeQuery(
                    "SELECT d.name,query_id,ug_id from cs_query_ug_assoc as uga ,cs_domain as d , cs_ug as ug where uga.ug_id=ug.id and d.id=ug.domain");   // NOI18N

            // Vorsicht die Id ist nicht Systemweit eindeutig statt ug_id muss ug_name verwendet werden
            // dazu suche wo werden query permissions abgefragt
            // checken wie der Abfrageschl\u00FCssel gebaut wird

            int q_id = 0;
            int ug_id = 0;
            String ug_domain = "";   // NOI18N

            while (rs.next()) {
                ug_domain = rs.getString("name");   // NOI18N
                if (ug_domain == null) {
                    ;
                }
                ug_domain = domain;

                q_id = rs.getInt("query_id");   // NOI18N
                ug_id = rs.getInt("ug_id");   // NOI18N

                final Object option = searchOptions.get(q_id + "@" + domain);//NOI18N

                if (option != null) {
                    ((SearchOption)option).addUserGroup(ug_id + "@" + ug_domain);   // NOI18N
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
    ////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    private void linkIt(final String domain) {
        // verkn\u00FCpfung der Queries
        try {
            final Statement getLinks = con.createStatement();

            final ResultSet rs = getLinks.executeQuery(
                    "SELECT id_from,id_to,d.name as domain_to,qfrom.name as fromName,qto.name as toName from cs_query_link as l,cs_query as qfrom,cs_query as qto,cs_domain as d where l.id_from = qfrom.id and l.id_to=qto.id and d.id=l.domain_to");   // NOI18N

            int from = 0;
            int to = 0;
            String nameFrom = "";   // NOI18N
            String nameTo = "";   // NOI18N

            while (rs.next()) {
                String domainTo = null;
                Query q = null;

                from = rs.getInt("id_from");   // NOI18N
                to = rs.getInt("id_to");   // NOI18N
                domainTo = rs.getString("domain_to");   // NOI18N
                nameFrom = rs.getString("fromName");   // NOI18N
                nameTo = rs.getString("toName");   // NOI18N

                final String qKey = from + "@" + domain;//NOI18N

                // xxx ls_name
                q = (Query)queries.get(nameFrom + "@" + domain);   // NOI18N
                if (logger.isDebugEnabled()) {
                    logger.debug("query linked" + q);   // NOI18N
                }

                Query subQ = null;
                // keine Queryreferenzierung auf einen anderen Server
                if ((domainTo == null) || domainTo.equals("LOCAL")) {   // NOI18N
                    // subQ=(Query)queries.get(to+"@"+domain);
                    subQ = (Query)queries.get(nameTo + "@" + domain);   // NOI18N
                    q.addSubQuery(subQ);
                } else // add QueryRumpf (referenziert anderen ls
                {
                    // vorsicht die Parameter fehlen
                    // daher setzte parameter der superQuery
                    final Query father = (Query)queries.get(nameFrom + "@" + this.domain);//NOI18N

                    subQ = new Query(new QueryIdentifier(domainTo, to));
                    subQ.setParameters(father.getParameters());
                    q.addSubQuery(subQ);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Subquery :: " + subQ);   // NOI18N
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    private void setSearchParameters(final String domain) {
        try {
            final Statement getSearchParameter = con.createStatement();

            final ResultSet rs = getSearchParameter.executeQuery(
                    "SELECT qp.id as p_id,q.id as q_id,qp.param_key as p_key,qp.is_query_result as is_result,qp.query_position as pos,q.name as query_name, qp.descr as query_description from cs_query_parameter as qp,cs_query as q where q.id=qp.query_id");   // NOI18N

            int id = 0;
            int q_id = 0;
            boolean isQueryResult = false;
            int pos = 0;

            String queryKey = "";   // NOI18N
            String queryDescription = "";   // NOI18N
            while (rs.next()) {
                String key = null;

                id = rs.getInt("p_id");   // NOI18N
                q_id = rs.getInt("q_id");   // NOI18N
                key = rs.getString("p_key").trim();   // NOI18N
                isQueryResult = rs.getBoolean("is_result");   // NOI18N

                pos = rs.getInt("pos");   // NOI18N
                queryKey = rs.getString("query_name").trim();   // NOI18N
                queryDescription = rs.getString("query_description");   // NOI18N

                final Query q = (Query)queries.get(queryKey + "@" + domain);//NOI18N

                if (q != null) {
                    q.addParameter(new DefaultSearchParameter(key, null, isQueryResult, pos, queryDescription));
                }
            }
        } catch (Exception e) {
            logger.error("search will not work queryparameters could not be set", e);   // NOI18N
            ExceptionHandler.handle(e);
        }
    }

    /**
     * /////////////////////////////////////////////////////////////////
     *
     * @param   qid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Query getQuery(final QueryIdentifier qid) {
        return (Query)queries.get(qid);
    }

    /**
     * ///////////////////////////////////////////////////////////////
     *
     * @param   qid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SearchOption getSearchOption(final QueryIdentifier qid) {
        return (SearchOption)searchOptions.get(qid);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap getSearchOptions() {
        return searchOptions;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     * @param   resultType   DOCUMENT ME!
     * @param   isUpdate     DOCUMENT ME!
     * @param   isBatch      DOCUMENT ME!
     * @param   isRoot       DOCUMENT ME!
     * @param   isUnion      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public int addQuery(final String name,
            final String description,
            final String statement,
            final int resultType,
            final char isUpdate,
            final char isBatch,
            final char isRoot,
            final char isUnion) throws Exception {
        final Statement s = con.createStatement();
        int maxId = 0;
        final ResultSet max = maxQueryId.executeQuery();

        if (max.next()) {
            maxId = max.getInt(1) + 1;
        }

        final String stmnt = "insert into cs_query values(" + maxId + ",'" + name + "','" + description + "','"//NOI18N
                    + statement
                    + "'," + resultType + ",'" + isUpdate + "','" + isBatch + "','" + isRoot + "','" + isUnion //NOI18N
                    + "', false,true )"; //NOI18N

        final Statement insert = con.createStatement();
        if (logger.isDebugEnabled()) {
            logger.debug("add query info :" + stmnt);   // NOI18N
        }

        if (insert.executeUpdate(stmnt) > 0) {
            return maxId;
        } else {
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public int addQuery(final String name, final String description, final String statement) throws Exception {
        return addQuery(name, description, statement, 0, 'F', 'F', 'T', 'F');
    }

    /**
     * DOCUMENT ME!
     *
     * @param   queryId        DOCUMENT ME!
     * @param   typeId         DOCUMENT ME!
     * @param   paramKey       DOCUMENT ME!
     * @param   description    DOCUMENT ME!
     * @param   isQueryResult  DOCUMENT ME!
     * @param   queryPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean addQueryParameter(final int queryId,
            final int typeId,
            final String paramKey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws Exception {
        final Statement s = con.createStatement();
        int maxId = 0;
        final ResultSet max = maxParameterId.executeQuery();

        if (max.next()) {
            maxId = max.getInt(1) + 1;
        }

        final String stmnt = "insert into cs_query_parameter values(" + maxId + "," + queryId + ",'" + paramKey + "','" //NOI18N
                    + description + "','" + isQueryResult + "'," + typeId + "," + queryPosition + ")"; //NOI18N
        if (logger.isDebugEnabled()) {
            logger.debug("add queryparam  info :" + stmnt);   // NOI18N
        }
        final Statement insert = con.createStatement();

        return insert.executeUpdate(stmnt) > 0;
    }
    /**
     * position set in order of the addition.
     *
     * @param   queryId      DOCUMENT ME!
     * @param   paramkey     DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean addQueryParameter(final int queryId, final String paramkey, final String description)
            throws Exception {
        return addQueryParameter(queryId, 0, paramkey, description, 'F', 0);
    }
}
