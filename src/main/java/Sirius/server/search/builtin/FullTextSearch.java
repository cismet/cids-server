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
    public Collection performServerSearch() throws Exception{
        try {
            getLog().info("FullTextSearch started");

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

            final String altsql = "select distinct ocid,oid,stringrep from "
                        + "\n(WITH recursive derived_index(ocid,oid,acid,aid,depth) AS "
                        + "\n( SELECT class_id         , "
                        + "\n        object_id         , "
                        + "\n        class_id         , "
                        + "\n        object_id         , "
                        + "\n        0 "
                        + "\nFROM    textsearch "
                        + "\nWHERE   class_id IN ( WITH recursive derived_child(father,child,depth) AS "
                        + "\n                     ( SELECT father, "
                        + "\n                             father , "
                        + "\n                             0 "
                        + "\n                     FROM    cs_class_hierarchy "
                        + "\n                     WHERE   father IN <cidsClassesInStatement> "
                        + "\n                      "
                        + "\n                     UNION ALL "
                        + "\n                      "
                        + "\n                     SELECT ch.father, "
                        + "\n                            ch.child , "
                        + "\n                            dc.depth+1 "
                        + "\n                     FROM   derived_child dc, "
                        + "\n                            cs_class_hierarchy ch "
                        + "\n                     WHERE  ch.father=dc.child "
                        + "\n                     ) "
                        + "\n              SELECT DISTINCT child "
                        + "\n              FROM            derived_child LIMIT 100000 ) "
                        + "\nAND             string_val " + caseSensitiveI + "LIKE '%<cidsSearchText>%' "
                        + "\n "
                        + "\nUNION ALL "
                        + "\n "
                        + "\nSELECT aam.class_id      , "
                        + "\n       aam.object_id     , "
                        + "\n       aam.attr_class_id , "
                        + "\n       aam.attr_object_id, "
                        + "\n       di.depth+1 "
                        + "\nFROM   cs_attr_object aam, "
                        + "\n       derived_index di "
                        + "\nWHERE  aam.attr_class_id =di.ocid AND"
                        + "\n       aam.attr_object_id=di.oid "
                        + "\n) "
                        + "\nSELECT ocid,oid,stringrep "
                        + "\nFROM   derived_index left outer join cs_stringrepcache on ocid=class_id AND oid =object_id "
                        + "\nWHERE  ocid IN <cidsClassesInStatement> LIMIT 10000000) as x";

            final String geoMidFix = "\n ) as txt,(select distinct class_id as ocid,object_id as oid,stringrep from (";

            final String geoPostfix = "\n )as y ) as geo "
                        + "\n where txt.ocid=geo.ocid and txt.oid=geo.oid";

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
                        String name = null;
                        try {
                            name = (String)al.get(2);
                        } catch (Exception e) {
                        }
                        final MetaObjectNode mon = new MetaObjectNode((String)key, oid, cid, name);
                        aln.add(mon);
                    }
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().error("Problem during Fulltextsearch", e);
            throw new Exception("Problem during Fulltextsearch",e);
        }
    }
}
