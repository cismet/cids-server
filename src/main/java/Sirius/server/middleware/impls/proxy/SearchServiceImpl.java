/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.Server;
import Sirius.server.ServerType;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.UserService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.search.Query;
import Sirius.server.search.QueryConfiguration;
import Sirius.server.search.QueryExecuter;
import Sirius.server.search.QueryPlaner;
import Sirius.server.search.SearchResult;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.cismet.cids.server.search.CidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class SearchServiceImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    // resolves Query tree
    private QueryExecuter qex;
    private java.util.Hashtable activeLocalServers;
    private NameServer nameServer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryStoreImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SearchServiceImpl(final java.util.Hashtable activeLocalServers, final NameServer nameServer)
            throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;

        qex = new QueryExecuter(activeLocalServers);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * public java.util.Collection getAllSearchOptions(User user) throws RemoteException { return
     * getSearchOptions(user).values(); } public java.util.Collection getAllSearchOptions(User user, String domain)
     * throws RemoteException { return getSearchOptions(user,domain).values(); }.
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public java.util.HashMap getSearchOptions(final User user) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getSearchOptions searchService gerufen User:: " + user); // NOI18N
        }
        // umstelen auf activelocalservrs sp\u00E4ter
        final Server[] localServers = nameServer.getServers(ServerType.LOCALSERVER);
        final HashMap result = new HashMap();

        String serverName = null;
        for (int i = 0; i < localServers.length; i++) {
            serverName = localServers[i].getName();
            if (logger.isDebugEnabled()) {
                logger.debug("localserver Suchoptionen " + serverName + " searchoptions ::"); // NOI18N
            }

            final HashMap options = getSearchOptions(user, serverName);
            if (logger.isDebugEnabled()) {
                logger.debug("localserver Suchoptionen " + serverName + " searchoptions ::" + options); // NOI18N
            }

            result.putAll(options);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Search Options ::" + result); // NOI18N
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user    DOCUMENT ME!
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public java.util.HashMap getSearchOptions(final User user, final String domain) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getSearchOptions searchService called User:: " + user + " domain::" + domain); // NOI18N
        }
        return ((Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain))
                    .getSearchOptions(user);
    }

    /**
     * public Sirius.server.search.SearchResult search(User user, String[] classIds, Sirius.server.search.SearchOption[]
     * searchOptions) throws RemoteException { SearchResult v = new SearchResult(new Node[0]); // //fehlt
     * \u00FCberpr\u00FCfung ob Typ tats\u00E4chlich passt //alle Queries m\u00FCssen einen Typ liefern //sp\u00E4ter //
     * <code>
     * Query q = null; try { for(int i =0;i&lt;searchOptions.length;i++) { q = searchOptions[i].getQuery();
     * v.addAll((SearchResult) qex.executeQuery(user,classIds,q)); } }catch(Exception e) {System.err.println(e);throw
     * new RemoteException(e.getMessage(),e);} return v; }
     * </code> 
     * permissions muessen hier gepr\u00FCft werden nicht nur die
     * Domain abfragen.
     *
     * @param   user         DOCUMENT ME!
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
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int addQuery(final User user,
            final String name,
            final String description,
            final String statement,
            final int resultType,
            final char isUpdate,
            final char isBatch,
            final char isRoot,
            final char isUnion) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addQuery searchService called User:: " + user + " queryName ::" + name); // NOI18N
        }
        final String domain = user.getDomain();
        final Sirius.server.middleware.interfaces.domainserver.SearchService s =
            (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);

        return s.addQuery(name, description, statement, resultType, isUpdate, isBatch, isRoot, isUnion);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public int addQuery(final User user, final String name, final String description, final String statement)
            throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("addQuery searchService called User:: " + user + " queryName ::" + name); // NOI18N
        }

        final String domain = user.getDomain();
        final Sirius.server.middleware.interfaces.domainserver.SearchService s =
            (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);

        return s.addQuery(name, description, statement);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   queryId        DOCUMENT ME!
     * @param   typeId         DOCUMENT ME!
     * @param   paramkey       DOCUMENT ME!
     * @param   description    DOCUMENT ME!
     * @param   isQueryResult  DOCUMENT ME!
     * @param   queryPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean addQueryParameter(final User user,
            final int queryId,
            final int typeId,
            final String paramkey,
            final String description,
            final char isQueryResult,
            final int queryPosition) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "addQueryParameter searchService called User:: "
                        + user
                        + " queryId ::"
                        + queryId
                        + " paramKey::" // NOI18N
                        + paramkey);
        }
        final String domain = user.getDomain();
        final Sirius.server.middleware.interfaces.domainserver.SearchService s =
            (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);
        return s.addQueryParameter(queryId, typeId, paramkey, description, isQueryResult, queryPosition);
    }

    /**
     * position set in order of the addition.
     *
     * @param   user         DOCUMENT ME!
     * @param   queryId      DOCUMENT ME!
     * @param   paramkey     DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public boolean addQueryParameter(final User user,
            final int queryId,
            final String paramkey,
            final String description) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "addQueryParameter searchService called User:: "
                        + user
                        + " queryId ::"
                        + queryId
                        + " paramKey::" // NOI18N
                        + paramkey);
        }
        final String domain = user.getDomain();
        final Sirius.server.middleware.interfaces.domainserver.SearchService s =
            (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);

        return s.addQueryParameter(queryId, paramkey, description);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user           DOCUMENT ME!
     * @param   classIds       DOCUMENT ME!
     * @param   searchOptions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SearchResult search(final User user,
            final String[] classIds,
            final Sirius.server.search.SearchOption[] searchOptions) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("search in searchService called User:: " + user); // NOI18N
        }

        final SearchResult v = new SearchResult(new MetaObjectNode[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("Queryplanner intantiated"); // NOI18N
        }

        final QueryPlaner qp = new QueryPlaner(classIds, searchOptions);
        if (logger.isDebugEnabled()) {
            logger.debug("Queryplans retrieved"); // NOI18N
        }

        final Collection<ArrayList<QueryConfiguration>> qps = qp.getQueryPlans();
        if (logger.isDebugEnabled()) {
            logger.debug("Queryplans retrieved" + qps); // NOI18N
        }

        final Iterator<ArrayList<QueryConfiguration>> iter = qps.iterator();

        while (iter.hasNext()) {
            try {
                v.addAll(searchX(user, iter.next()));
            } catch (RemoteException ex) {
                logger.error(ex);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return v;
    }

    /**
     * fromer searchAdapted.
     *
     * @param   user   DOCUMENT ME!
     * @param   qList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SearchResult searchX(final User user, final ArrayList<QueryConfiguration> qList) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("search in searchService called User:: " + user); // NOI18N
        }

        final SearchResult v = new SearchResult(new MetaObjectNode[0]);

        final QueryConfiguration[] qcs = qList.toArray(new QueryConfiguration[qList.size()]);

        Query q = null;

        try {
            for (int i = 0; i < qcs.length; i++) {
                q = qcs[i].getQuery();

                final String[] classIds = qcs[i].getClassIds();

                // debugtext
                String deb = "";                                           // NOI18N
                for (int it = 0; it < classIds.length; it++) {
                    deb += classIds[it] + "\n";                            // NOI18N
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("classids fro query" + q + " are" + deb); // NOI18N
                }

                final HashMap params = q.getParameters();
                if (logger.isDebugEnabled()) {
                    logger.debug("Parameter ::" + q + " isconjunction??" + q.isConjunction()); // NOI18N
                }

                final SearchResult result = (SearchResult)qex.executeQuery(user, classIds, q);

                if (q.isConjunction()) {
                    if (!v.retainerSet()) {
                        if ((v.getResult() != null) && (((HashSet)v.getResult()).size() > 0)) {
                            v.setRetainer(v.intersect((HashSet)result.getResult(), (HashSet)v.getResult())); // setze retainer mit suchergebnis
                        } else {
                            v.setRetainer((HashSet)result.getResult());
                        }
                    } else {
                        v.setRetainer(v.intersect(v.getRetainer(), (HashSet)result.getResult()));            // schnittmenge 2er retainer
                    }
                }

                v.addAll(result);
            }
        } catch (final Exception e) {
            logger.error(e, e);
        }

        return v;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   serverSearch  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Collection customServerSearch(final User user, final CidsServerSearch serverSearch) throws RemoteException {
        serverSearch.setUser(user);
        serverSearch.setActiveLocalServers(new HashMap(activeLocalServers));
        try {
            final Collection searchResults = serverSearch.performServerSearch();
            addDynamicChildren(user, searchResults);
            return searchResults;
        } catch (Exception e) {
            logger.error("Error in customSearch", e);
            throw new RemoteException("Error in customSearch", e);
        }
    }

    /**
     * Adds the dynamic children to the search results, if there are any assigned. Dynamic children will only be
     * assigned to MetaObjectNode objects
     *
     * @param  user           the user
     * @param  searchResults  the collection with the results of the search
     */
    private void addDynamicChildren(final User user, final Collection searchResults) {
        // caches the dynamic children due to performance reasons
        final HashMap<String, String> dynChildCache = new HashMap<String, String>();

        for (final Object tmp : searchResults) {
            if (tmp instanceof MetaObjectNode) {
                final MetaObjectNode node = (MetaObjectNode)tmp;
                final String key = node.getDomain() + ";" + node.getClassId();

                if (dynChildCache.containsKey(key)) {
                    // the dynamic children is contained in the cache
                    String dynamicChildrenStatement = dynChildCache.get(key);

                    if (dynamicChildrenStatement != null) {
                        // replace the object id wildcard with the object id. This wildcard can be used within the
                        // dynamic children sql statements
                        dynamicChildrenStatement = dynamicChildrenStatement.replace((CharSequence)"<object_id>",
                                (CharSequence)String.valueOf(node.getObjectId()));
                        node.setDynamicChildrenStatement(dynamicChildrenStatement);
                        node.setDynamic(true);
                        node.setLeaf(node.getDynamicChildrenStatement() == null);
                    }
                } else {
                    try {
                        // the dynamic children is not contained in the cache
                        final MetaClass cl = ((MetaService)activeLocalServers.get(node.getDomain())).getClass(
                                user,
                                node.getClassId());
                        ClassAttribute dynChild = cl.getClassAttribute("searchHit_dynamicChildren");
                        final ClassAttribute attribute = cl.getClassAttribute("searchHit_dynamicChildrenAttribute");
                        boolean hasAttribute = false;
                        String value = null;

                        if (attribute != null) {
                            value = ((UserService)activeLocalServers.get(node.getDomain())).getConfigAttr(
                                    user,
                                    (String)attribute.getValue());

                            if (value != null) {
                                hasAttribute = true;
                            }
                        }

                        if (hasAttribute) {
                            final ClassAttribute otherDynChild = cl.getClassAttribute(value);

                            if (otherDynChild != null) {
                                dynChild = otherDynChild;
                            }
                        }

                        if ((dynChild != null) && ((attribute == null) || hasAttribute)) {
                            String sqlText = (String)dynChild.getValue();
                            dynChildCache.put(key, sqlText);

                            // replace the object id wildcard with the object id. This wildcard can be used within the
                            // dynamic children sql statements
                            sqlText = sqlText.replace((CharSequence)"<object_id>",
                                    (CharSequence)String.valueOf(node.getObjectId()));
                            node.setDynamicChildrenStatement(sqlText);
                            node.setDynamic(true);
                            node.setLeaf(node.getDynamicChildrenStatement() == null);
                        } else {
                            dynChildCache.put(key, node.getDynamicChildrenStatement());
                        }
                    } catch (RemoteException e) {
                        logger.error("Error while trying to add the dynamic children to the search results.", e);
                    }
                }
            }
        }
    }
}
