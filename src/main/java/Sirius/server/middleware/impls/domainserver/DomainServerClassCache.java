/*
 * Copyright (C) 2012 cismet GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Sirius.server.middleware.impls.domainserver;

import Sirius.server.middleware.types.MetaClass;
import de.cismet.cids.utils.MetaClassUtils;
import java.util.HashMap;

/**
 *
 * @author thorsten
 */
public class DomainServerClassCache {

    private static DomainServerClassCache instance;
    private HashMap allClassesById = null;
    private HashMap allClassesByTableName = null;

    private DomainServerClassCache() {
    }

    public static DomainServerClassCache getInstance() {
        if (instance == null) {
            synchronized (DomainServerClassCache.class) {
                if (instance == null) {
                    instance = new DomainServerClassCache();
                }
            }
        }
        return instance;
    }

    public void setAllClasses(MetaClass[] classArray) {
        this.allClassesById =MetaClassUtils.getClassHashtable(classArray, "local");
        this.allClassesByTableName=MetaClassUtils.getClassByTableNameHashtable(classArray);
    }


    HashMap getAllClasses() {
        return allClassesById;
    }

    MetaClass getMetaClass(String tableName) {
        return (MetaClass)allClassesByTableName.get(tableName);
    }

    MetaClass getMetaClass(int classId) {
        return (MetaClass)allClassesById.get(classId);
    }
}
