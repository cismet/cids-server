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
        final String sql = "WITH recursive derived_index(ocid,oid,stringrep,acid,aid,depth) AS "
                    + "( "
                    + "SELECT class_id,object_id,stringrep,class_id,object_id,0 "
                    + "FROM GEOSUCHE2 WHERE class_id IN"
                    + "( "
                    + "WITH recursive derived_child(father,child,depth) AS ( "
                    + "SELECT father,father,0 FROM cs_class_hierarchy WHERE father in <cidsClassesInStatement> "
                    + "UNION ALL "
                    + "SELECT ch.father,ch.child,dc.depth+1  FROM derived_child dc,cs_class_hierarchy ch WHERE ch.father=dc.child) "
                    + "SELECT DISTINCT child FROM derived_child LIMIT 100 "
                    + ") "
                    + "AND geo_field && GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') AND intersects(geo_field,GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>')) "
                    + "UNION ALL "
                    + "SELECT aam.class_id,aam.object_id,stringrep, aam.attr_class_id, aam.attr_object_id,di.depth+1 FROM cs_attr_object aam,derived_index di WHERE aam.class_id=di.acid and aam.object_id=di.aid "
                    + ") "
                    + "SELECT DISTINCT ocid,oid,stringrep FROM derived_index WHERE ocid in <cidsClassesInStatement> LIMIT 10000000 ";
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

        if ((classesInStatement == null) || (classesInStatement.trim().length() == 0)) {
            getLog().warn("There are no search classes defined for domain '" + domainKey
                        + "'. This domain will be skipped.");
            return null;
        }

        return sql.replaceAll("<cidsClassesInStatement>", classesInStatement)
                    .replaceAll("<cidsSearchGeometryWKT>", cidsSearchGeometryWKT)
                    .replaceAll("<cidsSearchGeometrySRID>", sridString);
    }

    @Override
    public Collection performServerSearch() {
        final ArrayList<Node> aln = new ArrayList<Node>();
        try {
            getLog().info("geosearch started");

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
        } catch (Exception e) {
            getLog().error("Problem during GEOSEARCH", e);
        }

        return aln;
    }
}
