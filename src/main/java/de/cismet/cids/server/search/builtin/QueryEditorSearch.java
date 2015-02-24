/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class QueryEditorSearch extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final String rawQuery = "SELECT {2} AS classid, tbl.id AS objectid, c.stringrep FROM {0} tbl "
                + "LEFT OUTER JOIN cs_stringrepcache c "     // NOI18N
                + "       ON     ( "                         // NOI18N
                + "                     c.class_id ={2} "    // NOI18N
                + "              AND    c.object_id=tbl.id " // NOI18N
                + "              ) WHERE {1}";

    private static final String rawQueryPagination =
        "SELECT * FROM (SELECT {2} AS classid, tbl.id AS objectid, c.stringrep FROM {0} tbl "
                + "LEFT OUTER JOIN cs_stringrepcache c "     // NOI18N
                + "       ON     ( "                         // NOI18N
                + "                     c.class_id ={2} "    // NOI18N
                + "              AND    c.object_id=tbl.id " // NOI18N
                + "              ) WHERE {1}) AS sub LIMIT {3} OFFSET {4}";

    private static final transient Logger LOG = Logger.getLogger(QueryEditorSearch.class);

    //~ Instance fields --------------------------------------------------------

    private final String metaClass;
    private final String whereClause;
    private final int classId;
    private final String DOMAIN;
    private final int limit;
    private final int offset;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryEditorSearch object.
     *
     * @param  domain       DOCUMENT ME!
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     * @param  classId      DOCUMENT ME!
     */
    public QueryEditorSearch(final String domain, final String metaClass, final String whereClause, final int classId) {
        this.whereClause = whereClause;
        this.metaClass = metaClass;
        this.classId = classId;
        this.DOMAIN = domain;
        this.limit = -1;
        this.offset = -1;
    }

    /**
     * Creates a new QueryEditorSearch object.
     *
     * @param  domain       DOCUMENT ME!
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     * @param  classId      DOCUMENT ME!
     * @param  limit        DOCUMENT ME!
     * @param  offset       DOCUMENT ME!
     */
    public QueryEditorSearch(final String domain,
            final String metaClass,
            final String whereClause,
            final int classId,
            final int limit,
            final int offset) {
        this.whereClause = whereClause;
        this.metaClass = metaClass;
        this.classId = classId;
        this.DOMAIN = domain;
        this.limit = limit;
        this.offset = offset;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(DOMAIN);
        final ArrayList<MetaObjectNode> metaObjects = new ArrayList<MetaObjectNode>();
        if (ms != null) {
            try {
                final boolean paginationEnabled = limit
                            >= 0;
                final String query = (!paginationEnabled)
                    ? MessageFormat.format(rawQuery, metaClass, whereClause, classId)
                    : MessageFormat.format(rawQueryPagination, metaClass, whereClause, classId, limit, offset);
                LOG.info(query);
                final ArrayList<ArrayList> results = ms.performCustomSearch(query);

                for (final ArrayList al : results) {
                    final int cid = (Integer)al.get(0);
                    final int oid = (Integer)al.get(1);
                    String name = null;
                    try {
                        name = (String)al.get(2);
                    } catch (final Exception e) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("no name present for metaobjectnode", e); // NOI18N
                        }
                    }
                    final MetaObjectNode mon = new MetaObjectNode(DOMAIN, oid, cid, name);
                    metaObjects.add(mon);
                }

                return metaObjects;
            } catch (RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
                throw new SearchException("An error is occured, possibly an sql syntax error", ex);
            }
        } else {
            LOG.error("active local server not found"); // NOI18N
        }

        return metaObjects;
    }
}
