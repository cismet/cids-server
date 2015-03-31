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
package de.cismet.cids.server.actions;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface JsonableServerAction extends ServerAction {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   body  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    String bodyToJson(final Object body) throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    Object jsonToBody(final String json) throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    String paramsToJson(final ServerActionParameter[] params) throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    ServerActionParameter[] jsonToParams(final String json) throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param   result  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    String resultToJson(final Object result) throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    Object jsonToResult(final String json) throws IOException;
}
