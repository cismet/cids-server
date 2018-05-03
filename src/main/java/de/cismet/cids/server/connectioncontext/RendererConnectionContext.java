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

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RendererConnectionContext extends AbstractMetaObjectConnectionContext {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RendererConnectionContext object.
     *
     * @param  rendererClass  DOCUMENT ME!
     * @param  mo             DOCUMENT ME!
     */
    public RendererConnectionContext(final Class rendererClass, final MetaObject mo) {
        super(Category.RENDERER, rendererClass.getCanonicalName(), mo);
    }

    /**
     * Creates a new RendererConnectionContext object.
     *
     * @param  rendererClass  DOCUMENT ME!
     * @param  mos            DOCUMENT ME!
     */
    public RendererConnectionContext(final Class rendererClass, final Collection<MetaObject> mos) {
        super(
            Category.RENDERER,
            rendererClass.getCanonicalName(),
            ((mos != null) && !mos.isEmpty()) ? mos.iterator().next().getMetaClass() : null,
            mos);
    }
}
