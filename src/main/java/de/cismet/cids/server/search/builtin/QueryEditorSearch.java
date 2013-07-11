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

    private static final String query = "SELECT {2} classid, tbl.id objectid, c.stringrep FROM {0} tbl "
                + "LEFT OUTER JOIN cs_stringrepcache c "     // NOI18N
                + "       ON     ( "                         // NOI18N
                + "                     c.class_id ={2} "    // NOI18N
                + "              AND    c.object_id=tbl.id " // NOI18N
                + "              ) WHERE {1}";
    private static final String WRRL_DOMAIN = "WRRL_DB_MV";  // NOI18N
    private static final transient Logger LOG = Logger.getLogger(QueryEditorSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String whereClause;
    private int classId;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryEditorSearch object.
     *
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     * @param  classId      DOCUMENT ME!
     */
    public QueryEditorSearch(final String metaClass, final String whereClause, final int classId) {
        this.whereClause = whereClause;
        this.metaClass = metaClass;
        this.classId = classId;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(WRRL_DOMAIN);
        if (ms != null) {
            try {
                final String query = MessageFormat.format(this.query, metaClass, whereClause, classId);
                final ArrayList<ArrayList> results = ms.performCustomSearch(query);

                final ArrayList<MetaObjectNode> metaObjects = new ArrayList<MetaObjectNode>();
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
                    final MetaObjectNode mon = new MetaObjectNode(WRRL_DOMAIN, oid, cid, name);
                    metaObjects.add(mon);
                }

                return metaObjects;
            } catch (RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } else {
            LOG.error("active local server not found"); // NOI18N
        }

        return null;
    }
}
