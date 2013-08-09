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

import org.openide.util.Exceptions;

import java.rmi.RemoteException;

import java.text.MessageFormat;

import java.util.Collection;
import java.util.Map;

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
    private String domain = "WRRL_DB_MV"; // NOI18N

    /*private final String query =
     *  "%s ";private final String count = "Select count(*)";*/
    private final String select =
        "Select (select id from cs_class where table_name ilike '%s') as class_id, asEWKT(geom.geo_field)%s from %s, geom where %s = geom.id and geo_field && 'BOX3D(%s %s,%s %s)'::box3d";

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
     * @return  DOCUMENT ME!
     *
     * @throws  SearchException  DOCUMENT ME!
     */
    /*public void setCountOnly(final boolean countOnly) {
     *  this.searchCount = countOnly;}*/

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
            final Map<String, String> options = attribute.getOptions();
            final StringBuilder sb = new StringBuilder();
            for (final Map.Entry<String, String> entry : options.entrySet()) {
                if (!"geom_id".equals(entry.getKey())) {
                    sb.append(", ").append(entry.getValue()).append(" as ").append(entry.getKey());
                }
            }
            final String query = String.format(
                    select,
                    clazz.getTableName(),
                    sb.toString(),
                    clazz.getTableName(),
                    options.get("geom_id"),
                    x1,
                    y1,
                    x2,
                    y2);
            return ms.performCustomSearch(query);
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }
}
