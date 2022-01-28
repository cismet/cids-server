/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.configattr;

import org.openide.util.lookup.ServiceProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */

@ServiceProvider(service = ConfigAttrsAggregator.class)
public class FirstonlyConfigAttrsAggregator implements ConfigAttrsAggregator {

    //~ Static fields/initializers ---------------------------------------------

    public static String KEY = "first";

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String aggregate(final String[] configAttrs) {
        return ((configAttrs == null) || (configAttrs.length == 0)) ? null : configAttrs[0];
    }
}
