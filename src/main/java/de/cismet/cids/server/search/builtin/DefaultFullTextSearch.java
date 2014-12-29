/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
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
@ServiceProvider(service = FullTextSearch.class)
public class DefaultFullTextSearch extends AbstractCidsServerSearch implements FullTextSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DefaultFullTextSearch.class);

    //~ Instance fields --------------------------------------------------------

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

            if (geometry != null) {
                geoSearch.setClassesInSnippetsPerDomain(getClassesInSnippetsPerDomain());
            }

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(key);
                final String classesInStatement = getClassesInSnippetsPerDomain().get((String)key);
                if (classesInStatement != null) {
                    String geoSql = null;
                    if (geometry != null) {
                        geoSql = geoSearch.getSearchSql((String)key);
                    }

                    final String sqlStatement = SQLTools.getStatements(Lookup.getDefault().lookup(
                                    DialectProvider.class).getDialect())
                                .getDefaultFullTextSearchStmt(searchText, classesInStatement, geoSql, caseSensitive);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(sqlStatement);
                    }
                    final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);
                    for (final ArrayList al : result) {
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
                        final MetaObjectNode mon = new MetaObjectNode((String)key, oid, cid, name);
                        aln.add(mon);
                    }
                }
            }

            return aln;
        } catch (final Exception e) {
            LOG.error("Problem during Fulltextsearch", e);                 // NOI18N
            throw new SearchException("Problem during Fulltextsearch", e); // NOI18N
        }
    }
}
