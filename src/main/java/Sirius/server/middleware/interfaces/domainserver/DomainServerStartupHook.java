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
package Sirius.server.middleware.interfaces.domainserver;

/**
 * DOCUMENT ME!
 *
 * @author   daniel
 * @version  $Revision$, $Date$
 */
public interface DomainServerStartupHook {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    enum START_ON_DOMAIN {

        //~ Enum constants -----------------------------------------------------

        ANY
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void domainServerStarted();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDomain();
}
