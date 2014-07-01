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
import de.cismet.cids.server.cidslayer.CidsLayerInfo;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.SearchException;
import java.lang.reflect.Constructor;

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

//    private String envelopeQuery = "select st_asText(st_extent(geo_field)) from %s;";
//    private String envelopeQuery = "select st_asText(st_extent(tmp.%s)) from (%s) as tmp";
    private String envelopeQuery = "select st_asText(st_extent(%s)) %s";
//    private String initString = "select column_name, data_type from information_schema.columns where table_schema = '%s' and table_name = '%s' order by ordinal_position ASC";
    private int classId;
    private String domain;
    private CidsLayerInfo layerInfo; 

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
        final ClassAttribute attr = clazz.getClassAttribute("cidsLayer");

        if (attr != null) {
            String className = attr.getValue().toString();

            try {
                Class classObject = Class.forName(className);
                Constructor c = classObject.getConstructor(MetaClass.class);
                Object info = c.newInstance(clazz);

                if (info instanceof CidsLayerInfo) {
                    layerInfo = (CidsLayerInfo)info;
                }
            } catch (Exception e) {
//                logger.error("Cannot instantiate CidsLayerInfo class: " + className, e);
            }
        }        
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
//            final MetaClass clazz = ms.getClass(getUser(), classId);
//            final ClassAttribute attribute = clazz.getClassAttribute("cidsLayer");
            
//            if (!(attribute.getValue() instanceof String)) {
//                LOG.error("Could not read layer view for metaclass " + clazz.getTableName());
//                return null;
//            }

//            final String viewName = (String)attribute.getValue();
//            String view = viewName;
//            String schemaName = "public";
//            int dotPosition = viewName.indexOf(".");
//
//            if (dotPosition != -1) {
//                view = viewName.substring(dotPosition + 1);
//                schemaName = viewName.substring(0, dotPosition);
//            }
            
            
//            final ArrayList<ArrayList> columns = ms.performCustomSearch(String.format(initString, schemaName, view));
            
            String tables = layerInfo.getSelectString().substring(layerInfo.getSelectString().indexOf("from "));
            final String query = String.format(envelopeQuery, layerInfo.getSqlGeoField(), tables);
            final ArrayList<ArrayList> envelope = ms.performCustomSearch(query);

//            if ((envelope != null) && (envelope.size() > 0)) {
//                columns.add(envelope.get(0));
//            }

            return envelope;
        } catch (RemoteException ex) {
            LOG.error("Error in customSearch", ex);
        }
        return null;
    }
}
