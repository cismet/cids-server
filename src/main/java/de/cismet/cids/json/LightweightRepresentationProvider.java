/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.json;

import de.cismet.cids.dynamics.CidsBean;

/**
 *
 * @author thorsten
 */
public interface LightweightRepresentationProvider {
    String getLightweightrepresentation(CidsBean bean);
}
