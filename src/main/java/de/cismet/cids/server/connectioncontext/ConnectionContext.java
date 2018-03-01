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

import java.io.Serializable;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ConnectionContext<C extends Object> extends Serializable {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Origin {

        //~ Enum constants -----------------------------------------------------

        SERVER, CLIENT, UNKNOWN
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Category {

        //~ Enum constants -----------------------------------------------------

        EDITOR, RENDERER, CATALOGUE, ACTION, SEARCH, LEGACY, OTHER, DEPRECATED
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    C getContent();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Origin getOrigin();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Category getCategory();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map<String, String> getAdditionalFields();
}
