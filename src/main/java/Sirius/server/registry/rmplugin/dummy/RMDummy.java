/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RMDummy.java
 *
 * Created on 27. November 2006, 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin.dummy;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import de.cismet.rmplugin.interfaces.RMessenger;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class RMDummy implements RMessenger {

    //~ Instance fields --------------------------------------------------------

    Registry reg;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RMDummy.
     */
    public RMDummy() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void initRMI() {
        try {
            reg = LocateRegistry.createRegistry(9001);
            final RMessenger rm = (RMessenger)UnicastRemoteObject.exportObject(this);
            reg.rebind("sebastian@altlasten@WUNDA_BLAU_RMPlugin", rm);              // NOI18N
            System.out.println("sebastian@altlasten@WUNDA_BLAU_RMPlugin gebunden"); // NOI18N
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     * @param  port  DOCUMENT ME!
     */
    public void initRMI(final String name, final int port) {
        try {
            reg = LocateRegistry.getRegistry(9001);
            final RMessenger rm = (RMessenger)UnicastRemoteObject.exportObject(this);
            reg.rebind(name, rm);
            System.out.println(name + " gebunden"); // NOI18N
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void shutdown() {
        try {
            reg.unbind("sebastian@altlasten@WUNDA_BLAU_RMPlugin");                    // NOI18N
            System.out.println("sebastian@altlasten@WUNDA_BLAU_RMPlugin ungebunden"); // NOI18N
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void shutdown(final String name) {
        try {
            reg.unbind(name);
            System.out.println(name + " ungebunden"); // NOI18N
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendMessage(final String message, final String title) throws RemoteException {
        System.out.println("Message received Title: " + title + " Message: " + message); // NOI18N
    }

    @Override
    public void test() throws RemoteException {
    }
}
