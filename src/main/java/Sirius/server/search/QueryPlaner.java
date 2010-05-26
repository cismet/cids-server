/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryPlaner.java
 *
 * Created on 31. Oktober 2006, 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ordnet die SuchOptionen nach Ls und deren classIds.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryPlaner {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            QueryPlaner.class);

    //~ Instance fields --------------------------------------------------------

    HashMap<String, ArrayList<QueryConfiguration>> queryPlansPerDomain =
        new HashMap<String, ArrayList<QueryConfiguration>>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryPlaner.
     *
     * @param  classIds       DOCUMENT ME!
     * @param  searchOptions  DOCUMENT ME!
     */
    public QueryPlaner(final String[] classIds, final SearchOption[] searchOptions) {
        final HashSet<String> domainList = extractAllDomains(convertSearchOptions(searchOptions));

        setSubqueryParameters(convertSearchOptions(searchOptions));
        if (logger.isDebugEnabled()) {
            logger.debug("List der Query domains aufgestellt" + domainList);
        }

        final String[] domains = (String[])domainList.toArray(new String[domainList.size()]);

        // query
        for (int i = 0; i < domains.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("construct queryplan for domain " + domains[i] + "of # of domains :" + domains.length);
            }

            final String[] cIds = filterClassIdsForDomain(domains[i], classIds);
            if (logger.isDebugEnabled()) {
                logger.debug("classids f\u00FCr domain" + domains[i] + " ids:" + cIds);
            }

            final ArrayList<Query> qs = extractQueriesForDomain(domains[i], convertSearchOptions(searchOptions));

            final Iterator<Query> iter = qs.iterator();

            final ArrayList<QueryConfiguration> qcList = new ArrayList<QueryConfiguration>(qs.size());

            while (iter.hasNext()) {
                qcList.add(new QueryConfiguration(iter.next(), cIds));
            }

            queryPlansPerDomain.put(domains[i], qcList);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String extractDomainFromClassId(final String classId) {
        if (!checkClassId(classId)) {
            logger.error("improper classid has to be of the form: digit@domain");
            return null;
        }

        return classId.split("@")[1];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   qId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String extractDomainFromQueryId(final String qId) {
        if (!checkQueryId(qId)) {
            logger.error("improper classid has to be of the form: query@domain");
            return null;
        }

        return qId.split("@")[1];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int extractClassId(final String classId) {
        if (!checkClassId(classId)) {
            logger.error("improper classid has to be of the form: digit@domain");
            return -1;
        }

        return new Integer(classId.split("@")[0]).intValue();
    }
    /**
     * DOCUMENT ME!
     *
     * @param   qId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int extractQueryId(final String qId) {
        if (!checkQueryId(qId)) {
            logger.error("improper queryid has to be of the form: quid@domain");
            return -1;
        }

        return new Integer(qId.split("@")[0]).intValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean checkClassId(final String classId) {
        // classid@domain

        final String regex = "[0-9]+[@][^@]+"; // digit of arbitray length + @ +arbitrary not @

        final Pattern p = Pattern.compile(regex);

        final Matcher m = p.matcher(classId);

        return m.matches();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   qId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean checkQueryId(final String qId) {
        // classid@domain

        final String regex = "[^@]+[@][^@]+"; // non at of arbitray length + @ +arbitrary not @

        final Pattern p = Pattern.compile(regex);

        final Matcher m = p.matcher(qId);

        return m.matches();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain    DOCUMENT ME!
     * @param   classIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] filterClassIdsForDomain(final String domain, final String[] classIds) {
        final ArrayList v = new ArrayList(classIds.length);

        for (int i = 0; i < classIds.length; i++) {
            final String cdomain = extractDomainFromClassId(classIds[i]);
            if (logger.isDebugEnabled()) {
                logger.debug("domain aus class id " + classIds[i] + " extrahiert" + cdomain);
            }

            if (domain.equals(cdomain)) {
                v.add(classIds[i]);
                if (logger.isDebugEnabled()) {
                    logger.debug(classIds[i] + " zu den klassids der domain" + domain + "hinzugef\u00FCgt");
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug(cdomain + "of classId:: " + classIds[i] + " ::does not match domain " + domain);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(" classids for domain" + domain + " are" + v);
        }
        return (String[])v.toArray(new String[v.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   qs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    HashSet<String> extractAllDomains(final Query[] qs) {
        final HashSet<String> domains = new HashSet<String>(qs.length);

        for (int i = 0; i < qs.length; i++) {
            domains.add(qs[i].getQueryIdentifier().getDomain());

            final Query[] subs = qs[i].getSubQueries();

            if (subs != null) {
                domains.addAll(extractAllDomains(subs));
            }
        }

        return domains;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain   DOCUMENT ME!
     * @param   queries  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ArrayList<Query> extractQueriesForDomain(final String domain, final Query[] queries) {
        final ArrayList<Query> queryList = new ArrayList<Query>(queries.length + 5);

        for (int i = 0; i < queries.length; i++) {
            if (queries[i].getQueryIdentifier().getDomain().equals(domain)) {
                final Query q = queries[i];

                queryList.add(q);
            }

            if (queries[i].isUnionQuery()) {
                queryList.addAll(extractQueriesForDomain(domain, queries[i].getSubQueries()));
            }
        }

        return queryList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   so  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Query searchOption2Query(final SearchOption so) {
        return so.getQuery();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sos  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Query[] convertSearchOptions(final SearchOption[] sos) {
        final ArrayList<Query> qs = new ArrayList<Query>(sos.length + 5);

        for (int i = 0; i < sos.length; i++) {
            qs.add(sos[i].getQuery());
        }

        return (Query[])qs.toArray(new Query[qs.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<ArrayList<QueryConfiguration>> getQueryPlans() {
        return queryPlansPerDomain.values();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  qs  DOCUMENT ME!
     */
    public void setSubqueryParameters(final Query[] qs) {
        for (int i = 0; i < qs.length; i++) {
            final Query[] subs = qs[i].getSubQueries();

            if (subs != null) {
                for (int j = 0; j < subs.length; j++) {
                    subs[j].setParameters(qs[i].getParameters());
                }

                setSubqueryParameters(subs);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final String t = "444444444444444444@oppp";

        System.out.println(extractDomainFromClassId(t));
    }
}
