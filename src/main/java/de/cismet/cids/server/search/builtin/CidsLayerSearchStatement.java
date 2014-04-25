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

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

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

    /*private final String query =
     *  "%s ";private final String count = "Select count(*)";*/
    private final String select =
        "Select (select id from cs_class where table_name ilike '%s') as class_id, asEWKT(geom.geo_field)%s from %s, geom where %s = geom.id and geo_field && 'BOX3D(%s %s,%s %s)'::box3d";
    private final String selectFromView =
        "Select * from %s where geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";
    private final String selectFromViewExactly =
        "Select * from %1$s where geo_field && setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d) and st_intersects(geo_field::GEOMETRY, setSrid('BOX3D(%2$s %3$s,%4$s %5$s)'::box3d, %6$d))";
    private final String selectCountFromView =
        "Select count(*) from %s where geo_field && setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";

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
    public CidsLayerSearchStatement(final MetaClass clazz) {
        classId = clazz.getID();
        domain = clazz.getDomain();
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
            final MetaClass clazz = ms.getClass(getUser(), classId);
            final ClassAttribute attribute = clazz.getClassAttribute("cidsLayer");
            if (attribute == null) {
                return null;
            }

            if (!(attribute.getValue() instanceof String)) {
                LOG.error("Could not read layer view for metaclass " + clazz.getTableName());
                return null;
            }
            final String viewName = (String)attribute.getValue();
            final StringBuilder queryString;

            if (countOnly) {
                queryString = new StringBuilder(String.format(selectCountFromView, viewName, x1, y1, x2, y2, srid));
            } else {
                if (exactSearch) {
                    queryString = new StringBuilder(String.format(
                                selectFromViewExactly,
                                viewName,
                                x1,
                                y1,
                                x2,
                                y2,
                                srid));
                } else {
                    queryString = new StringBuilder(String.format(selectFromView, viewName, x1, y1, x2, y2, srid));
                }
            }

            if ((query != null) && !query.equals("")) {
                queryString.append(" AND ").append(query);
            }

            if (limit > 0) {
                queryString.append(" LIMIT ").append(limit);
            }

            if (offset > 0) {
                queryString.append(" OFFSET ").append(offset);
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

            LOG.info(queryString.toString());
            final ArrayList<ArrayList> result = ms.performCustomSearch(queryString.toString());

            if (!countOnly) {
                final ArrayList<ArrayList> columns = ms.performCustomSearch(
                        "select column_name from information_schema.columns where table_name = '"
                                + viewName
                                + "' order by ordinal_position ASC");

                final ArrayList columnNames = new ArrayList();
                for (final ArrayList column : columns) {
                    columnNames.add(column.get(0));
                }
                LOG.info("Column names are " + columnNames.toString());
                result.add(0, columnNames);
            }
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
