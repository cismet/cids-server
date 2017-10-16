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

import de.cismet.cids.server.connectioncontext.ClientConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContextProvider;
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
@ServiceProvider(service = GeoSearch.class)
public class DefaultGeoSearch extends AbstractCidsServerSearch implements GeoSearch,
    SearchResultListenerProvider,
    ServerConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DefaultGeoSearch.class);

    //~ Instance fields --------------------------------------------------------

    private Geometry geometry;
    private transient SearchResultListener searchResultListener;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public PreparableStatement getSearchSql(final String domainKey) {
        final String cidsSearchGeometryWKT = geometry.toText();
        final String sridString = Integer.toString(geometry.getSRID());
        final String classesInStatement = getClassesInSnippetsPerDomain().get(domainKey);
        if ((cidsSearchGeometryWKT == null) || (cidsSearchGeometryWKT.trim().length() == 0)
                    || (sridString == null)
                    || (sridString.trim().length() == 0)) {
            // TODO: Notify user?
            LOG.error(
                "Search geometry or srid is not given. Can't perform a search without those information."); // NOI18N

            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("cidsClassesInStatement=" + classesInStatement);   // NOI18N
            LOG.debug("cidsSearchGeometryWKT=" + cidsSearchGeometryWKT); // NOI18N
            LOG.debug("cidsSearchGeometrySRID=" + sridString);           // NOI18N
        }

        if ((classesInStatement == null) || (classesInStatement.trim().length() == 0)) {
            LOG.warn("There are no search classes defined for domain '" + domainKey // NOI18N
                        + "'. This domain will be skipped."); // NOI18N
            return null;
        }

        return SQLTools.getStatements(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                    .getDefaultGeoSearchStmt(cidsSearchGeometryWKT, sridString, classesInStatement);
    }

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        final ArrayList<MetaObjectNode> aln = new ArrayList<MetaObjectNode>();
        final ArrayList<MetaObjectNode> filtered = new ArrayList<MetaObjectNode>();
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("geosearch started"); // NOI18N
            }

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLocalServers().keySet());

            for (final Object domainKey : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(domainKey);

                final PreparableStatement sqlStatement = getSearchSql((String)domainKey);
                if (sqlStatement != null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("geosearch: " + sqlStatement); // NOI18N
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
                            },
                            getServerConnectionContext());

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
                            final MetaObjectNode mon = new MetaObjectNode((String)domainKey,
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
        } catch (final Exception e) {
            LOG.error("Problem during GEOSEARCH", e);                             // NOI18N
            throw new SearchException("Problem during GEOSEARCH", e);             // NOI18N
        }
        if (filtered.size() > 0) {
            LOG.info(filtered.size() + " Objcets filtered");
            if (LOG.isDebugEnabled()) {
                LOG.debug(filtered.size() + " Objcets filtered\n" + filtered.toString());
            }
        }
        return aln;
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

    @Override
    public ServerConnectionContext getServerConnectionContext() {
        return ServerConnectionContext.create(getClass().getSimpleName());
    }
}
