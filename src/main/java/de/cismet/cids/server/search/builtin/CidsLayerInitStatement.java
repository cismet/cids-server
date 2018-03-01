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

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.connectioncontext.ServerConnectionContext;
import de.cismet.cids.server.connectioncontext.ServerConnectionContextProvider;
import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cids.tools.CidsLayerUtil;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerInitStatement extends AbstractCidsServerSearch implements ServerConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerInitStatement.class);

    //~ Instance fields --------------------------------------------------------

    private final String envelopeQuery = "select st_asText(st_extent(%s)) %s";
    private final String geometryTypeQuery = "SELECT distinct st_geometryType(%1$s) %2$s  where %1$s is not null";
    private int classId;
    private String domain;
    private CidsLayerInfo layerInfo;
    private String queryString = null;

    private ServerConnectionContext serverConnectionContext = ServerConnectionContext.create(getClass()
                    .getSimpleName());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerSearchStatement object.
     */
    public CidsLayerInitStatement() {
    }

    /**
     * Creates a new CidsLayerSearchStatement object.
     *
     * @param  clazz  DOCUMENT ME!
     * @param  user   DOCUMENT ME!
     */
    public CidsLayerInitStatement(final MetaClass clazz, final User user) {
        classId = clazz.getID();
        domain = clazz.getDomain();
        layerInfo = CidsLayerUtil.getCidsLayerInfo(clazz, user);
    }

    /**
     * Creates a new CidsLayerSearchStatement object.
     *
     * @param  clazz  DOCUMENT ME!
     * @param  user   DOCUMENT ME!
     * @param  query  DOCUMENT ME!
     */
    public CidsLayerInitStatement(final MetaClass clazz, final User user, final String query) {
        classId = clazz.getID();
        domain = clazz.getDomain();
        layerInfo = CidsLayerUtil.getCidsLayerInfo(clazz, user);
        queryString = query;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClassId() {
        return classId;
    }

    @Override
    public Collection performServerSearch() throws SearchException {
        final MetaService ms = (MetaService)getActiveLocalServers().get(domain);
        try {
            final String tables = layerInfo.getSelectString().substring(layerInfo.getSelectString().indexOf("from "));
            String query;

            if (queryString == null) {
                query = String.format(envelopeQuery, layerInfo.getSqlGeoField(), tables);
            } else {
                query = String.format(envelopeQuery, layerInfo.getSqlGeoField(), tables);
                if (query.toLowerCase().contains("where")) {
                    query = query + " and (" + envelopeQuery + ")";
                } else {
                    query = query + " WHERE (" + envelopeQuery + ")";
                }
            }
            final ArrayList<ArrayList> envelope = ms.performCustomSearch(query, getServerConnectionContext());
            final String typeQuery = String.format(geometryTypeQuery, layerInfo.getSqlGeoField(), tables);

            final ArrayList<ArrayList> geometryType = ms.performCustomSearch(typeQuery, getServerConnectionContext());
            String type = null;

            if (geometryType.size() == 1) {
                final ArrayList list = geometryType.get(0);

                if ((list.size() == 1) && (list.get(0) != null)) {
                    type = list.get(0).toString();
                }
            }

            if (type != null) {
                envelope.get(0).add(type);
            }

            return envelope;
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }

    @Override
    public ServerConnectionContext getServerConnectionContext() {
        return serverConnectionContext;
    }

    @Override
    public void setServerConnectionContext(final ServerConnectionContext serverConnectionContext) {
        this.serverConnectionContext = serverConnectionContext;
    }
}
