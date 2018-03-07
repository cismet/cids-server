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

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!s.
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SearchConnectionContext extends ConnectionContext {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RendererConnectionContext object.
     *
     * @param  canonicalSearchClassName  DOCUMENT ME!
     */
    public SearchConnectionContext(final String canonicalSearchClassName) {
        super(Category.SEARCH, canonicalSearchClassName);
    }
}
