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

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ClientConnectionContextStore extends ConnectionContextProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void initAfterConnectionContext();

    /**
     * DOCUMENT ME!
     *
     * @param  connectionContext  DOCUMENT ME!
     */
    void setConnectionContext(ClientConnectionContext connectionContext);
    @Override
    ClientConnectionContext getConnectionContext();
}
