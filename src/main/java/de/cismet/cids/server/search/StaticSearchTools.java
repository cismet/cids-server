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
import java.util.HashSet;
import java.util.Set;

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
    public static Set<String> getMetaClassIdsForInStatement(final Collection<MetaClass> classes)
            throws IllegalArgumentException {
        final Set<String> s = new HashSet<>();
        if ((classes == null) || (classes.isEmpty())) {
            throw new IllegalArgumentException("ArrayList of MetaClasses must neither be null nor empty");
        }
        final String domainCheck = classes.iterator().next().getDomain();
        for (final MetaClass mc : classes) {
            s.add(mc.getTableName());
            if (!mc.getDomain().equals(domainCheck)) {
                throw new IllegalArgumentException("ArrayList of MetaClasses must be from the same domain");
            }
        }
        return s;
    }
}
