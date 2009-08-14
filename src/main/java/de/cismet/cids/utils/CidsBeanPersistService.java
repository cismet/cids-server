/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cids.utils;

import de.cismet.cids.dynamics.CidsBean;

/**
 *
 * @author thorsten
 */
public interface CidsBeanPersistService {
    public CidsBean persistCidsBean(CidsBean cidsBean) throws Exception;
}
