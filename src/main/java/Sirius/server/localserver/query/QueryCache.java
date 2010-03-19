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
import Sirius.server.sql.*;

import java.util.*;

import Sirius.server.search.*;

import java.sql.*;

import Sirius.server.search.searchparameter.*;

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
    public QueryCache(DBConnection dbcon, String domain) {
        this(dbcon.getConnection(), dbcon.getStatementCache(), domain);
    }

    /**
     * Creates a new instance of QueryCache.
     *
     * @param  con     DOCUMENT ME!
     * @param  cache   DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public QueryCache(Connection con, StatementCache cache, String domain) {
        this.con = con;
        queries = new HashMap(cache.size());

        // to large but doesn't matter
        searchOptions = new HashMap(cache.size());

        Collection qs = cache.values();

        Iterator iter = qs.iterator();

        while (iter.hasNext()) {
            Query q = new Query((SystemStatement)iter.next(), domain);

            queries.put(q.getKey(), q);

            if (logger.isDebugEnabled()) {
                logger.debug("query added " + q);
            }

            if (q.isRoot() && q.isSearch()) {
                searchOptions.put(q.getKey(), new SearchOption(q));
                if (logger.isDebugEnabled()) {
                    logger.debug("query added " + q + " ROOT");
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
            maxQueryId = con.prepareStatement("select max(id) from cs_query");

            maxParameterId = con.prepareStatement("select max(id) from cs_query_parameter");
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
    private void setClassPermissions(String domain) {
        // verkn\u00FCpfung der Queries
        try {
            Statement getQueryClassAssoc = con.createStatement();

            ResultSet rs = getQueryClassAssoc.executeQuery("SELECT * from cs_query_class_assoc");

            int q_id = 0;
            int c_id = 0;
            while (rs.next()) {
                q_id = rs.getInt("query_id");
                c_id = rs.getInt("class_id");

                Object option = searchOptions.get(q_id + "@" + domain);

                if (option != null) {
                    ((SearchOption)option).addClass(c_id + "@" + domain);
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
    private void setUserGroupPermissions(String domain) {
        // verkn\u00FCpfung der Queries
        try {
            Statement getQueryClassAssoc = con.createStatement();

            ResultSet rs = getQueryClassAssoc.executeQuery(
                    "SELECT d.name,query_id,ug_id from cs_query_ug_assoc as uga ,cs_domain as d , cs_ug as ug where uga.ug_id=ug.id and d.id=ug.domain");

            // Vorsicht die Id ist nicht Systemweit eindeutig statt ug_id muss ug_name verwendet werden
            // dazu suche wo werden query permissions abgefragt
            // checken wie der Abfrageschl\u00FCssel gebaut wird

            int q_id = 0;
            int ug_id = 0;
            String ug_domain = "";

            while (rs.next()) {
                ug_domain = rs.getString("name");
                if (ug_domain == null) {
                    ;
                }
                ug_domain = domain;

                q_id = rs.getInt("query_id");
                ug_id = rs.getInt("ug_id");

                Object option = searchOptions.get(q_id + "@" + domain);

                if (option != null) {
                    ((SearchOption)option).addUserGroup(ug_id + "@" + ug_domain);
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
    private void linkIt(String domain) {
        // verkn\u00FCpfung der Queries
        try {
            Statement getLinks = con.createStatement();

            ResultSet rs = getLinks.executeQuery(
                    "SELECT id_from,id_to,d.name as domain_to,qfrom.name as fromName,qto.name as toName from cs_query_link as l,cs_query as qfrom,cs_query as qto,cs_domain as d where l.id_from = qfrom.id and l.id_to=qto.id and d.id=l.domain_to");

            int from = 0;
            int to = 0;
            String nameFrom = "";
            String nameTo = "";

            while (rs.next()) {
                String domainTo = null;
                Query q = null;

                from = rs.getInt("id_from");
                to = rs.getInt("id_to");
                domainTo = rs.getString("domain_to");
                nameFrom = rs.getString("fromName");
                nameTo = rs.getString("toName");

                String qKey = from + "@" + domain;

                // xxx ls_name
                q = (Query)queries.get(nameFrom + "@" + domain);
                if (logger.isDebugEnabled()) {
                    logger.debug("query linked" + q);
                }

                Query subQ = null;
                // keine Queryreferenzierung auf einen anderen Server
                if ((domainTo == null) || domainTo.equals("LOCAL")) {
                    // subQ=(Query)queries.get(to+"@"+domain);
                    subQ = (Query)queries.get(nameTo + "@" + domain);
                    q.addSubQuery(subQ);
                } else // add QueryRumpf (referenziert anderen ls
                {
                    // vorsicht die Parameter fehlen
                    // daher setzte parameter der superQuery
                    Query father = (Query)queries.get(nameFrom + "@" + this.domain);

                    subQ = new Query(new QueryIdentifier(domainTo, to));
                    subQ.setParameters(father.getParameters());
                    q.addSubQuery(subQ);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Subquery :: " + subQ);
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
    private void setSearchParameters(String domain) {
        try {
            Statement getSearchParameter = con.createStatement();

            ResultSet rs = getSearchParameter.executeQuery(
                    "SELECT qp.id as p_id,q.id as q_id,qp.param_key as p_key,qp.is_query_result as is_result,qp.query_position as pos,q.name as query_name, qp.descr as query_description from cs_query_parameter as qp,cs_query as q where q.id=qp.query_id");

            int id = 0;
            int q_id = 0;
            boolean isQueryResult = false;
            int pos = 0;

            String queryKey = "";
            String queryDescription = "";
            while (rs.next()) {
                String key = null;

                id = rs.getInt("p_id");
                q_id = rs.getInt("q_id");
                key = rs.getString("p_key").trim();
                isQueryResult = rs.getBoolean("is_result");

                pos = rs.getInt("pos");
                queryKey = rs.getString("query_name").trim();
                queryDescription = rs.getString("query_description");

                Query q = (Query)queries.get(queryKey + "@" + domain);

                if (q != null) {
                    q.addParameter(new DefaultSearchParameter(key, null, isQueryResult, pos, queryDescription));
                }
            }
        } catch (Exception e) {
            logger.error("search will not work queryparameters could not be set", e);
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
    public Query getQuery(QueryIdentifier qid) {
        return (Query)queries.get(qid);
    }

    /**
     * ///////////////////////////////////////////////////////////////
     *
     * @param   qid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SearchOption getSearchOption(QueryIdentifier qid) {
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
    public int addQuery(
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws Exception {
        Statement s = con.createStatement();
        int maxId = 0;
        ResultSet max = maxQueryId.executeQuery();

        if (max.next()) {
            maxId = max.getInt(1) + 1;
        }

        String stmnt = "insert into cs_query values(" + maxId + ",'" + name + "','" + description + "','" + statement
            + "'," + resultType + ",'" + isUpdate + "','" + isBatch + "','" + isRoot + "','" + isUnion
            + "', false,true )";

        Statement insert = con.createStatement();
        if (logger.isDebugEnabled()) {
            logger.debug("add query info :" + stmnt);
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
    public int addQuery(String name, String description, String statement) throws Exception {
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
    public boolean addQueryParameter(
            int queryId,
            int typeId,
            String paramKey,
            String description,
            char isQueryResult,
            int queryPosition) throws Exception {
        Statement s = con.createStatement();
        int maxId = 0;
        ResultSet max = maxParameterId.executeQuery();

        if (max.next()) {
            maxId = max.getInt(1) + 1;
        }

        String stmnt = "insert into cs_query_parameter values(" + maxId + "," + queryId + ",'" + paramKey + "','"
            + description + "','" + isQueryResult + "'," + typeId + "," + queryPosition + ")";
        if (logger.isDebugEnabled()) {
            logger.debug("add queryparam  info :" + stmnt);
        }
        Statement insert = con.createStatement();

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
    public boolean addQueryParameter(int queryId, String paramkey, String description) throws Exception {
        return addQueryParameter(queryId, 0, paramkey, description, 'F', 0);
    }
}
