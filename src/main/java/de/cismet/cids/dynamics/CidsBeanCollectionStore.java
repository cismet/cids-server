/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cids.dynamics;

import java.util.Collection;

/**
 *
 * @author thorsten
 */
public interface CidsBeanCollectionStore {
    public Collection<CidsBean> getCidsBeans();
    public void setCidsBeans(Collection<CidsBean> beans);
}
