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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractConnectionContext<C extends Object> implements ConnectionContext<C> {

    //~ Static fields/initializers ---------------------------------------------

    protected static final transient boolean SHOW_FULL_DEPRECATED_STACKTRACE = false;

    //~ Instance fields --------------------------------------------------------

    private final Category category;
    private final C content;
    private final HashMap<String, String> additionalFields = new HashMap<>();
}
