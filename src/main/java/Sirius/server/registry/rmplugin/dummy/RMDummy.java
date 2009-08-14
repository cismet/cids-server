/*
 * RMDummy.java
 *
 * Created on 27. November 2006, 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.registry.rmplugin.dummy;

import de.cismet.rmplugin.interfaces.RMessenger;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Sebastian
 */
public class RMDummy implements RMessenger {
    
    /** Creates a new instance of RMDummy */
    public RMDummy() {                
    }
    
    Registry reg;
    
    public void initRMI(){
        try {
            reg = LocateRegistry.createRegistry(9001);
            RMessenger rm = (RMessenger) UnicastRemoteObject.exportObject(this);
            reg.rebind("sebastian@altlasten@WUNDA_BLAU_RMPlugin",rm);
            System.out.println("sebastian@altlasten@WUNDA_BLAU_RMPlugin gebunden");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    
    public void initRMI(String name,int port){
        try {
            reg = LocateRegistry.getRegistry(9001);
            RMessenger rm = (RMessenger) UnicastRemoteObject.exportObject(this);
            reg.rebind(name,rm);
            System.out.println(name + " gebunden");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    
    public void shutdown(){
        try {
            reg.unbind("sebastian@altlasten@WUNDA_BLAU_RMPlugin");
            System.out.println("sebastian@altlasten@WUNDA_BLAU_RMPlugin ungebunden");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void shutdown(String name){
        try {
            reg.unbind(name);
            System.out.println(name+" ungebunden");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        }
    }
    

    public void sendMessage(String message, String title) throws RemoteException {
        System.out.println("Message received Title: "+title+" Message: "+message);
    }

    public void test() throws RemoteException {
    }
    
    
    
}
