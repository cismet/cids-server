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
package de.cismet.cids.server.connectioncontext;

import Sirius.server.middleware.types.MetaObject;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RendererConnectionContext extends ClientConnectionContext {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RendererConnectionContext object.
     *
     * @param  mo  DOCUMENT ME!
     */
    public RendererConnectionContext(final MetaObject mo) {
        super(Category.RENDERER, constructContext(mo));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String constructContext(final MetaObject mo) {
        return "Renderer: " + mo.getId() + "@" + mo.getMetaClass().getName() + "(" + mo.getMetaClass().getId() + ")";
    }
}
