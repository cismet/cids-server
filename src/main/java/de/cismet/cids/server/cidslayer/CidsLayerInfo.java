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
package de.cismet.cids.server.cidslayer;

import Sirius.server.middleware.types.MetaClass;

/**
 * This Interface provides all information about a cids layer, which are required to visualize and modify objects of a
 * cids layer.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface CidsLayerInfo {

    //~ Methods ----------------------------------------------------------------

    /**
     * The field name of the id.
     *
     * @return  DOCUMENT ME!
     */
    String getIdField();

    /**
     * The field name of the geometry.
     *
     * @return  DOCUMENT ME!
     */
    String getSqlGeoField();

    /**
     * The name of the geometry.
     *
     * @return  DOCUMENT ME!
     */
    String getGeoField();

    /**
     * The selection string.
     *
     * @return  DOCUMENT ME!
     */
    String getSelectString();

    /**
     * The names of the columns.
     *
     * @return  DOCUMENT ME!
     */
    String[] getColumnNames();

    /**
     * The field names of the columns in the database.
     *
     * @return  DOCUMENT ME!
     */
    String[] getColumnPropertyNames();

    /**
     * The primitive datatype of the columns.
     *
     * @return  DOCUMENT ME!
     */
    String[] getPrimitiveColumnTypes();

    /**
     * The primitive datatype of the columns.
     *
     * @return  DOCUMENT ME!
     */
    String[] getSqlColumnNames();

    /**
     * True, iff the given column has a primitve type.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isPrimitive(String column);

    /**
     * True, if the given column references to a catalogue.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isCatalogue(String column);

    /**
     * The class, the given column references to.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Integer getCatalogueClass(String column);

    /**
     * True, if the given column references to a station object.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isStation(String column);

    /**
     * Get information about the station, the given column references to.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    StationInfo getStationInfo(String column);

    /**
     * DOCUMENT ME!
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getReferencedCidsClass(String column);

    /**
     * DOCUMENT ME!
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isReferenceToCidsClass(String column);
    
    String getRestriction();

//    /**
//     *
//     * @return
//     */
//    DefaultAttributeTableRuleSet getAttributeTableRuleSet();
}
