

package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;
import java.rmi.server.*;


import Sirius.server.newuser.*;
import Sirius.server.search.store.*;



public interface QueryStore extends java.rmi.Remote
{
	public boolean storeQuery (User user, QueryData data)
	throws RemoteException;

      
	public Info[] getQueryInfos(User user)
	throws RemoteException;

	public Info[] getQueryInfos(UserGroup userGroup)
	throws RemoteException;

   
	public QueryData getQuery (int id)
	throws RemoteException;

	public boolean delete(int id)
	throws RemoteException;

}

