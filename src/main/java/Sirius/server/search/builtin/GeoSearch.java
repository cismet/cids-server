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

import Sirius.server.localserver.object.ObjectHierarchy;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.search.CidsServerSearch;
import Sirius.server.search.StaticSearchTools;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;

import de.cismet.tools.collections.MultiMap;

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

    @Override
    public Collection performServerSearch() {
        final ArrayList<Node> aln = new ArrayList<Node>();
        try {
            getLog().info("geosearch started");

            final Collection<MetaClass> classes = getValidClasses();

//          "select class_id,object_id,name from GEOSUCHE where class_id in <cidsClassesInStatement> and geo_field && GeometryFromText('SRID=-1;<cidsSearchGeometryWKT>') and intersects(geo_field,GeometryFromText('SRID=-1;<cidsSearchGeometryWKT>'))";
            final String sql = "WITH recursive derived_index(ocid,oid,acid,aid,depth) AS "
                        + "( "
                        + "SELECT class_id,object_id,cast (NULL AS int), cast (NULL AS int),0 "
                        + "FROM GEOSUCHE WHERE class_id IN"
                        + "( "
                        + "WITH recursive derived_child(father,child,depth) AS ( "
                        + "SELECT father,father,0 FROM cs_class_hierarchy WHERE father in <cidsClassesInStatement> "
                        + "UNION ALL "
                        + "SELECT ch.father,ch.child,dc.depth+1  FROM derived_child dc,cs_class_hierarchy ch WHERE ch.father=dc.child) "
                        + "SELECT DISTINCT father FROM derived_child LIMIT 100 "
                        + ") "
                        + "AND geo_field && GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>') AND intersects(geo_field,GeometryFromText('SRID=<cidsSearchGeometrySRID>;<cidsSearchGeometryWKT>')) "
                        + "UNION ALL "
                        + "SELECT aam.class_id,aam.object_id, aam.attr_class_id, aam.attr_object_id,di.depth+1 FROM cs_all_attr_mapping aam,derived_index di WHERE aam.attr_class_id=di.ocid AND aam.attr_object_id=di.oid"
                        + ") "
                        + "SELECT DISTINCT ocid,oid FROM derived_index WHERE ocid in <cidsClassesInStatement> LIMIT 1000 ";

            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLoaclServers().keySet());
            final String cidsSearchGeometryWKT = searchGeometry.toText();
            final String sridString = Integer.toString(searchGeometry.getSRID());

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLoaclServers().get(key);
                final String classesInStatement = getClassesInSnippetsPerDomain().get((String)key);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("cidsClassesInStatement=" + classesInStatement);
                }
                if (getLog().isDebugEnabled()) {
                    getLog().debug("cidsSearchGeometryWKT=" + cidsSearchGeometryWKT);
                }
                if (getLog().isDebugEnabled()) {
                    getLog().debug("cidsSearchGeometrySRID=" + sridString);
                }
                final String sqlStatement = sql.replaceAll("<cidsClassesInStatement>", classesInStatement)
                            .replaceAll("<cidsSearchGeometryWKT>", cidsSearchGeometryWKT)
                            .replaceAll("<cidsSearchGeometrySRID>", sridString);
                getLog().info("geosearch: " + sqlStatement);
                final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);
                for (final ArrayList al : result) {
                    final int cid = (Integer)al.get(0);
                    final int oid = (Integer)al.get(1);
                    final String name = null; // (String) al.get(2);
                    final MetaObjectNode mon = new MetaObjectNode((String)key, oid, cid);
                    aln.add(mon);
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().error("Problem during GEOSEARCH", e);
            return aln;
        }
    }
}
