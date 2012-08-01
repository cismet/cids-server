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
package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaClass;
import java.util.HashMap;

/**
 *
 * @author thorsten
 */
public class MetaClassUtils {
    
    
    
    public static HashMap getClassHashtable(final MetaClass[] classes, final String localServerName) {
        final HashMap classHash = new HashMap();
        for (int i = 0; i < classes.length; i++) {
            final String key = localServerName + classes[i].getID();
            if (!classHash.containsKey(key)) {
                classHash.put(key, classes[i]);
            }
        }
        return classHash;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static HashMap getClassByTableNameHashtable(final MetaClass[] classes) {
        final HashMap classHash = new HashMap();
        for (final MetaClass mc : classes) {
            final String key = mc.getTableName().toLowerCase();
            if (!classHash.containsKey(key)) {
                classHash.put(key, mc);
            }
        }
        return classHash;
    }
}
