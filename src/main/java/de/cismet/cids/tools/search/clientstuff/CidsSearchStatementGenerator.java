/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.search.clientstuff;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.search.SearchOption;

import java.util.Collection;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public interface CidsSearchStatementGenerator {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<MetaClass> getPossibleResultClasses();

    /**
     * DOCUMENT ME!
     *
     * @param   parameterMap  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<SearchOption> getParameterizedSearchStatement(Map<String, Object> parameterMap);
}
