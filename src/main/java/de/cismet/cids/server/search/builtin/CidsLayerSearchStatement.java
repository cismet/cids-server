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

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContextProvider;
import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cids.tools.CidsLayerUtil;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsLayerSearchStatement extends AbstractCidsServerSearch implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerSearchStatement.class);

    private static final String selectFromView = "%s where %s && st_setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";
    private static final String selectFromViewExactly =
        "%1$s where %2$s && st_setSrid('BOX3D(%3$s %4$s,%5$s %6$s)'::box3d, %7$d) and st_intersects(%2$s, st_setSrid('BOX3D(%3$s %4$s,%5$s %6$s)'::box3d, %7$d))";
    private static final String selectAll = "%s";
    private static final String selectCountFromView =
        "Select count(*) from (%s where %s && st_setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)) as tmp";
    private static final String selectTotalCountFromView = "Select count(*) from (%s) as tmp";
    private static final String selectFromViewWithRestriction =
        "%s where %s and %s && st_setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)";
    private static final String selectFromViewExactlyWithRestriction =
        "%1$s where %8$s and %2$s && st_setSrid('BOX3D(%3$s %4$s,%5$s %6$s)'::box3d, %7$d) and st_intersects(%2$s, st_setSrid('BOX3D(%3$s %4$s,%5$s %6$s)'::box3d, %7$d))";
    private static final String selectAllWithRestriction = "%s WHERE %s";
    private static final String selectCountFromViewWithRestriction =
        "Select count(*) from (%s where %s and %s && st_setSrid('BOX3D(%s %s,%s %s)'::box3d, %d)) as tmp";
    private static final String selectTotalCountFromViewWithRestriction = "Select count(*) from (%s where %s) as tmp";

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
    private boolean compressed = false;

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
     * @param  user   DOCUMENT ME!
     */
    public CidsLayerSearchStatement(final MetaClass clazz, final User user) {
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
                        queryString = new StringBuilder(String.format(
                                    selectTotalCountFromViewWithRestriction,
                                    layerInfo.getSelectString(),
                                    restriction));
                    } else {
                        queryString = new StringBuilder(String.format(
                                    selectTotalCountFromView,
                                    layerInfo.getSelectString()));
                    }
                } else {
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(
                                    selectCountFromViewWithRestriction,
                                    layerInfo.getSelectString(),
                                    restriction,
                                    layerInfo.getSqlGeoField(),
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid));
                    } else {
                        queryString = new StringBuilder(String.format(
                                    selectCountFromView,
                                    layerInfo.getSelectString(),
                                    layerInfo.getSqlGeoField(),
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
                        queryString = new StringBuilder(String.format(
                                    selectAllWithRestriction,
                                    layerInfo.getSelectString(),
                                    restriction));
                    } else {
                        queryString = new StringBuilder(String.format(selectAll, layerInfo.getSelectString()));
                    }
                } else if (exactSearch) {
                    if (restriction != null) {
                        queryString = new StringBuilder(String.format(
                                    selectFromViewExactlyWithRestriction,
                                    layerInfo.getSelectString(),
                                    layerInfo.getSqlGeoField(),
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
                                    layerInfo.getSqlGeoField(),
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
                                    layerInfo.getSqlGeoField(),
                                    x1,
                                    y1,
                                    x2,
                                    y2,
                                    srid));
                    } else {
                        queryString = new StringBuilder(String.format(
                                    selectFromView,
                                    layerInfo.getSelectString(),
                                    layerInfo.getSqlGeoField(),
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
            ArrayList<ArrayList> result = ms.performCustomSearch(queryString.toString(), getConnectionContext());

            if (compressed) {
                try {
                    final ByteArrayOutputStream iout = new ByteArrayOutputStream();
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final ObjectOutputStream oout = new ObjectOutputStream(iout);
                    oout.writeObject(result);
                    oout.flush();
                    final GZIPOutputStream zstream = new GZIPOutputStream(bout);
                    zstream.write(iout.toByteArray());
                    zstream.finish();

                    // FileOutputStream foutUncompressed = new FileOutputStream("/home/therter/tmp/uncomp.out");
                    // foutUncompressed.write(iout.toByteArray());
                    // foutUncompressed.close();
                    //
                    // FileOutputStream foutCompressed = new FileOutputStream("/home/therter/tmp/comp.out");
                    // foutCompressed.write(bout.toByteArray());
                    // foutCompressed.close();
                    result = new ArrayList<ArrayList>();
                    result.add(new ArrayList());
                    result.get(0).add(bout.toByteArray());
                } catch (Exception e) {
                    LOG.error("error while compressing.-", e);
                }
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

    /**
     * DOCUMENT ME!
     *
     * @return  the compressed
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  compressed  the compressed to set
     */
    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   result  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    public static ArrayList<ArrayList> uncompressResult(final ArrayList<ArrayList> result) throws IOException,
        ClassNotFoundException {
        if ((result != null) && (result.get(0) != null) && (result.get(0).get(0) instanceof byte[])) {
            final GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream((byte[])result.get(0).get(0)));
            final ObjectInputStream uncompressedIn = new ObjectInputStream(gzipIn);
            return (ArrayList<ArrayList>)uncompressedIn.readObject();
        } else {
            return result;
        }
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return ConnectionContext.create(CidsLayerSearchStatement.class.getSimpleName());
    }
}
