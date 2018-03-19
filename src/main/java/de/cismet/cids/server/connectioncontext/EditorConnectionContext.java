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

import de.cismet.connectioncontext.AbstractConnectionContext.Category;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class EditorConnectionContext extends AbstractMetaObjectConnectionContext {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RendererConnectionContext object.
     *
     * @param  rendererClass  DOCUMENT ME!
     * @param  mo             DOCUMENT ME!
     */
    public EditorConnectionContext(final Class rendererClass, final MetaObject mo) {
        super(Category.RENDERER, rendererClass.getCanonicalName(), mo);
    }
}
