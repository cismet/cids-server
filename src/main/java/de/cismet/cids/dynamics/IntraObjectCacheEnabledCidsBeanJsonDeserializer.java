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
public class IntraObjectCacheEnabledCidsBeanJsonDeserializer extends CidsBeanJsonDeserializer {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IntraObjectCacheEnabledCidsBeanJsonDeserializer object.
     */
    public IntraObjectCacheEnabledCidsBeanJsonDeserializer() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected boolean isIntraObjectCacheEnabled() {
        return true;
    }
}
