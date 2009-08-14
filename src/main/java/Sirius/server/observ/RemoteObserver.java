package Sirius.server.observ;

import java.rmi.*;


public interface RemoteObserver extends Remote
{ 
  public void update(RemoteObservable obs, java.lang.Object arg)
  throws RemoteException; 
}  
                            
