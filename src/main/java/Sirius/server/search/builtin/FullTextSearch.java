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
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        try {
            getLog().info("FullTextSearch started");

            String sql =
                "select distinct class_id,object_id,name,string_val from TEXTSEARCH where lower(string_val) like lower('%<cidsSearchText>%') and class_id in <cidsClassesInStatement>";
            if (caseSensitive) {
                sql =
                    "select distinct class_id,object_id,name,string_val from TEXTSEARCH where string_val like '%<cidsSearchText>%' and class_id in <cidsClassesInStatement>";
            }
            // Deppensuche sequentiell
            final HashSet keyset = new HashSet(getActiveLocalServers().keySet());

            final ArrayList<Node> aln = new ArrayList<Node>();

            for (final Object key : keyset) {
                final MetaService ms = (MetaService)getActiveLocalServers().get(key);
                final String classesInStatement = getClassesInSnippetsPerDomain().get((String)key);
                final String sqlStatement = sql.replaceAll("<cidsClassesInStatement>", classesInStatement)
                            .replaceAll("<cidsSearchText>", searchText);
                if (getLog().isDebugEnabled()) {
                    getLog().debug(sqlStatement);
                }
                final ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);
                for (final ArrayList al : result) {
                    final int cid = (Integer)al.get(0);
                    final int oid = (Integer)al.get(1);
                    final String name = (String)al.get(2);
                    final String string_val = (String)al.get(3);
                    final MetaObjectNode mon = new MetaObjectNode((String)key, oid, cid);
                    aln.add(mon);
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().error("Problem", e);
            return null;
        }
    }
}
