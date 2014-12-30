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
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = GeoSearch.class)
public class DefaultGeoSearch extends AbstractCidsServerSearch implements GeoSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DefaultGeoSearch.class);

    //~ Instance fields --------------------------------------------------------

    private Geometry geometry;

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
    public String getSearchSql(final String domainKey) {
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
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("geosearch started"); // NOI18N
            }

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLocalServers().keySet());

            for (final Object domainKey : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(domainKey);

                final String sqlStatement = getSearchSql((String)domainKey);
                if (sqlStatement != null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("geosearch: " + sqlStatement); // NOI18N
                    }

                    final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);

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

                        final MetaObjectNode mon = new MetaObjectNode((String)domainKey, oid, cid, name);
                        aln.add(mon);
                    }
                }
            }
        } catch (final Exception e) {
            LOG.error("Problem during GEOSEARCH", e);                 // NOI18N
            throw new SearchException("Problem during GEOSEARCH", e); // NOI18N
        }

        return aln;
    }
}
