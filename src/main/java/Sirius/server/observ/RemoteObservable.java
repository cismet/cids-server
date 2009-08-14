package Sirius.server.observ;

import java.rmi.*;
import java.io.*;

public interface RemoteObservable extends Remote
{
	void addObserver(RemoteObserver ob) throws RemoteException;

 	void deleteObserver(RemoteObserver ob) throws RemoteException;

	int countObservers() throws RemoteException;

	void notifyObservers() throws RemoteException;

	void notifyObservers(Remote arg) throws RemoteException;

 	void notifyObservers(Serializable arg) throws RemoteException;

	boolean hasChanged() throws RemoteException;

}
