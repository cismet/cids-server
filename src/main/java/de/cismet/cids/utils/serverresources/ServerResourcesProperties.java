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
package de.cismet.cids.utils.serverresources;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class ServerResourcesProperties extends Properties {

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Properties> targetPropertiesFiles = new HashMap<>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getProperty(final String key) {
        targetPropertiesFiles.put(null, this);
        final String value = super.getProperty(key);
        return (value != null) ? ServerResourcesLoader.substitute(value, targetPropertiesFiles) : null;
    }
}
