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
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerInitStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerInitStatement.class);

    //~ Instance fields --------------------------------------------------------

    private String envelopeQuery = "select st_asText(st_extent(geo_field)) from %s;";
    private int classId;
    private String domain;

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
     */
    public CidsLayerInitStatement(final MetaClass clazz) {
        classId = clazz.getID();
        domain = clazz.getDomain();
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
            final ArrayList<ArrayList> columns = ms.performCustomSearch(
                    "select column_name, data_type from information_schema.columns where table_name = '"
                            + viewName
                            + "' order by ordinal_position ASC");

            final String query = String.format(envelopeQuery, viewName);
            final ArrayList<ArrayList> envelope = ms.performCustomSearch(query);
            ;

            if ((envelope != null) && (envelope.size() > 0)) {
                columns.add(envelope.get(0));
            }

            return columns;
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }
}
