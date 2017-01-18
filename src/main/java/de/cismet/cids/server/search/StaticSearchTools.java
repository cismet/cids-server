/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search;

import Sirius.server.middleware.types.MetaClass;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class StaticSearchTools {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StaticSearchTools object.
     */
    private StaticSearchTools() {
    }

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
    public static String getMetaClassIdsForInStatement(final Collection<MetaClass> classes)
            throws IllegalArgumentException {
        String s = "";
        if ((classes == null) || (classes.isEmpty())) {
            throw new IllegalArgumentException("ArrayList of MetaClasses must neither be null nor empty");
        }
        final String domainCheck = classes.iterator().next().getDomain();
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
