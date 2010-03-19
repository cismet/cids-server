/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.fromstring;
import Sirius.server.localserver.object.Object;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface StringCreateable {

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a new instance of FromString.
     *
     * @param   objectRepresentation  DOCUMENT ME!
     * @param   mo                    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    java.lang.Object fromString(String objectRepresentation, java.lang.Object mo) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isStringCreateable();
}
