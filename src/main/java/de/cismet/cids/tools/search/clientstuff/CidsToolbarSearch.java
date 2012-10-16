/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.search.clientstuff;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public interface CidsToolbarSearch extends CidsSearch {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  toolbarSearchString  DOCUMENT ME!
     */
    void setSearchParameter(final String toolbarSearchString);

    /**
     * DOCUMENT ME!
     *
     * @param  modifiers  DOCUMENT ME!
     */
    void applyModifiers(final Collection<? extends Modifier> modifiers);
}
