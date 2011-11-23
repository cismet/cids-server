/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.search.clientstuff;

import Sirius.server.search.CidsServerSearch;

import javax.swing.ImageIcon;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public interface CidsSearch {

    //~ Methods ----------------------------------------------------------------

    /**
     * Collection<MetaClass> getPossibleResultClasses();
     *
     * @return  DOCUMENT ME!
     */
    CidsServerSearch getServerSearch();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ImageIcon getIcon();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getHint();
}
