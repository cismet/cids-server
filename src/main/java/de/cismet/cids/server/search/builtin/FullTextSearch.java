/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.server.search.MetaObjectNodeServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FullTextSearch extends MetaObjectNodeServerSearch {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSearchText();
    /**
     * DOCUMENT ME!
     *
     * @param  searchText  DOCUMENT ME!
     */
    void setSearchText(String searchText);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isCaseSensitive();
    /**
     * DOCUMENT ME!
     *
     * @param  caseSensitive  DOCUMENT ME!
     */
    void setCaseSensitive(boolean caseSensitive);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Geometry getGeometry();
    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    void setGeometry(Geometry geom);
}
