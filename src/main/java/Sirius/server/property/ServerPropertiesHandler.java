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
package Sirius.server.property;

/**
 * This interface can be used, if a DomainServerStartupHook needs the ServerProperties.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface ServerPropertiesHandler {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  properties  DOCUMENT ME!
     */
    void setServerProperties(ServerProperties properties);
}
