/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.search.builtin.legacy;

import de.cismet.cids.server.search.CidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public interface LightweightMetaObjectsSearch extends CidsServerSearch {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getRepresentationFields();
    /**
     * DOCUMENT ME!
     *
     * @param  representationFields  DOCUMENT ME!
     */
    void setRepresentationFields(String[] representationFields);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getRepresentationPattern();
    /**
     * DOCUMENT ME!
     *
     * @param  representationPattern  DOCUMENT ME!
     */
    void setRepresentationPattern(String representationPattern);
}
