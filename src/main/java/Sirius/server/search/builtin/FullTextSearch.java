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
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.middleware.types.Node;
import Sirius.server.search.CidsServerSearch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author thorsten
 */
public class FullTextSearch extends CidsServerSearch {

    String searchText;

    public FullTextSearch(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public Collection performServerSearch() {
        try {
            getLog().info("geosearch started");

            Collection<MetaClass> classes = getValidClasses();


            String sql = "select  distinct class_id,object_id,name,string_val  from TEXTSEARCH where lower(string_val) like lower('%<cidsSearchText>%') and class_id in <cidsClassesInStatement>";
            //Deppensuche sequentiell
            HashSet keyset = new HashSet(getActiveLoaclServers().keySet());

            ArrayList<Node> aln = new ArrayList<Node>();


            for (Object key : keyset) {
                MetaService ms = (MetaService) getActiveLoaclServers().get(key);
                String classesInStatement = getClassesInSnippetsPerDomain().get((String) key);
                String sqlStatement = sql.replaceAll("<cidsClassesInStatement>", classesInStatement).replaceAll("<cidsSearchText>", searchText);
                getLog().fatal(sqlStatement);
                ArrayList<ArrayList> result = ms.performCustomSearch(sqlStatement);
                for (ArrayList al : result) {
                    int cid = (Integer) al.get(0);
                    int oid = (Integer) al.get(1);
                    String name = (String) al.get(2);
                    String string_val = (String) al.get(3);
                    MetaObjectNode mon = new MetaObjectNode((String) key, oid, cid);
                    aln.add(mon);
                }
            }
            return aln;
        } catch (Exception e) {
            getLog().fatal("Problem", e);
            return null;
        }
    }
}
