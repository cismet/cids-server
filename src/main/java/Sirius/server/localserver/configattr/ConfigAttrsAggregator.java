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
package Sirius.server.localserver.configattr;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ConfigAttrsAggregator {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getKey();

    /**
     * DOCUMENT ME!
     *
     * @param   configAttrs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String aggregate(final String[] configAttrs);
}
