/*
 * UserServiceImpl.java
 *
 * Created on 25. September 2003, 12:53
 */

package Sirius.server.middleware.impls.proxy;

import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import Sirius.server.newuser.*;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.middleware.interfaces.proxy.*;
/**
 *
 * @author  awindholz
 */
public class UserServiceImpl
{
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private UserServer userServer;
    private java.util.Hashtable activeLocalServers;
    
    /** Creates a new instance of UserServiceImpl */
    public UserServiceImpl(java.util.Hashtable activeLocalServers, UserServer userServer)
    throws RemoteException
    {
        this.activeLocalServers = activeLocalServers;
        this.userServer = userServer;
    }
    
    //Wie konnte das jemals gehen 
    //Falsche Reihenfolge in Signatur
//     public User getUser(
//            String userLsName,
//            String userName,
//            String userGroupLsName,
//            String userGroupName,
//            String password) throws RemoteException, UserException
//    {
        
    public User getUser(
            String userGroupLsName,
            String userGroupName,
            String userLsName,
            String userName,
            String password) throws RemoteException, UserException
    {
        
        
        logger.debug("getUser gerufen f\u00FCr user::"+userName);
       
        logger.debug("userLsName:"+userLsName);
        logger.debug("userName:"+userName);
        logger.debug("userGroupLsName:"+userGroupLsName);
        logger.debug("userGroupName:"+userGroupName);
        logger.debug("password:"+password);
        User u = userServer.getUser(userLsName, userName, userGroupLsName, userGroupName, password);
        
        
        boolean validated=false;
        
        if(u!=null)
        {  Sirius.server.middleware.interfaces.domainserver.UserService us =(Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(userLsName);
           
           if(us!=null)
               validated = us.validateUser(u, password);
           else
               throw new UserException("Login fehlgeschlagen, Heimatserver des Users nicht erreichbar :: "+password, false,false,false,true);
        }
        
        if (validated)
            return u;
        
        throw new UserException("Login fehlgeschlagen, Passwort falsch :: "+password, false,true,false,false);
        
    }
    
    /**
     * result contains strings
     */
    public Vector getUserGroupNames()
    throws RemoteException
    {
        logger.debug("getUserGroupName gerufen");
        
        Vector names = new Vector(20,20);
        
        Collection c = userServer.getUserGroups();
        
        Iterator i = c.iterator();
        
        
        
        while(i.hasNext())
        {
            UserGroup tmpUserGroup;
            
            String[] s = new String[2];
            tmpUserGroup = (UserGroup)i.next();
            
            s[0]= tmpUserGroup.getName();
            s[1]= tmpUserGroup.getDomain();
            
            
            names.add(s);
        }
        
        return names;
    }
    
    /**
     * result contains string[2] subset of all ugs
     */
    public Vector getUserGroupNames(String userName,String lsHome)
    throws RemoteException
    {
        logger.debug("getUserGroupNames gerufen for :username:"+userName);
        return userServer.getUserGroupNames(userName.trim(),lsHome.trim());
    }
    
    public boolean changePassword(User user, String oldPassword, String newPassword)
    throws RemoteException, UserException
    {
        logger.debug("changePassword gerufen for :user:"+user);
        return ((Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(user.getDomain())).changePassword(user,oldPassword,newPassword);
    }
}
