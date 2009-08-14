package Sirius.server.newuser;

import java.rmi.*;
import java.util.*;

public interface UserServer extends Remote
{
    //public void login(User u) throws RemoteException;
    
    public Vector getUsers() throws RemoteException;
    
    public User getUser(String userGroupLocalServerName,String userGroupName,String userLocalServerName, String userName,String password)
    throws RemoteException, UserException;
    
    public void registerUser(User user)
    throws RemoteException;
    
    public void registerUsers(Vector users)
    throws RemoteException;
    
    public void unregisterUsers(Vector users)
    throws RemoteException;
    
    public void unregisterUser(User user)
    throws RemoteException;
    
    public void registerUserGroup(UserGroup userGroup)
    throws RemoteException;
    
    public void registerUserGroups(Vector userGroups)
    throws RemoteException;
    
    public void unregisterUserGroups(Vector userGroups)
    throws RemoteException;
    
    public void unregisterUserGroup(UserGroup userGroups)
    throws RemoteException;
    
    public boolean registerUserMembership(Membership membership)
    throws RemoteException;
    
    public void registerUserMemberships(Vector memberships)
    throws RemoteException;
    
    /** liefert alle Benutergruppen **/
    public Vector getUserGroups()
    throws RemoteException;
    
    /** liefert einen Vector mit String-Arrays[2]
     * String[0] - userName
     * String[1] - userLocalServerName **/
    public Vector getUserGroupNames(User user)
    throws RemoteException;
    
    public Vector getUserGroupNames(String userName,String lsName)
    throws RemoteException;
    
    //public Vector getUserGroups(String userName,String lsName)
    //throws RemoteException;
    
    /** liefert alle Benutzergruppen eines Benutzers **/
    //public Vector getUserGroups(User user)
    //throws RemoteException;
    
}
