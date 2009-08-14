package Sirius.server.middleware.interfaces.proxy;

import java.rmi.*;
import Sirius.util.image.*;

/**
 * gives access to system parameters, resources and configurations of the MIS and its domain servers
 */

public interface SystemService extends Remote
{

	//public Server getTranslationServer() throws RemoteException;

	/**
	 * delivers default icons of a certain domain (resources of this domain)
	 * used to initialize a client (navigator)
	 * @param lsName domain
	 * @throws java.rmi.RemoteException server error
	 * @return default icons as images of a certain domain
	 */
	public Image[] getDefaultIcons(String lsName) throws RemoteException;

	/**
	 * delivers default icons of a random domain 
	 * used to initialize a client (navigator)
	 * @throws java.rmi.RemoteException server error (eg no domain server online)
	 * @return list of default icons (images)
	 */
	public Image[] getDefaultIcons() throws RemoteException;
        
        
        //public Sirius.Server.Server getTranslationServer() throws RemoteException; 
       

}
