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
import Sirius.server.sql.PreparableStatement;
import Sirius.server.sql.SQLTools;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.cismet.cids.nodepermissions.NoNodePermissionProvidedException;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.QueryPostProcessor;
import de.cismet.cids.server.search.SearchException;
import de.cismet.cids.server.search.SearchResultListener;
import de.cismet.cids.server.search.SearchResultListenerProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = FullTextSearch.class)
public class DefaultFullTextSearch extends AbstractCidsServerSearch implements FullTextSearch,
    SearchResultListenerProvider {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DefaultFullTextSearch.class);

    //~ Instance fields --------------------------------------------------------

    private transient SearchResultListener searchResultListener;
    private String searchText;
    private boolean caseSensitive;
    private Geometry geometry;
    private GeoSearch geoSearch;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getSearchText() {
        return searchText;
    }

    @Override
    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
        if (geometry == null) {
            geoSearch = null;
        } else {
            // DefaultGeoSearch always present
            geoSearch = Lookup.getDefault().lookup(GeoSearch.class);
            geoSearch.setGeometry(geometry);
        }
    }

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("FullTextSearch started"); // NOI18N
            }

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLocalServers().keySet());

            final ArrayList<MetaObjectNode> aln = new ArrayList<MetaObjectNode>();
            final ArrayList<MetaObjectNode> filtered = new ArrayList<MetaObjectNode>();

            if (geometry != null) {
                geoSearch.setClassesInSnippetsPerDomain(getClassesInSnippetsPerDomain());
            }

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(key);
                final String classesInStatement = getClassesInSnippetsPerDomain().get((String)key);
                if (classesInStatement != null) {
                    PreparableStatement geoSql = null;
                    if (geometry != null) {
                        geoSql = geoSearch.getSearchSql((String)key);
                    }

                    final PreparableStatement sqlStatement = SQLTools.getStatements(Lookup.getDefault().lookup(
                                    DialectProvider.class).getDialect())
                                .getDefaultFullTextSearchStmt(searchText, classesInStatement, geoSql, caseSensitive);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(sqlStatement);
                    }
                    final ArrayList<ArrayList> result = ms.performCustomSearch(
                            sqlStatement,
                            new QueryPostProcessor() {

                                @Override
                                public ArrayList<ArrayList> postProcess(final ArrayList<ArrayList> result) {
                                    for (final ArrayList row : result) {
                                        // Cashed Geometry
                                        Geometry cashedGeometry = null;
                                        try {
                                            final Object cashedGeometryTester = row.get(3);

                                            if (cashedGeometryTester != null) {
                                                cashedGeometry = SQLTools.getGeometryFromResultSetObject(
                                                        cashedGeometryTester);
                                                row.set(3, cashedGeometry);
                                            }
                                        } catch (Exception e) {
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug(
                                                    "cashedGeometry was not in the resultset. But this is normal for the most parts",
                                                    e); // NOI18N
                                            }
                                        }
                                    }
                                    return result;
                                }
                            });
                    for (final ArrayList al : result) {
                        // FIXME: yet another hack to circumvent odd type behaviour
                        final int cid = ((Number)al.get(0)).intValue();
                        final int oid = ((Number)al.get(1)).intValue();
                        String name = null;
                        try {
                            name = (String)al.get(2);
                        } catch (final Exception e) {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("no name present for metaobjectnode", e); // NOI18N
                            }
                        }

                        // Cashed Geometry
                        final Geometry cashedGeometry = (Geometry)al.get(3);

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
                            final MetaObjectNode mon = new MetaObjectNode((String)key,
                                    getUser(),
                                    oid,
                                    cid,
                                    name,
                                    cashedGeometry,
                                    lightweightJson);
                            aln.add(mon);
                        } catch (NoNodePermissionProvidedException noNodePermissionProvidedException) {
                            filtered.add(noNodePermissionProvidedException.getMon());
                        }
                    }
                }
            }
            if (filtered.size() > 0) {
                LOG.info(filtered.size() + " Objcets filtered");
                if (LOG.isDebugEnabled()) {
                    LOG.debug(filtered.size() + " Objcets filtered\n" + filtered.toString());
                }
            }
            return aln;
        } catch (final Exception e) {
            LOG.error("Problem during Fulltextsearch", e);                 // NOI18N
            throw new SearchException("Problem during Fulltextsearch", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchResultListener  DOCUMENT ME!
     */
    @Override
    public void setSearchResultListener(final SearchResultListener searchResultListener) {
        this.searchResultListener = searchResultListener;
    }

    @Override
    public SearchResultListener getSearchResultListener() {
        return searchResultListener;
    }
}
