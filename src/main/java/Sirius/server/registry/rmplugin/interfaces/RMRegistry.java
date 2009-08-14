/*
 * RMRegisterService.java
 *
 * Created on 23. November 2006, 16:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.registry.rmplugin.interfaces;


import Sirius.server.registry.rmplugin.exception.UnableToDeregisterException;
import Sirius.server.registry.rmplugin.util.RMInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Sebastian
 */
public interface RMRegistry extends Remote{
    
     /**
     * This Method registers an RMPlugin in the RMRegistry. In this process a mapping
     * between the full qualified identifier (username@group@domain) and the delivered
     * RMInfo Object which contains all necessary information to connect to the 
     * RMPlugin. This information more specific the mapping will be saved in a hashmap.
     * @param info This object contains all the information of the object who is asking
     * for registration.
     */
    void register(RMInfo info) throws RemoteException;
    
    /**
     * This method is the pendant to the register method and deregisters a rmplugin
     * by deleting the mapping between qualified name and RMInfo object.
     * @param info The object which should be removed from the RMRegistry
     * @throws Sirius.server.registry.rmplugin.exception.UnableToDeregisterException This Exception is thrown if the object which should be deregister does not exist
     * in the RMRegistry
     */
    void deregister(RMInfo info) throws RemoteException, UnableToDeregisterException;
}
