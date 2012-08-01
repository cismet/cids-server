/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaClass;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class MetaClassUtils {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   classes          DOCUMENT ME!
     * @param   localServerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
