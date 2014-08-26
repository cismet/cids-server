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
import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = CidsServerSearch.class)
public class QueryEditorSearch extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final String query = "SELECT {2} AS classid, tbl.id AS objectid, c.stringrep FROM {0} tbl "
                + "LEFT OUTER JOIN cs_stringrepcache c "     // NOI18N
                + "       ON     ( "                         // NOI18N
                + "                     c.class_id ={2} "    // NOI18N
                + "              AND    c.object_id=tbl.id " // NOI18N
                + "              ) WHERE {1}";
    private static final transient Logger LOG = Logger.getLogger(QueryEditorSearch.class);

    //~ Instance fields --------------------------------------------------------

    private String metaClass;
    private String whereClause;
    private int classId;
    private String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryEditorSearch object.
     */
    public QueryEditorSearch() {
    }

    /**
     * Creates a new QueryEditorSearch object.
     *
     * @param  domain       DOCUMENT ME!
     * @param  metaClass    DOCUMENT ME!
     * @param  whereClause  DOCUMENT ME!
     * @param  classId      DOCUMENT ME!
     */
    public QueryEditorSearch(final String domain, final String metaClass, final String whereClause, final int classId) {
        setWhereClause(whereClause);
        setMetaClass(metaClass);
        setClassId(classId);
        setDomain(domain);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMetaClass() {
        return metaClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClassId() {
        return classId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaClass  DOCUMENT ME!
     */
    public final void setMetaClass(final String metaClass) {
        this.metaClass = metaClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  whereClause  DOCUMENT ME!
     */
    public final void setWhereClause(final String whereClause) {
        this.whereClause = whereClause;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classId  DOCUMENT ME!
     */
    public final void setClassId(final int classId) {
        this.classId = classId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  DOMAIN  DOCUMENT ME!
     */
    public final void setDomain(final String DOMAIN) {
        this.domain = DOMAIN;
    }

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(domain);
        final ArrayList<MetaObjectNode> metaObjects = new ArrayList<MetaObjectNode>();
        if (ms != null) {
            try {
                final String query = MessageFormat.format(this.query, metaClass, whereClause, classId);
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
                    final MetaObjectNode mon = new MetaObjectNode(domain, oid, cid, name);
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
