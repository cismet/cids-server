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

import com.fasterxml.jackson.core.JsonFactory;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class DefaultServerResourceJsonHandler implements ServerResourceJsonHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    //~ Methods ----------------------------------------------------------------

    @Override
    public JsonFactory getJsonFactory() throws Exception {
        return JSON_FACTORY;
    }
}
