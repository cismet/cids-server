package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;

import Sirius.util.image.*;

/**



*/

public interface SystemService extends Remote
{

	
	public Image[] getDefaultIcons() throws RemoteException;


}
