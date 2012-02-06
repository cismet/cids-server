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
package Sirius.server.search;

import Sirius.server.middleware.types.MetaClass;

import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class StaticSearchTools {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static String getMetaClassIdsForInStatement(final ArrayList<MetaClass> classes)
            throws IllegalArgumentException {
        String s = "";
        if ((classes == null) || (classes.size() == 0)) {
            throw new IllegalArgumentException("ArrayList of MetaClasses must neither be null nor empty");
        }
        final String domainCheck = classes.get(0).getDomain();
        for (final MetaClass mc : classes) {
            s += mc.getID() + ",";
            if (!mc.getDomain().equals(domainCheck)) {
                throw new IllegalArgumentException("ArrayList of MetaClasses must be from the same domain");
            }
        }
        s = s.trim().substring(0, s.length() - 1);
        return "(" + s + ")";
    }
}
