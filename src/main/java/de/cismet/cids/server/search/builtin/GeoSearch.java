/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import Sirius.server.sql.PreparableStatement;

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
     * @param  geometry  DOCUMENT ME!
     */
    void setGeometry(Geometry geometry);

    /**
     * DOCUMENT ME!
     *
     * @param   domainKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PreparableStatement getSearchSql(String domainKey);
}
