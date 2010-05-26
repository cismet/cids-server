/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SearchServiceImpl.java
 *
 * Created on 23. November 2003, 14:39
 */
package Sirius.server.middleware.impls.proxy;
import Sirius.server.*;
import Sirius.server.middleware.types.*;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.*;
import Sirius.server.search.*;

import java.rmi.*;

import java.util.*;
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
            logger.debug("getSearchOptions searchService gerufen User:: " + user);
        }
        // umstelen auf activelocalservrs sp\u00E4ter
        final Server[] localServers = nameServer.getServers(ServerType.LOCALSERVER);
        final HashMap result = new HashMap();

        String serverName = null;
        for (int i = 0; i < localServers.length; i++) {
            serverName = localServers[i].getName();
            if (logger.isDebugEnabled()) {
                logger.debug("localserver Suchoptionen " + serverName + " searchoptions ::");
            }

            final HashMap options = getSearchOptions(user, serverName);
            if (logger.isDebugEnabled()) {
                logger.debug("localserver Suchoptionen " + serverName + " searchoptions ::" + options);
            }

            result.putAll(options);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Search Options ::" + result);
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
            logger.debug("getSearchOptions searchService gerufen User:: " + user + " domain::" + domain);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain))
                    .getSearchOptions(user);
    }

    /**
     * public Sirius.server.search.SearchResult search(User user, String[] classIds, Sirius.server.search.SearchOption[]
     * searchOptions) throws RemoteException { SearchResult v = new SearchResult(new Node[0]); // //fehlt
     * \u00FCberpr\u00FCfung ob Typ tats\u00E4chlich passt //alle Queries m\u00FCssen einen Typ liefern //sp\u00E4ter //
     * Query q = null; try { for(int i =0;i<searchOptions.length;i++) { q = searchOptions[i].getQuery();
     * v.addAll((SearchResult) qex.executeQuery(user,classIds,q)); } }catch(Exception e) {System.err.println(e);throw
     * new RemoteException(e.getMessage(),e);} return v; } permissions muessen hier gepr\u00FCft werden nicht nur die
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
            logger.debug("addQuery searchService gerufen User:: " + user + " queryName ::" + name);
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
            logger.debug("addQuery searchService gerufen User:: " + user + " queryName ::" + name);
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
                "addQueryParameter searchService gerufen User:: "
                + user
                + " queryId ::"
                + queryId
                + " paramKey::"
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
                "addQueryParameter searchService gerufen User:: "
                + user
                + " queryId ::"
                + queryId
                + " paramKey::"
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
            logger.debug("search in searchService gerufen User:: " + user);
        }

        final SearchResult v = new SearchResult(new MetaObjectNode[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("Queryplaner instnziiert");
        }

        final QueryPlaner qp = new QueryPlaner(classIds, searchOptions);
        if (logger.isDebugEnabled()) {
            logger.debug("Querypl\u00E4ne abgerufen");
        }

        final Collection<ArrayList<QueryConfiguration>> qps = qp.getQueryPlans();
        if (logger.isDebugEnabled()) {
            logger.debug("Querypl\u00E4ne abgerufen" + qps);
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
            logger.debug("search in searchService gerufen User:: " + user);
        }

        final SearchResult v = new SearchResult(new MetaObjectNode[0]);

        final QueryConfiguration[] qcs = (QueryConfiguration[])qList.toArray(new QueryConfiguration[qList.size()]);

        Query q = null;

        try {
            for (int i = 0; i < qcs.length; i++) {
                q = qcs[i].getQuery();

                final String[] classIds = qcs[i].getClassIds();

                // debugtext
                String deb = "";
                for (int it = 0; it < classIds.length; it++) {
                    deb += classIds[it] + "\n";
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("classids fro query" + q + " are" + deb);
                }

                final HashMap params = q.getParameters();
                if (logger.isDebugEnabled()) {
                    logger.debug("Parameter ::" + q + " isconjunction??" + q.isConjunction());
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

                // wenn union hole alle subqueries und f\u00FCge diese zu den ausgef\u00FChrten queries hinzu
                // if(q.isUnionQuery()) { Query[] sqs = q.getSubQueries();
                //
                //
                // for(int j =0;j<sqs.length;j++) { sqs[j].setParameters(params); logger.debug("Subquery"+sqs[j]);
                // SearchResult s = null; try { if(qex.serviceAvailable(sqs[j].getQueryIdentifier().getDomain() )) s =
                // (SearchResult) qex.executeQuery(user,qcs[i].getClassIds(),sqs[j]);
                //
                // if(s==null||(s.isEmpty()&&q.isConjunction()))// wenn server nicht da \u00FCberspringen continue;
                //
                // if(sqs[j].isConjunction()) {
                //
                // if(!v.retainerSet()) { if(v.getResult()!=null&&((HashSet)v.getResult()).size()>0)
                // v.setRetainer(v.intersect((HashSet)s.getResult(),(HashSet)v.getResult()));// setze retainer mit
                // suchergebnis else v.setRetainer((HashSet)s.getResult()); } else { if (!q.isConjunction())
                // v.setRetainer( v.intersect(v.getRetainer(),(HashSet)s.getResult() )); // schnittmenge 2er retainer
                // else { HashSet h = (HashSet)s.getResult(); h.addAll(v.getRetainer()); v.setRetainer(h);  } } }
                // v.addAll(s); } catch(Exception e) { logger.error(e,e); }   } }

            }
        } catch (Exception e) {
            logger.error(e, e);
        }

        return v;
    }
}
