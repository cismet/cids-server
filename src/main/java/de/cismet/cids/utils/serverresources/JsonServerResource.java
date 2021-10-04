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
public class JsonServerResource extends TextServerResource {

    //~ Instance fields --------------------------------------------------------

    @Getter private final Class<? extends ServerResourceJsonHandler> jsonHandlerClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JsonServerResource object.
     *
     * @param  value  DOCUMENT ME!
     */
    public JsonServerResource(final String value) {
        this(value, DefaultServerResourceJsonHandler.class);
    }

    /**
     * Creates a new PropertiesServerResource object.
     *
     * @param  value             DOCUMENT ME!
     * @param  jsonHandlerClass  DOCUMENT ME!
     */
    public JsonServerResource(final String value, final Class<? extends ServerResourceJsonHandler> jsonHandlerClass) {
        super(value);

        this.jsonHandlerClass = jsonHandlerClass;
    }
}
