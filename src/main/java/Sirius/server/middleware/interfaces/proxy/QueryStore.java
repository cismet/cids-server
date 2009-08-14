

package Sirius.server.middleware.interfaces.proxy;

import java.rmi.*;
import java.rmi.server.*;


import Sirius.server.newuser.*;
import Sirius.server.search.store.*;



/**
 * in the MIS the user is cappable of storing predefind queries on the domain server
 * this interface profides methods to manage everything in this context
 */
public interface QueryStore extends java.rmi.Remote
{
	public boolean storeQuery (User user, QueryData data)
	throws RemoteException;

      
	public Info[] getQueryInfos(User user)
	throws RemoteException;

	public Info[] getQueryInfos(UserGroup userGroup)
	throws RemoteException;

   
	public QueryData getQuery (int id,String domain)
	throws RemoteException;

	public boolean delete(int id,String domain)
	throws RemoteException;

}

