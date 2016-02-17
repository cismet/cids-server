/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.feature;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cids.dynamics.CidsBean;

/**
 *
 * @author thorsten
 */
public interface CacheGeometryProvider {
    Geometry getCacheGeometry(CidsBean bean);
}
