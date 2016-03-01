/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.search.store;

/**
 * Stellt Basisinformationen fuer Suchprofile und Profilergebnisse zur Verfuegung. *
 *
 * @version  $Revision$, $Date$
 */

public interface Info {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  InfoId *
     */
    int getID();
    /**
     * DOCUMENT ME!
     *
     * @return  InfoName *
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  HeimatLocalServerName*
     */
    String getDomain();
}
