/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryExecuter.java
 *
 * Created on 29. Oktober 2003, 11:10
 */
package Sirius.server.search;

import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryExecuter {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(QueryExecuter.class);

    //~ Instance fields --------------------------------------------------------

    // conatins references to all local servers available
    private Map activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryExecuter.
     *
     * @param  activeLocalServers  DOCUMENT ME!
     */
    public QueryExecuter(final Map activeLocalServers) {
        this.activeLocalServers = activeLocalServers;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   u         DOCUMENT ME!
     * @param   classIds  DOCUMENT ME!
     * @param   q         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SearchResult executeQuery(final User u, final String[] classIds, final Query q) throws Exception {
        final Sirius.server.middleware.interfaces.domainserver.SearchService s =
            (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(
                q.getQueryIdentifier().getDomain());
        q.isExecuted();

        if (s == null) {
            LOG.error(
                "query for ls " // NOI18N
                        + q.getQueryIdentifier().getDomain()
                        + " not possible as server is not online"); // NOI18N
            return new SearchResult(new MetaObjectNode[0]);
        }

//        if(logger.isDebugEnabled())
//        {
//            for(int i =0;i<classIds.length;i++)
//            {
//                logger.debug("classIds inkl Domain");
//                logger.debug(classIds[i]+",");
//            }
//        }

        final int[] cIds = parseClassIds(classIds, q.getQueryIdentifier().getDomain());

        // keine ausgew\u00E4hlte Klasse
        if ((cIds != null) && (cIds.length == 1) && (cIds[0] == -1)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No class on this local server selected. For this reason return"); // NOI18N
            }
            return new SearchResult(new MetaObjectNode[0]);
        }

//        if(logger.isDebugEnabled())
//        {
//            for(int i =0;i<cIds.length;i++)
//            {
//                logger.debug(cIds[i]+",");
//            }
//        }

        // if(cIds.length>0)
        // return ((SearchResult)s.search(u,cIds,q));
        // else
        // return new SearchResult(new Sirius.server.middleware.types.Node[0]);

        return s.search(u, cIds, q);
    }
    /**
     * such classIds bzgl. ls raus
     *
     * @param   classIds  DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int[] parseClassIds(final String[] classIds, final String domain) {
        if (classIds == null) {
            return new int[0];
        }

        final ArrayList v = new ArrayList(classIds.length);

        // add default class_id = -1 to ensure that the sql statement will be correct
        v.add(new Integer(-1));

        for (int i = 0; i < classIds.length; i++) {
            final String[] res = classIds[i].split("@"); // NOI18N

            // secon part contains domain
            if (res[1].equals(domain)) {
                v.add(new Integer(res[0]));
            }
        }

        v.trimToSize();
        if (LOG.isDebugEnabled()) {
            LOG.debug("classids " + v + "for domain" + domain); // NOI18N
        }

        final int[] result = new int[v.size()];
        final Iterator iter = v.iterator();
        int i = 0;
        while (iter.hasNext()) {
            result[i++] = ((Integer)iter.next()).intValue();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean serviceAvailable(final String domain) {
        return activeLocalServers.get(domain) != null;
    }
}