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

import lombok.Getter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class PropertiesServerResource extends TextServerResource {

    //~ Instance fields --------------------------------------------------------

    @Getter private final Class<? extends ServerResourcePropertiesHandler> propertiesClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PropertiesServerResource object.
     *
     * @param  value            DOCUMENT ME!
     * @param  propertiesClass  DOCUMENT ME!
     */
    public PropertiesServerResource(final String value,
            final Class<? extends ServerResourcePropertiesHandler> propertiesClass) {
        super(value);

        this.propertiesClass = propertiesClass;
    }
}
