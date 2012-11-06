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
public interface GeoSearch extends MetaObjectNodeServerSearch {

    //~ Methods ----------------------------------------------------------------

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

    /**
     * DOCUMENT ME!
     *
     * @param   domainKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSearchSql(String domainKey);
}
