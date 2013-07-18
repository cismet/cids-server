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

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.rmi.RemoteException;

import java.text.MessageFormat;

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

    private static final String WRRL_DOMAIN = "WRRL_DB_MV"; // NOI18N
    private static final Logger LOG = Logger.getLogger(CidsLayerSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    double x1;
    double x2;
    double y1;
    double y2;
    boolean searchCount = false;

    private final String query =
        "%s from wk_sg, geom where wk_sg.geom = geom.id and geo_field && 'BOX3D(%s %s,%s %s)'::box3d";
    private final String count = "Select count(*)";
    private final String select =
        "Select (select id from cs_class where table_name ilike 'wk_sg') as class_id, wk_sg.id as object_id, asEWKT(geom.geo_field)";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerSearchStatement object.
     */
    public CidsLayerSearchStatement() {
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
     * @param  countOnly  DOCUMENT ME!
     */
    public void setCountOnly(final boolean countOnly) {
        this.searchCount = countOnly;
    }

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(WRRL_DOMAIN);
        try {
            final String query1 = String.format(query, searchCount ? count : select, x1, y1, x2, y2);
            return ms.performCustomSearch(query1);
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }
}
