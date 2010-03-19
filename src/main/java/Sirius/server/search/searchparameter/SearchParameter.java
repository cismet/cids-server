/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.search.searchparameter;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public interface SearchParameter {

    //~ Instance fields --------------------------------------------------------

    String CLASSIDS = "cs_classids";

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getKey();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getValue();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDescription();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    Collection values() throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param  parameter  DOCUMENT ME!
     */
    void setValue(Object parameter);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isQueryResult();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getQueryPosition();
}
