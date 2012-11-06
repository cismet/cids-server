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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
import de.cismet.cids.server.search.SearchException;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DefaultFullTextSearch extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DefaultFullTextSearch.class);

    //~ Instance fields --------------------------------------------------------

    private final String searchText;
    private final boolean caseSensitive;
    private final Geometry geometry;
    private final GeoSearch geoSearch;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultFullTextSearch object.
     *
     * @param  searchText     The text to search for.
     * @param  caseSensitive  A flag indicating whether to make the search case sensitive or not.
     */
    public DefaultFullTextSearch(final String searchText, final boolean caseSensitive) {
        this(searchText, caseSensitive, null);
    }

    /**
     * Creates a new DefaultFullTextSearch object.
     *
     * @param  searchText     The text to search for.
     * @param  caseSensitive  A flag indicating whether to make the search case sensitive or not.
     * @param  geometry       The search will be restricted to the given geometry.
     */
    public DefaultFullTextSearch(final String searchText, final boolean caseSensitive, final Geometry geometry) {
        this.searchText = searchText;
        this.caseSensitive = caseSensitive;
        this.geometry = geometry;
        if (geometry == null) {
            geoSearch = null;
        } else {
            // DefaultGeoSearch always present
            geoSearch = Lookup.getDefault().lookup(GeoSearch.class);
            geoSearch.setGeometry(geometry);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() throws SearchException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("FullTextSearch started"); // NOI18N
            }

            final String caseSensitiveI = (caseSensitive) ? "" : "I"; // Ein I vor LIKE macht die Suche case insensitive

            final String geoPrefix = "\n select distinct * from ( "; // NOI18N

            final String sql = ""                                                                // NOI18N
                        + "SELECT DISTINCT i.class_id ocid,i.object_id as oid, c.stringrep "     // NOI18N
                        + "FROM   cs_attr_string s, "                                            // NOI18N
                        + "       cs_attr_object_derived i "                                     // NOI18N
                        + "       LEFT OUTER JOIN cs_stringrepcache c "                          // NOI18N
                        + "       ON     ( "                                                     // NOI18N
                        + "                     c.class_id =i.class_id "                         // NOI18N
                        + "              AND    c.object_id=i.object_id "                        // NOI18N
                        + "              ) "                                                     // NOI18N
                        + "WHERE  i.attr_class_id = s.class_id "                                 // NOI18N
                        + "AND    i.attr_object_id=s.object_id "                                 // NOI18N
                        + "AND    s.string_val " + caseSensitiveI + "like '%<cidsSearchText>%' " // NOI18N
                        + "AND i.class_id IN <cidsClassesInStatement>";

            final String geoMidFix = "\n ) as txt,(select distinct class_id as ocid,object_id as oid,stringrep from ("; // NOI18N

            final String geoPostfix = "\n )as y ) as geo "                  // NOI18N
                        + "\n where txt.ocid=geo.ocid and txt.oid=geo.oid"; // NOI18N

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
                    String sqlStatement =
                        sql.replaceAll("<cidsClassesInStatement>", classesInStatement) // NOI18N
                        .replaceAll("<cidsSearchText>", searchText);                   // NOI18N

                    if (geometry != null) {
                        final String geoSql = geoSearch.getSearchSql((String)key);
                        if (geoSql != null) {
                            sqlStatement = geoPrefix + sqlStatement + geoMidFix + geoSql + geoPostfix;
                        }
                    }

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
