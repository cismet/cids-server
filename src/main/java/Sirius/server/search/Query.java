/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Query.java
 *
 * Created on 25. September 2003, 11:55
 */
package Sirius.server.search;

import Sirius.server.search.searchparameter.SearchParameter;
import Sirius.server.sql.SystemStatement;

import Sirius.util.Mapable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class Query implements Mapable, java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    /** identifies this query in the system. */
    protected QueryIdentifier qid;

    // will be set druring Execution
    protected boolean isExecuted = false;

    // this indicates whether the query produces a union over entities of different domoainservers
    // in this case no statement is needed on the root level
    protected boolean isUnionQuery = false;

    /** contains all subqueries. */
    protected Vector subQueries = new Vector(2);

    protected boolean isRoot;

    protected boolean isUpdate;

    // protected java.lang.Class resultType;
    protected int resultType;

    // used if a query is instatiatiated in the server/ set by the copy constructor
    protected String statement = null;

    protected boolean isBatch;

    /** parameters for this level of the query. */
    private Map<Object, List<SearchParameter>> parameters;

    // logging
    // transient
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private boolean conjunction = false;

    private boolean search = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Query.
     *
     * @param  qid  DOCUMENT ME!
     */
    public Query(final QueryIdentifier qid) {
        this.qid = qid;
        this.parameters = null;
        this.isRoot = true;
        this.isUpdate = false;
        this.resultType = 1;
        this.isBatch = false;
    }

    /**
     * Creates a new Query object.
     *
     * @param  stmnt   DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public Query(final SystemStatement stmnt, final String domain) {
        this.qid = new QueryIdentifier(domain, stmnt.getID(), stmnt.getName(), stmnt.getDescription());
        this.parameters = stmnt.getParameters();
        this.isRoot = stmnt.isRoot();
        this.isUpdate = stmnt.isUpdate();
        this.resultType = stmnt.getResultType();
        this.statement = stmnt.getStatement();
        this.isBatch = stmnt.isBatch();
        this.isUnionQuery = stmnt.isUnion();
        this.conjunction = stmnt.isConjunction();
        this.search = stmnt.isSearch();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public QueryIdentifier getQueryIdentifier() {
        return qid;
    }

    /**
     * Getter for property parameters.
     *
     * @return  Value of property parameters.
     */
    public Map<Object, List<SearchParameter>> getParameters() {
        return parameters;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List getParameterList() {
        final ArrayList l = new ArrayList();

        // collection contains syncedlinkedlists
        final Collection c = parameters.values();

        final Iterator iter = c.iterator();

        while (iter.hasNext()) {
            l.addAll((Collection)iter.next());
        }

        return l;
    }

    /**
     * Getter for property subQueries.
     *
     * @return  Value of property subQueries.
     */
    public Query[] getSubQueries() {
        return (Query[])this.subQueries.toArray(new Query[subQueries.size()]);
    }

    /**
     * Setter for property subQueries.
     *
     * @param  subQueries  New value of property subQueries.
     */
    public void setSubQueries(final Vector subQueries) {
        this.subQueries = subQueries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  q  DOCUMENT ME!
     */
    public void addSubQuery(final Query q) {
        subQueries.add(q);
    }

    /**
     * no cycle testing //////////////////////////////////////////////////////////////////////
     *
     * @param   qid  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Query getSubQuery(final QueryIdentifier qid) {
        if (subQueries != null) {
            for (int i = 0; i < subQueries.size(); i++) {
                if (((Query)subQueries.get(i)).getQueryIdentifier().equals(qid)) {
                    return (Query)subQueries.get(i);
                } else {
                    return ((Query)subQueries.get(i)).getSubQuery(qid);
                }
            }
        }

        return null;
    }
    /**
     * returns null if not found no cycle checking.
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List getParameter(final Object key) {
        if ((parameters != null) && parameters.containsKey(key)) {
            return (List)parameters.get(key);
        } else {
            for (int i = 0; i < subQueries.size(); i++) {
                final List value = ((Query)subQueries.get(i)).getParameter(key);

                if (value != null) {
                    return value;
                }
            }
        }

        return Collections.synchronizedList(new ArrayList(0));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parameter  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void setParameter(final SearchParameter parameter) throws Exception {
        // xxx
        final List params = (List)getParameter(parameter.getKey());

        final Iterator iter = params.iterator();
        while (iter.hasNext()) {
            final SearchParameter p = (SearchParameter)iter.next();

            if (p != null) {
                p.setValue(parameter.getValue());
            } else {
                throw new Exception("Parameter " + parameter.getKey() + " in query " + qid + " not existent"); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key    DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void setParameter(final Object key, final Object value) throws Exception {
        // xxx
        final List params = (List)getParameter(key);

        final Iterator iter = params.iterator();
        while (iter.hasNext()) {
            final SearchParameter p = (SearchParameter)iter.next();

            if (p != null) {
                p.setValue(value);
            } else {
                throw new Exception("Parameter " + key + " in query " + qid + " not existent"); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parameter  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addParameter(final SearchParameter parameter) throws Exception {
        final Object key = parameter.getKey();
        List<SearchParameter> params = parameters.get(key);
        if (params == null) {
            params = Collections.synchronizedList(new ArrayList<SearchParameter>());
            parameters.put(key, params);
        }

        if (!params.contains(parameter)) {
            params.add(parameter);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Iterator getParameterKeys() {
        final Set keys = new HashSet(parameters.size());

        // recursivly filling the set
        getParameterKeys(keys);

        return keys.iterator();
    }

    /////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param  keys  DOCUMENT ME!
     */
    private void getParameterKeys(final Set keys) {
        final Set k = parameters.keySet();

        if (k != null) {
            keys.addAll(k);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isExecutable() {
        final Iterator iter = getParameterKeys();

        while (iter.hasNext()) {
            SearchParameter ref = null;

            ref = (SearchParameter)getParameter(iter.next());

            // value not set and no result of a subQuery (to be set during execution)
            if ((ref.getValue() == null) && !ref.isQueryResult()) {
                logger.error("SearchParameter Value not set :" + ref.getKey()); // NOI18N
                return false;
            }
        }

        return true;
    }

    /**
     * Getter for property isExecuted.
     *
     * @return  Value of property isExecuted.
     */
    public boolean isExecuted() {
        return isExecuted;
    }

    /**
     * Setter for property isExecuted.
     *
     * @param  isExecuted  New value of property isExecuted.
     */
    public void setIsExecuted(final boolean isExecuted) {
        this.isExecuted = isExecuted;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isLeaf() {
        return (subQueries.size() == 0);
    }

    /**
     * Getter for property isUnionQuery.
     *
     * @return  Value of property isUnionQuery.
     */
    public boolean isUnionQuery() {
        return isUnionQuery;
    }

    /**
     * Setter for property isUnionQuery.
     *
     * @param  isUnionQuery  New value of property isUnionQuery.
     */
    public void setIsUnionQuery(final boolean isUnionQuery) {
        this.isUnionQuery = isUnionQuery;
    }

    /**
     * Getter for property isRoot.
     *
     * @return  Value of property isRoot.
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Setter for property isRoot.
     *
     * @param  isRoot  New value of property isRoot.
     */
    public void setIsRoot(final boolean isRoot) {
        this.isRoot = isRoot;
    }

    /**
     * Getter for property isUpdate.
     *
     * @return  Value of property isUpdate.
     */
    public boolean isUpdate() {
        return isUpdate;
    }

    /**
     * Setter for property isUpdate.
     *
     * @param  isUpdate  New value of property isUpdate.
     */
    public void setIsUpdate(final boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSingleQuery() {
        return isRoot && isLeaf();
    }

    /**
     * Setter for property parameters.
     *
     * @param  parameters  New value of property parameters.
     */
    public void setParameters(final Map<Object, List<SearchParameter>> parameters) {
        this.parameters = parameters;
    }

    /**
     * Getter for property resultType.
     *
     * @return  Value of property resultType.
     */
    public int getResultType() {
        return resultType;
    }

    /**
     * Setter for property resultType.
     *
     * @param  resultType  New value of property resultType.
     */
    public void setResultType(final int resultType) {
        this.resultType = resultType;
    }

    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof Query) {
            return m.getKey();
        } else {
            return null;
        }
    }

    @Override
    public Object getKey() {
        return qid.getKey();
    }

    @Override
    public String toString() {
        return getKey().toString() + "\n parameter :: \n" + parameters; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStatement() {
        return statement;
    }

    /**
     * Getter for property isBatch.
     *
     * @return  Value of property isBatch.
     */
    public boolean isBatch() {
        return isBatch;
    }

    /**
     * Setter for property isBatch.
     *
     * @param  isBatch  New value of property isBatch.
     */
    public void setIsBatch(final boolean isBatch) {
        this.isBatch = isBatch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isConjunction() {
        return conjunction;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conjunction  DOCUMENT ME!
     */
    public void setConjunction(final boolean conjunction) {
        this.conjunction = conjunction;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSearch() {
        return search;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  search  DOCUMENT ME!
     */
    public void setSearch(final boolean search) {
        this.search = search;
    }
}
