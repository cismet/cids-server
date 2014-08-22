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
        final String sql = ""                                                                                                      // NOI18N
                    + "SELECT DISTINCT i.class_id , "                                                                              // NOI18N
                    + "                i.object_id, "                                                                              // NOI18N
                    + "                s.stringrep "                                                                               // NOI18N
                    + "FROM            geom g, "                                                                                   // NOI18N
                    + "                cs_attr_object_derived i "                                                                  // NOI18N
                    + "                LEFT OUTER JOIN cs_stringrepcache s "                                                       // NOI18N
                    + "                ON              ( "                                                                         // NOI18N
                    + "                                                s.class_id =i.class_id "                                    // NOI18N
                    + "                                AND             s.object_id=i.object_id "                                   // NOI18N
                    + "                                ) "                                                                         // NOI18N
                    + "WHERE           i.attr_class_id = "                                                                         // NOI18N
                    + "                ( SELECT cs_class.id "                                                                      // NOI18N
                    + "                FROM    cs_class "                                                                          // NOI18N
                    + "                WHERE   cs_class.table_name::text = 'GEOM'::text "                                          // NOI18N
                    + "                ) "                                                                                         // NOI18N
                    + "AND             i.attr_object_id = g.id "                                                                   // NOI18N
                    + "AND i.class_id IN <cidsClassesInStatement> "                                                                // NOI18N
                    + "AND geo_field && st_GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') "             // NOI18N
                    + "AND st_intersects(geo_field,st_GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>')) " // NOI18N
                    + "ORDER BY        1,2,3";

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

        return
            sql.replaceAll("<cidsClassesInStatement>", classesInStatement) // NOI18N
            .replaceAll("<cidsSearchGeometryWKT>", cidsSearchGeometryWKT)  // NOI18N
            .replaceAll("<cidsSearchGeometrySRID>", sridString);           // NOI18N
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
