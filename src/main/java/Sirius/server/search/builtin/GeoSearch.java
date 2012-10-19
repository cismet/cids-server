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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class GeoSearch extends CidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    Geometry searchGeometry = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoSearch object.
     *
     * @param  searchGeometry  DOCUMENT ME!
     */
    public GeoSearch(final Geometry searchGeometry) {
        this.searchGeometry = searchGeometry;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   domainKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGeoSearchSql(final Object domainKey) {
//        final String ssql = "WITH recursive derived_index(ocid,oid,stringrep,acid,aid,depth) AS "
//                    + "( "
//                    + "SELECT class_id,object_id,stringrep,class_id,object_id,0 "
//                    + "FROM GEOSUCHE2 WHERE class_id IN"
//                    + "( "
//                    + "WITH recursive derived_child(father,child,depth) AS ( "
//                    + "SELECT father,father,0 FROM cs_class_hierarchy WHERE father in <cidsClassesInStatement> "
//                    + "UNION ALL "
//                    + "SELECT ch.father,ch.child,dc.depth+1  FROM derived_child dc,cs_class_hierarchy ch WHERE ch.father=dc.child) "
//                    + "SELECT DISTINCT child FROM derived_child LIMIT 100 "
//                    + ") "
//                    + "AND geo_field && GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') AND intersects(geo_field,GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>')) "
//                    + "UNION ALL "
//                    + "SELECT aam.class_id,aam.object_id,stringrep, aam.attr_class_id, aam.attr_object_id,di.depth+1 FROM cs_attr_object aam,derived_index di WHERE aam.attr_class_id =di.ocid AND    aam.attr_object_id=di.oid "
//                    + ") "
//                    + "SELECT DISTINCT ocid,oid,stringrep FROM derived_index WHERE ocid in <cidsClassesInStatement> LIMIT 10000000 ";
//
        final String sql = ""
                    + "SELECT DISTINCT i.class_id , "
                    + "                i.object_id, "
                    + "                s.stringrep "
                    + "FROM            geom g, "
                    + "                cs_attr_object_derived i "
                    + "                LEFT OUTER JOIN cs_stringrepcache s "
                    + "                ON              ( "
                    + "                                                s.class_id =i.class_id "
                    + "                                AND             s.object_id=i.object_id "
                    + "                                ) "
                    + "WHERE           i.attr_class_id = "
                    + "                ( SELECT cs_class.id "
                    + "                FROM    cs_class "
                    + "                WHERE   cs_class.table_name::text = 'GEOM'::text "
                    + "                ) "
                    + "AND             i.attr_object_id = g.id "
                    + "AND i.class_id IN <cidsClassesInStatement> "
                    + "AND geo_field && GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') "
                    + "AND <intersectsStatement> "
                    + "ORDER BY        1,2,3";

        final String sqlAlt = ""
                    + "\nWITH recursive derived_index(ocid,oid,acid,aid,depth) AS "
                    + "\n( SELECT class_id, "
                    + "\n        object_id, "
                    + "\n        class_id , "
                    + "\n        object_id, "
                    + "\n        0 "
                    + "\nFROM    geosuche2 "
                    + "\nWHERE   class_id IN( WITH recursive derived_child(father,child,depth) AS "
                    + "\n                    ( SELECT father, "
                    + "\n                            father , "
                    + "\n                            0 "
                    + "\n                    FROM    cs_class_hierarchy "
                    + "\n                    WHERE   father IN <cidsClassesInStatement> "
                    + "\n                     "
                    + "\n                    UNION ALL "
                    + "\n                     "
                    + "\n                    SELECT ch.father, "
                    + "\n                           ch.child , "
                    + "\n                           dc.depth+1 "
                    + "\n                    FROM   derived_child dc, "
                    + "\n                           cs_class_hierarchy ch "
                    + "\n                    WHERE  ch.father=dc.child "
                    + "\n                    ) "
                    + "\n             SELECT DISTINCT child "
                    + "\n             FROM            derived_child LIMIT 100 ) "
                    + "\nAND             geo_field && GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') "
                    + "\nAND             intersects(geo_field,GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>')) "
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
                    + "\nWHERE  aam.attr_class_id =di.ocid "
                    + "\nAND    aam.attr_object_id=di.oid "
                    + "\n) "
                    + "\nSELECT DISTINCT ocid, "
                    + "\n                oid , "
                    + "\n                stringrep "
                    + "\nFROM            derived_index "
                    + "\n                LEFT OUTER JOIN cs_stringrepcache "
                    + "\n                ON              ( "
                    + "\n                                                ocid=class_id "
                    + "\n                                AND             oid =object_id "
                    + "\n                                ) "
                    + "\nWHERE           ocid IN <cidsClassesInStatement>  LIMIT 10000000 ";

        final String intersectsStatement;
        if (searchGeometry.getSRID() == 4326) {
            intersectsStatement =
                "intersects(geo_field,GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>'))";
        } else {
            if ((searchGeometry instanceof Polygon) || (searchGeometry instanceof MultiPolygon)) {    // with buffer for searchGeometry
                intersectsStatement =
                    "intersects(st_buffer(geo_field, 0.000001),st_buffer(GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>'), 0.000001))";
            } else {                                                                                  // without buffer for searchGeometry
                intersectsStatement =
                    "intersects(st_buffer(geo_field, 0.000001),GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>'), 0.000001)";
            }
        }
        final String cidsSearchGeometryWKT = searchGeometry.toText();
        final String sridString = Integer.toString(searchGeometry.getSRID());
        final String classesInStatement = getClassesInSnippetsPerDomain().get((String)domainKey);
        if ((cidsSearchGeometryWKT == null) || (cidsSearchGeometryWKT.trim().length() == 0)
                    || (sridString == null)
                    || (sridString.trim().length() == 0)) {
            // TODO: Notify user?
            getLog().error(
                "Search geometry or srid is not given. Can't perform a search without those information.");
            return null;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("cidsClassesInStatement=" + classesInStatement);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("cidsSearchGeometryWKT=" + cidsSearchGeometryWKT);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("cidsSearchGeometrySRID=" + sridString);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("intersectsStatement=" + intersectsStatement);
        }

        if ((classesInStatement == null) || (classesInStatement.trim().length() == 0)) {
            getLog().warn("There are no search classes defined for domain '" + domainKey
                        + "'. This domain will be skipped.");
            return null;
        }
        return sql.replaceAll("<intersectsStatement>", intersectsStatement)
                    .replaceAll("<cidsClassesInStatement>", classesInStatement)
                    .replaceAll("<cidsSearchGeometryWKT>", cidsSearchGeometryWKT)
                    .replaceAll("<cidsSearchGeometrySRID>", sridString);
    }

    @Override
    public Collection performServerSearch() throws Exception {
        try {
            getLog().info("geosearch started");
            final ArrayList<Node> aln = new ArrayList<Node>();

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLoaclServers().keySet());

            for (final Object domainKey : keyset) {
                final MetaService ms = (MetaService)getActiveLoaclServers().get(domainKey);

                final String sqlStatement = getGeoSearchSql(domainKey);
                if (sqlStatement != null) {
                    getLog().info("geosearch: " + sqlStatement);
                    final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);

                    for (final ArrayList al : result) {
                        final int cid = (Integer)al.get(0);
                        final int oid = (Integer)al.get(1);
                        String name = null;
                        try {
                            name = (String)al.get(2);
                        } catch (Exception e) {
                        }

                        final MetaObjectNode mon = new MetaObjectNode((String)domainKey, oid, cid, name);
                        aln.add(mon);
                    }
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().error("Problem during GEOSEARCH", e);
            throw new Exception("Problem during GEOSEARCH", e);
        }
    }
}
