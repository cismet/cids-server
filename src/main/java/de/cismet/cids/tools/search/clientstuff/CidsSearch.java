/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.search.clientstuff;

import javax.swing.ImageIcon;

import de.cismet.cids.server.search.MetaObjectNodeServerSearch;

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
    MetaObjectNodeServerSearch getServerSearch();

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
}
