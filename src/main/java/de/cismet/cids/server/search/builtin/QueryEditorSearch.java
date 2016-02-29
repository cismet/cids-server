/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.nodepermissions.NoNodePermissionProvidedException;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * As this search allows the user to specify a where clause he has to know what backend the server it is executed on
 * uses. Thus the search may fail due to wrong dialect.
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class QueryEditorSearch extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {

    //~ Static fields/initializers ---------------------------------------------

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
        final ArrayList<MetaObjectNode> filtered = new ArrayList<MetaObjectNode>();
        if (ms != null) {
            try {
                final boolean paginationEnabled = limit >= 0;

                final String query;
                if (paginationEnabled) {
                    query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                                .getQueryEditorSearchPaginationStmt(metaClass, classId, whereClause, limit, offset);
                } else {
                    query = SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                                .getQueryEditorSearchStmt(metaClass, classId, whereClause);
                }

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
                    // Cashed Geometry
                    Geometry cashedGeometry = null;
                    try {
                        final Object cashedGeometryTester = al.get(3);

                        if (cashedGeometryTester != null) {
                            cashedGeometry = SQLTools.getGeometryFromResultSetObject(cashedGeometryTester);
                        }
                    } catch (Exception e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(
                                "cashedGeometry was not in the resultset. But this is normal for the most parts",
                                e); // NOI18N
                        }
                    }

                    // Lightweight Json
                    String lightweightJson = null;
                    try {
                        final Object tester = al.get(4);
                        if ((tester != null) && (tester instanceof String)) { // NOI18N
                            lightweightJson = (String)tester;                 // NOI18N
                        }
                    } catch (Exception skip) {
                    }

                    try {
                        final MetaObjectNode mon = new MetaObjectNode(
                                DOMAIN,
                                getUser(),
                                oid,
                                cid,
                                name,
                                cashedGeometry,
                                lightweightJson); // TODO: Check4CashedGeomAndLightweightJson

                        metaObjects.add(mon);
                    } catch (NoNodePermissionProvidedException noNodePermissionProvidedException) {
                        filtered.add(noNodePermissionProvidedException.getMon());
                    }
                }
                if (filtered.size() > 0) {
                    LOG.info(filtered.size() + " Objcets filtered");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(filtered.size() + " Objcets filtered\n" + filtered.toString());
                    }
                }
                return metaObjects;
            } catch (final RemoteException ex) {
                LOG.error(ex.getMessage(), ex);
                throw new SearchException("An error is occured, possibly an sql syntax error", ex); // NOI18N
            }
        } else {
            LOG.error("active local server not found");                                             // NOI18N
        }

        return metaObjects;
    }
}
