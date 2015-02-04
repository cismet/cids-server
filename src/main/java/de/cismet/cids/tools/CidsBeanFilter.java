/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.tools;

import de.cismet.cids.dynamics.CidsBean;

/**
 *
 * @author therter
 */
public interface CidsBeanFilter {
    public boolean accept(CidsBean bean);
}
