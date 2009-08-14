package Sirius.server.naming;

import java.rmi.*;
import Sirius.server.*;
import java.util.HashMap;

public interface NameServer extends Remote
{
    public boolean registerServer(int serverTyp, String name, String ip)
    throws RemoteException;
    
    public boolean registerServer(int serverTyp, String name, String ip, String port)
    throws RemoteException;
    
    public boolean unregisterServer( int serverTyp, String name, String ip)
    throws RemoteException;
    
    public boolean unregisterServer( int serverTyp, String name, String ip, String port)
    throws RemoteException;
    
    public HashMap<String,String> getServerIPs(int serverTyp)
    throws RemoteException;
    
    public String getServerIP(int serverTyp, String name)
    throws RemoteException;
    
    public Server[] getServers(int serverTyp)
    throws RemoteException;
    
    public Server getServer(int serverTyp, String serverName)
    throws RemoteException;
}
