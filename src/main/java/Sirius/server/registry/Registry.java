package Sirius.server.registry;

import Sirius.server.registry.rmplugin.RMRegistryServerImpl;
import java.security.Permission;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.Vector;
import Sirius.server.naming.*;
import java.io.*;
import Sirius.server.newuser.*;
import Sirius.server.observ.*;
import Sirius.server.property.ServerProperties;
import Sirius.server.*;
import java.util.HashMap;


/**
 * Name- und UserServer des gesammten Siriussystems. Jede Art von Server (LocalServer,CallServer,ProtocolServer)
 * registriert sich hier mit Namen und IPAdresse (Port optional)  um f\u00FCr andere Server erreichbar zu sein.
 * Ausserdem \u00FCbernimmt die Registry eine UserServer-Funktionalit\u00E4t
 * @autor Bernd Kiefer,Sascha Schlobinski
 * @version 1.0
 */

public class Registry extends UnicastRemoteObject implements NameServer,Sirius.server.newuser.UserServer, RemoteObservable
{
    
    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Registry.class);
    
    protected ServerProperties props;
    
    protected Observable obs;
    
    protected ServerManager sm;
    
    protected Sirius.server.newuser.UserManager um;
    
    protected static Registry THIS;
    
    protected int port;
    
    protected  java.rmi.registry.Registry rmiRegistry;
    
    protected ServerStatus status;
    
    protected RMRegistryServerImpl rmRegistryServer;
    
    
    //========================Konstruktoren=============================================================
    
    
    //pd
    public Registry(int port) throws Throwable
    {
        super();
        this.port=port;
        
        
        
        
        obs = new Observable(this);
        sm = new ServerManager();// xxx obs
        um = new Sirius.server.newuser.UserManager();
        
        
        // do the rmi-stuff
        startRMIServer(port);
        rmRegistryServer = new RMRegistryServerImpl();
        rmRegistryServer.startRMRegistryServer(port);
        
        status = new ServerStatus();
        
        THIS=this;
    }
    
    
    
    
    
    //-----------------------------------------------------------------------------------------------------
    
    private void startRMIServer(int port) throws Throwable
    {
        
        
        try
        {
            
            
           // if(System.getSecurityManager()==null)
                System.setSecurityManager(new RMISecurityManager()
                {
                    public void checkPermission(Permission perm,Object context){}
                }
                );
            
            
            
            try
            {rmiRegistry=LocateRegistry.createRegistry(port);}
            
            catch(Exception e)
            {rmiRegistry=LocateRegistry.getRegistry(port); }
            
            
            
            // wenn keine Registry vorhanden, wird an dieser Stelle Exception ausgeloest
            String[] list = rmiRegistry.list();
            
            
            if(list.length>0)
                System.out.println("<Reg> STATUS registerd with RMIRegistry:");
            
            String l ="";
            for (int i=0; i< list.length; i++)
                l+=("\t"+ list[i]);
            
            logger.info(l);
            
            System.out.println("<REG> Bind SiriusRegistry on RMIRegistry as nameServer and userServer");
            logger.info("<REG> Bind SiriusRegistry on RMIRegistry as nameServer and userServer");
            
            
            //pd:
            Naming.bind("rmi://localhost:" + port + "/userServer", this);
            Naming.bind("rmi://localhost:" + port + "/nameServer", this);
            
            
            
            System.out.println("<REG> ----------Sirius.Registry.Registry STARTED!!!----------\n");
            
            
            
        }
        
        catch(Exception e)
        {logger.error(e.getMessage(),e);throw new ServerExitError(e); }
        
        
    }
    
    public static Registry getServerInstance()
    {return THIS;}
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // --------------------------Methods of the Interface Nameserver-----------------------------------------------
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public boolean registerServer(int typ, String name, String ip) throws RemoteException
    {
        logger.debug("registerServer gerufen f\u00FCr ::"+name +" ip:"+ip);
        try
        {
            if( sm.registerServer(typ,name,ip))
            {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Neuer Server",name + " insgesamt ::" +sm.getServerCount(typ)+ " Typ "+typ);
                return true;
            }
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public boolean registerServer(int typ, String name, String ip, String port) throws RemoteException
    {
        logger.debug("registerServer gerufen f\u00FCr ::"+name +" ip:"+ip);
        try
        {
            if( sm.registerServer(typ,name,ip,port))
            {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Neuer Server",name + " insgesamt ::" +sm.getServerCount(typ)+ " Typ "+typ);
                return true;
            }
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        return false;
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public boolean unregisterServer( int typ, String name, String ip) throws RemoteException
    {
        logger.debug("unregisterServer gerufen f\u00FCr ::"+name +" ip:"+ip);
        try
        {
            if(sm.unregisterServer(typ,name,ip))
            {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Server abgemeldet",name + " insgesamt ::" +sm.getServerCount(typ)+ " Typ "+typ);
                return true;
            }
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        return false;
        
    }
    
    //---------------------------------------------------------------------------------------
    
    public boolean unregisterServer( int typ, String name, String ip, String port) throws RemoteException
    {
        logger.debug("unregisterServer gerufen f\u00FCr ::"+name +" ip:"+ip);
        try
        {
            if(sm.unregisterServer(typ,name,ip,port))
            {
                obs.setChanged();
                obs.notifyObservers();
                status.addMessage("Server abgemeldet",name + " insgesamt ::" +sm.getServerCount(typ)+ " Typ "+typ);
                return true;
            }
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        return false;
        
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    public HashMap<String,String>  getServerIPs(int typ) throws RemoteException
    {
        logger.debug("getServerIps gerufen f\u00FCr servertyp::"+typ);
        try
        {
            return sm.getServerIPs(typ);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public String getServerIP(int typ, String name) throws RemoteException
    {
        logger.debug("getServerIp gerufen f\u00FCr servertyp::"+typ+ " servername:"+name);
        try
        {
            return sm.getServerIP(typ,name);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public Server[] getServers(int typ)throws RemoteException
    {
        logger.debug("getServers gerufen f\u00FCr servertyp::"+typ);
        try
        {
            return sm.getServers(typ);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
    }
    
    //---------------------------------------------------------------------------------------
    
    public Server getServer(int typ, String name)throws RemoteException
    {
        logger.debug("getServer gerufen f\u00FCr servertyp::"+typ+ " servername:"+name);
        try
        {
            return sm.getServer(typ,name);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // --------------------------remote Methods of the Interface Userserver-----------------------------------------------
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    public Vector getUsers() throws RemoteException
    {
        logger.debug("getUsers gerufen");
        try
        {
            return um.getUsers();
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        
        
    }
    
    //---------------------------------------------------------------------------------------
    
    public User getUser(String userGroupLocalServerName,String userGroupName,String userLocalServerName, String userName,String password)
    throws RemoteException, UserException
    {
        logger.debug("getUser gerufen f\u00FCr user" + userName );
        User user = null;
        Remote remoteServer =null;
        try
        {
            
            user= um.getUser(userGroupLocalServerName,userGroupName,userLocalServerName,userName,password);
            
            // ServerObjekt des LocalServer
            Server localServer = getServer(ServerType.LOCALSERVER,userLocalServerName);
            
            String lookupString = localServer.getRMIAddress();
            
            // Referenz auf LocalServer
            remoteServer = (Remote) Naming.lookup(lookupString);
            
            
            
            
        }
        catch(UserException e)
        {logger.error(e);throw e;}
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
        
        //        xxx
        //        if (!((UserService)remoteServer).validateUser(user,password))
        //        {
        //            logger.error("<REG> result von remoteServer.validateUser(user,password) :: false");
        //            throw new UserException("<REG> wrong password for "+user,false,true,false,false);
        //        }
        //
        
        return user;
        
    }
    
    //---------------------------------------------------------------------------------------
    
    public void registerUser(User user)throws RemoteException
    {
        logger.debug("registerUser gerufen f\u00FCr user::"+user);
        try
        {
            um.registerUser(user);
            // status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ user.toString() +"\nBenutzer  System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ user.toString() +"\nBenutzer ");
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void registerUsers(Vector users)throws RemoteException
    {
        logger.debug("registerUsers gerufen");
        try
        {
            um.registerUsers(users);
            // status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ users.toString() +"\nBenutzer  System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ users.toString() +"\nBenutzer  System insgesamt ::");
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void unregisterUsers(Vector users)throws RemoteException
    {
        logger.debug("unregisterUsers gerufen");
        try
        {
            um.unregisterUsers(users);
            //status.addMessage("Benutzer entfernt","Benutzer :: "+ users.toString() +"\nBenutzer  System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzer hinzugef\u00FCgt","Gruppen :: "+ users.toString() +"\nBenutzer");
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    
    public void unregisterUser(User user) throws RemoteException
    {
        logger.debug("unregisterUser gerufen f\u00FCr user::"+user);
        try
        {
            um.unregisterUser(user);
            //  status.addMessage("Benutzer entfernt","Benutzer :: "+ user.toString() +"\nBenutzer implements System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzer entfernt","Benutzer :: "+ user.toString() +"\nBenutzer ");
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    public void registerUserGroup(UserGroup userGroup) throws RemoteException
    {
        logger.debug("registerUserGroup gerufen f\u00FCr userGroup::"+userGroup);
        try
        {
            um.registerUserGroup(userGroup);
            // status.addMessage("Benutzergruppe hinzugef\u00FCgt","Gruppen :: "+ userGroup.toString() +"\nBenutzer implements System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzer entfernt","Benutzer :: "+ userGroup.toString() +"\nBenutzer");
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void registerUserGroups(Vector userGroups) throws RemoteException
    {
        logger.debug("registerUserGroups gerufen");
        try
        {
            um.registerUserGroups(userGroups);
            
            status.addMessage("Benutzergruppe hinzugef\u00FCgt","Gruppen :: "+ userGroups.toString() );
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void unregisterUserGroups(Vector userGroups) throws RemoteException
    {
        logger.debug("unregisterUserGroups gerufen");
        try
        {
            um.unregisterUserGroups(userGroups);
            // status.addMessage("Benutzergruppen entfernt","Benutzergruppen :: "+ userGroups.toString() +"\nBenutzer System insgesamt ::"+um.getUserCount()+ " in "+ um.getUserGroupCount()+ " Benutzergruppen");
            status.addMessage("Benutzergruppen entfernt","Benutzergruppen :: "+ userGroups.toString() );
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public void unregisterUserGroup(UserGroup userGroup)throws RemoteException
    {
        logger.debug("unregisterUserGroup gerufen f\u00FCr userGroup"+userGroup);
        try
        {
            um.unregisterUserGroup(userGroup);
            status.addMessage("Benutzergruppe entfernt","Benutzergruppen :: "+ userGroup.toString() );
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public boolean registerUserMembership(Membership membership) throws RemoteException
    {
        logger.debug("registerUserMembership gerufen f\u00FCr membership"+membership);
        try
        {
            return um.registerUserMembership(membership);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void registerUserMemberships(Vector memberships)throws RemoteException
    {
        logger.debug("registerUserMemberships gerufen");
        try
        {
            um.registerUserMemberships(memberships);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    /** liefert alle Benutergruppen */
    public Vector getUserGroups() throws RemoteException
    {
        logger.debug("getUserGroups gerufen");
        try
        {
            return um.getUserGroups();
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    //---------------------------------------------------------------------------------------
    
    
    /** liefert einen Vector mit String-Arrays[2] String[0] - userName String[1] - userLocalServerName */
    public Vector getUserGroupNames(User user) throws RemoteException
    {
          logger.debug("getUserGroupNames gerufen f\u00FCr user::"+user);
        try
        {
            return um.getUserGroupNames(user);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public Vector getUserGroupNames(String userName,String lsName) throws RemoteException
    {
         logger.debug("getUserGroupNames gerufen f\u00FCr userName::"+userName);
        try
        {
            return um.getUserGroupNames(userName,lsName);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // --------------------------remote Methods of the Interface RemoteObservable-----------------------------------------------
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    public void addObserver(RemoteObserver ob) throws RemoteException
    {
        try
        {
            obs.addObserver(ob);
            
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    
    
    public void deleteObserver(RemoteObserver ob) throws RemoteException
    {
        try
        {
            obs.deleteObserver(ob);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    
    public int countObservers() throws RemoteException
    {
        try
        {
            return obs.countObservers();
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    public void notifyObservers() throws RemoteException
    {
        try
        {
            obs.notifyObservers();
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    //---------------------------------------------------------------------------------------
    
    
    public void notifyObservers(Remote arg) throws RemoteException
    {
        try
        {
            obs.notifyObservers(arg);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    
    public void notifyObservers(Serializable arg) throws RemoteException
    {
        try
        {
            obs.notifyObservers(arg);
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //---------------------------------------------------------------------------------------
    
    
    
    public boolean hasChanged() throws RemoteException
    {
        try
        {
            return obs.hasChanged();
        }
        catch (Exception e)
        {logger.error(e);throw new RemoteException(e.getMessage(),e);}
        
    }
    
    
    //-----------------------------------------------------------------------------------------------------
    public void shutdown() throws Throwable
    {
        try
        {
            
            Naming.unbind("rmi://localhost:" + port + "/userServer");
            Naming.unbind("rmi://localhost:" + port + "/nameServer");
            rmRegistryServer.stopRMRegistryServer();
            rmiRegistry=null;
            System.gc();
            
            throw new ServerExit("Server ist regul\u00E4r beendet worden");
        }
        catch(Exception e)
        {throw new ServerExitError(e);}
    }
    
    
    
    public ServerStatus getStatus()
    {return status;}
    
    
    public static void main(String[] args) throws Throwable
    {
        int port = 1099;
        
        try
        {
            if(args==null)
                throw new ServerExitError("args == null keine Kommandozeilenparameter \u00FCbergeben (Configfile / port)" );
            
            else if(args.length > 0)
            {
                port = Integer.valueOf(args[0]).intValue();
                
                
            }
        }
        catch(NumberFormatException nfexp)
        {
            System.err.println(nfexp.getMessage());
        }
        
        
        
        try
        {
            //Sirius.Registry.Registry siriusRegistry = new Registry();
            new Registry(port);
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new ServerExitError(e);
        }
    }
    
    
    
    
    
}// end class registry


