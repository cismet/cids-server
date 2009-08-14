/*
 * SystemServiceImpl.java
 *
 * Created on 25. September 2003, 12:42
 */

package Sirius.server.middleware.impls.proxy;

import java.rmi.*;
import java.rmi.server.*;
import Sirius.server.*;
import Sirius.server.naming.NameServer;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.util.image.*;

/**
 *
 * @author  awindholz
 */
public class SystemServiceImpl
 {
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    private NameServer nameServer;
    private java.util.Hashtable activeLocalServers;
    
    /** Creates a new instance of SystemServiceImpl */
    public SystemServiceImpl(java.util.Hashtable activeLocalServers, NameServer nameServer)
    throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
    }
    
    public Image[] getDefaultIcons(String lsName) throws RemoteException {
        
        logger.info("Info <CS> getDefIcons from "+lsName);
        Image[] i = new Image[0];
        Sirius.server.middleware.interfaces.domainserver.SystemService s = null;
        
        try {
            s=(Sirius.server.middleware.interfaces.domainserver.SystemService)activeLocalServers.get(lsName.trim());
            i= s.getDefaultIcons();
            
            logger.debug("image[] "+i);
        } catch(Exception e) {
          logger.error("Info <CS> getDefIcons from "+lsName+" failed",e);
                     
            throw new RemoteException("getDefIcons(lsName) failed",e);
        }
        
        return i;
    }
    
    public Image[] getDefaultIcons() throws RemoteException {
        Image[] i =new Image[0];
       Sirius.server.middleware.interfaces.domainserver.SystemService s = null;
        
        try {
            if(activeLocalServers.size()>0) {
                s=(Sirius.server.middleware.interfaces.domainserver.SystemService)activeLocalServers.values().iterator().next();
               logger.debug("<CS> getDefIcons");
                i= s.getDefaultIcons();
            }
            else
                throw new Exception("kein LocalServer bei der Registry eingetragen!");
            
        } catch(Exception e) {
            logger.error("Info <CS> getDefIcons failed",e);
           
            throw new RemoteException("getDefIcons(void) fehlgeschlagen",e);
        }
        
        return i;
    }
    
   
    
//    public Sirius.Server.Server getTranslationServer() throws RemoteException {
//        Sirius.Server.Server[] ts = nameServer.getServers(ServerType.TRANSLATIONSERVER);
//        
//        return ts[ts.length-1]; // der zuletzt angemeldete
//    }
     
}
