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

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationInfo implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private boolean stationLine;
    private boolean fromStation;
    private String routeTable;
    private int lineId;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationInfo object.
     */
    public StationInfo() {
    }

    /**
     * Creates a new StationInfo object.
     *
     * @param  stationLine  DOCUMENT ME!
     * @param  fromStation  DOCUMENT ME!
     * @param  routeTable   DOCUMENT ME!
     * @param  lineId       DOCUMENT ME!
     */
    public StationInfo(final boolean stationLine,
            final boolean fromStation,
            final String routeTable,
            final int lineId) {
        this.stationLine = stationLine;
        this.fromStation = fromStation;
        this.routeTable = routeTable;
        this.lineId = lineId;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the stationLine
     */
    public boolean isStationLine() {
        return stationLine;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  stationLine  the stationLine to set
     */
    public void setStationLine(final boolean stationLine) {
        this.stationLine = stationLine;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the routeTable
     */
    public String getRouteTable() {
        return routeTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  routeTable  the routeTable to set
     */
    public void setRouteTable(final String routeTable) {
        this.routeTable = routeTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the lineId
     */
    public int getLineId() {
        return lineId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lineId  the lineId to set
     */
    public void setLineId(final int lineId) {
        this.lineId = lineId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the fromStation
     */
    public boolean isFromStation() {
        return fromStation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromStation  the fromStation to set
     */
    public void setFromStation(final boolean fromStation) {
        this.fromStation = fromStation;
    }
}
