/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.search.builtin;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cids.tools.CidsLayerUtil;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    double x1;
    double x2;
    double y1;
    double y2;
    int classId;
    int srid;
    private String domain = ""; // NOI18N
    private boolean countOnly = false;
    private String query;
    private int offset;
    private int limit;
    private String[] orderBy;
    private boolean exactSearch = false;
    private CidsLayerInfo layerInfo;

    private final String selectFromView = "%s where geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";
    private final String selectFromViewExactly =
        "%1$s where geo_field && setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d) and st_intersects(geo_field::GEOMETRY, setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d))";
    private final String selectAll = "%s";
    private final String selectCountFromView =
        "Select count(*) from (%s where geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)) as tmp";
    private final String selectTotalCountFromView =
        "Select count(*) from (%s) as tmp";
    private final String selectFromViewWithRestriction = "%s where %s and geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";
    private final String selectFromViewExactlyWithRestriction =
        "%1$s where %7$s and geo_field && setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d) and st_intersects(geo_field::GEOMETRY, setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d))";
    private final String selectAllWithRestriction = "%s WHERE %s";
    private final String selectCountFromViewWithRestriction =
        "Select count(*) from (%s where %s and geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)) as tmp";
    private final String selectTotalCountFromViewWithRestriction =
        "Select count(*) from (%s where %s) as tmp";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerSearchStatement object.
     */
    public CidsLayerSearchStatement() {
    }

    /**
     * Creates a new CidsLayerSearchStatement object.
     *
     * @param  clazz  DOCUMENT ME!
     */
    public CidsLayerSearchStatement(final MetaClass clazz, User user) {
        classId = clazz.getID();
        domain = clazz.getDomain();
        layerInfo = CidsLayerUtil.getCidsLayerInfo(clazz, user);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  x1  DOCUMENT ME!
     */
    public void setX1(final double x1) {
        this.x1 = x1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y1  DOCUMENT ME!
     */
    public void setY1(final double y1) {
        this.y1 = y1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x2  DOCUMENT ME!
     */
    public void setX2(final double x2) {
        this.x2 = x2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y2  DOCUMENT ME!
     */
    public void setY2(final double y2) {
        this.y2 = y2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClassId() {
        return classId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  countOnly  DOCUMENT ME!
     */
    public void setCountOnly(final boolean countOnly) {
        this.countOnly = countOnly;
    }

    /*public void setClassId(final String className) {
     *  this.classId = className;}*/

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(domain);
        try {
            final StringBuilder queryString;
            String restriction = layerInfo.getRestriction();
            
            if ((query != null) && !query.equals("")) {
                if (restriction == null) {
                    restriction = query;
                } else {
                    restriction = "(" + restriction + ") AND (" + query + ")";
                }
            }

            if (countOnly) {
                if ((x1 == 0.0) && (x2 == 0.0) && (y1 == 0.0) && (y2 == 0.0)) {
                    // retrieve all features
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(selectTotalCountFromViewWithRestriction, layerInfo.getSelectString(), restriction));
                    } else {
                        queryString = new StringBuilder(String.format(selectTotalCountFromView, layerInfo.getSelectString()));
                    }
                } else {
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(
                                selectCountFromViewWithRestriction,
                                layerInfo.getSelectString(),
                                restriction,
                                x1,
                                y1,
                                x2,
                                y2,
                                srid));
                    } else {
                        queryString = new StringBuilder(String.format(
                                selectCountFromView,
                                layerInfo.getSelectString(),
                                x1,
                                y1,
                                x2,
                                y2,
                                srid));
                    }
                }
            } else {
                if ((x1 == 0.0) && (x2 == 0.0) && (y1 == 0.0) && (y2 == 0.0)) {
                    // retrieve all features
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(selectAllWithRestriction, layerInfo.getSelectString(), restriction));
                    } else {
                        queryString = new StringBuilder(String.format(selectAll, layerInfo.getSelectString()));
                    }
                } else if (exactSearch) {
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(
                                    selectFromViewExactlyWithRestriction,
                                    layerInfo.getSelectString(),
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid,
                                    restriction));
                    } else {
                        queryString = new StringBuilder(String.format(
                                    selectFromViewExactly,
                                    layerInfo.getSelectString(),
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid));
                    }
                } else {
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(
                                    selectFromViewWithRestriction,
                                    layerInfo.getSelectString(),
                                    restriction,
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid));
                    } else {
                        queryString = new StringBuilder(String.format(
                                    selectFromView,
                                    layerInfo.getSelectString(),
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid));
                    }
                }
            }

            if ((orderBy != null) && (orderBy.length > 0)) {
                boolean firstAttr = true;
                queryString.append(" ORDER BY ");
                for (final String attr : orderBy) {
                    if (firstAttr) {
                        firstAttr = false;
                    } else {
                        queryString.append(",");
                    }
                    queryString.append(attr);
                }
            }

            if (limit > 0) {
                queryString.append(" LIMIT ").append(limit);
            }

            if (offset > 0) {
                queryString.append(" OFFSET ").append(offset);
            }

            LOG.info(queryString.toString());
            final ArrayList<ArrayList> result = ms.performCustomSearch(queryString.toString());

            return result;
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  defaultCrsAlias  DOCUMENT ME!
     */
    public void setSrid(final int defaultCrsAlias) {
        srid = defaultCrsAlias;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  query  the query to set
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  offset  the offset to set
     */
    public void setOffset(final int offset) {
        this.offset = offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  limit  the limit to set
     */
    public void setLimit(final int limit) {
        this.limit = limit;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the orderBy
     */
    public String[] getOrderBy() {
        return orderBy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  orderBy  the orderBy to set
     */
    public void setOrderBy(final String[] orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the exactSearch
     */
    public boolean isExactSearch() {
        return exactSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  exactSearch  the exactSearch to set
     */
    public void setExactSearch(final boolean exactSearch) {
        this.exactSearch = exactSearch;
    }
}
