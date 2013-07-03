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

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class IntraObjectCacheEnabledCidsBeanJsonSerializer extends CidsBeanJsonSerializer {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntraObjectCacheEnabledCidsBeanJsonSerializer object.
     */
    public IntraObjectCacheEnabledCidsBeanJsonSerializer() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected boolean isIntraObjectCacheEnabled() {
        return true;
    }
}
