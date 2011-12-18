/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Sirius.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.search.CidsServerSearch;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class FullTextSearch extends CidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    private String searchText;
    private boolean caseSensitive;
    private Geometry geometry;
    private GeoSearch geoSearch;

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
        if (geometry != null) {
            geoSearch = new GeoSearch(geometry);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        try {
            getLog().info("FullTextSearch started");

            final String caseSensitiveI = (caseSensitive) ? "" : "I"; // Ein I vor LIKE macht die Suche case insensitive

            final String geoPrefix = " select distinct * from ( ";

            final String sql = "select distinct ocid,oid from "
                        + "(WITH recursive derived_index(ocid,oid,acid,aid,depth) AS "
                        + "( SELECT class_id         , "
                        + "        object_id         , "
                        + "        class_id         , "
                        + "        object_id         , "
                        + "        0 "
                        + "FROM    textsearch "
                        + "WHERE   class_id IN ( WITH recursive derived_child(father,child,depth) AS "
                        + "                     ( SELECT father, "
                        + "                             father , "
                        + "                             0 "
                        + "                     FROM    cs_class_hierarchy "
                        + "                     WHERE   father IN <cidsClassesInStatement> "
                        + "                      "
                        + "                     UNION ALL "
                        + "                      "
                        + "                     SELECT ch.father, "
                        + "                            ch.child , "
                        + "                            dc.depth+1 "
                        + "                     FROM   derived_child dc, "
                        + "                            cs_class_hierarchy ch "
                        + "                     WHERE  ch.father=dc.child "
                        + "                     ) "
                        + "              SELECT DISTINCT child "
                        + "              FROM            derived_child LIMIT 100000 ) "
                        + "AND             string_val " + caseSensitiveI + "LIKE '%<cidsSearchText>%' "
                        + " "
                        + "UNION ALL "
                        + " "
                        + "SELECT aam.class_id      , "
                        + "       aam.object_id     , "
                        + "       aam.attr_class_id , "
                        + "       aam.attr_object_id, "
                        + "       di.depth+1 "
                        + "FROM   cs_attr_object aam, "
                        + "       derived_index di "
                        + "WHERE  aam.class_id=di.acid "
                        + "AND    aam.object_id=di.aid "
                        + ") "
                        + "SELECT * "
                        + "FROM   derived_index "
                        + "WHERE  ocid IN <cidsClassesInStatement> LIMIT 1000) as x";

            final String geoMidFix = " ) as txt,(select distinct ocid,oid from (";

            final String geoPostfix = " )as y ) as geo "
                        + " where txt.ocid=geo.ocid and txt.oid=geo.oid";

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLoaclServers().keySet());

            final ArrayList<Node> aln = new ArrayList<Node>();

            if (geometry != null) {
                geoSearch.setClassesInSnippetsPerDomain(getClassesInSnippetsPerDomain());
            }

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLoaclServers().get(key);
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

                    if (getLog().isDebugEnabled()) {
                        getLog().debug(sqlStatement);
                    }
                    final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);
                    for (final ArrayList al : result) {
                        final int cid = (Integer)al.get(0);
                        final int oid = (Integer)al.get(1);
                        final MetaObjectNode mon = new MetaObjectNode((String)key, oid, cid);
                        aln.add(mon);
                    }
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().fatal("Problem bei der Volltextsuche", e);
            return null;
        }
    }
}
