/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RemoteMessenger.java
 *
 * Created on 22. November 2006, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.rmplugin.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public interface RMessenger extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   message  DOCUMENT ME!
     * @param   title    DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void sendMessage(String message, String title) throws RemoteException;
    /**
     * DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void test() throws RemoteException;
}
