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
import Sirius.server.middleware.types.Node;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class FullTextSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(FullTextSearch.class);

    //~ Instance fields --------------------------------------------------------

    private final String searchText;
    private final boolean caseSensitive;
    private final Geometry geometry;
    private final GeoSearch geoSearch;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FullTextSearch object.
     *
     * @param  searchText     The text to search for.
     * @param  caseSensitive  A flag indicating whether to make the search case sensitive or not.
     */
    public FullTextSearch(final String searchText, final boolean caseSensitive) {
        this(searchText, caseSensitive, null);
    }

    /**
     * Creates a new FullTextSearch object.
     *
     * @param  searchText     The text to search for.
     * @param  caseSensitive  A flag indicating whether to make the search case sensitive or not.
     * @param  geometry       The search will be restricted to the given geometry.
     */
    public FullTextSearch(final String searchText, final boolean caseSensitive, final Geometry geometry) {
        this.searchText = searchText;
        this.caseSensitive = caseSensitive;
        this.geometry = geometry;
        if (geometry == null) {
            geoSearch = null;
        } else {
            geoSearch = new GeoSearch(geometry);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() throws Exception {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("FullTextSearch started");
            }

            final String caseSensitiveI = (caseSensitive) ? "" : "I"; // Ein I vor LIKE macht die Suche case insensitive

            final String geoPrefix = "\n select distinct * from ( ";

            final String sql = ""
                        + "SELECT DISTINCT i.class_id ocid,i.object_id as oid, c.stringrep "
                        + "FROM   cs_attr_string s, "
                        + "       cs_attr_object_derived i "
                        + "       LEFT OUTER JOIN cs_stringrepcache c "
                        + "       ON     ( "
                        + "                     c.class_id =i.class_id "
                        + "              AND    c.object_id=i.object_id "
                        + "              ) "
                        + "WHERE  i.attr_class_id = s.class_id "
                        + "AND    i.attr_object_id=s.object_id "
                        + "AND    s.string_val " + caseSensitiveI + "like '%<cidsSearchText>%' "
                        + "AND i.class_id IN <cidsClassesInStatement>";

            final String geoMidFix = "\n ) as txt,(select distinct class_id as ocid,object_id as oid,stringrep from (";

            final String geoPostfix = "\n )as y ) as geo "
                        + "\n where txt.ocid=geo.ocid and txt.oid=geo.oid";

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLocalServers().keySet());

            final ArrayList<Node> aln = new ArrayList<Node>();

            if (geometry != null) {
                geoSearch.setClassesInSnippetsPerDomain(getClassesInSnippetsPerDomain());
            }

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(key);
                final String classesInStatement = getClassesInSnippetsPerDomain().get((String)key);
                if (classesInStatement != null) {
                    String sqlStatement = sql.replaceAll("<cidsClassesInStatement>", classesInStatement)
                                .replaceAll("<cidsSearchText>", searchText);

                    if (geometry != null) {
                        final String geoSql = geoSearch.getGeoSearchSql(key);
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
            LOG.error("Problem during Fulltextsearch", e);           // NOI18N
            throw new Exception("Problem during Fulltextsearch", e); // NOI18N
        }
    }
}
