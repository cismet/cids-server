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
package de.cismet.cids.dynamics;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface CidsBeanCollectionStore {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<CidsBean> getCidsBeans();
    /**
     * DOCUMENT ME!
     *
     * @param  beans  DOCUMENT ME!
     */
    void setCidsBeans(Collection<CidsBean> beans);
}
