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
 *
 * @author Sebastian
 */
public interface RMessenger extends Remote {
    void sendMessage(String message,String title) throws RemoteException;    
    void test() throws RemoteException;
}
