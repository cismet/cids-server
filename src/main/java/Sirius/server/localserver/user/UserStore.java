package Sirius.server.localserver.user;

import Sirius.server.newuser.*;
import Sirius.server.sql.*;
import Sirius.server.property.*;
import java.sql.*;
import java.util.*;

public class UserStore
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    protected DBConnectionPool conPool;
    
    protected Vector users;
    protected Vector userGroups;
    //protected Hashtable userGroupHash;
    protected Vector memberships;
   // protected Hashtable membershipHash;// by userIDplusLsName
    protected ServerProperties properties;
    protected PreparedStatement validateUser;
    
    
    public UserStore(DBConnectionPool conPool,ServerProperties properties)
    {
        this.conPool = conPool;
        this.properties = properties;
        users = new Vector(100,100);
        userGroups = new Vector(10,10);
        // userGroupHash = new Hashtable(25);
        memberships = new Vector(100,100);
       // membershipHash = new Hashtable(101);
        
        DBConnection con = conPool.getConnection();
        
        try
        {
            ResultSet userTable = con.submitQuery("get_all_users",new Object[0]);
            
            //--------------------load users--------------------------------------------------
            
            
            while(userTable.next())
            {
                try
                {
                    //User tmp = new User(userTable.getString("login_name").trim(),properties.getLocalServerName(),userTable.getInt("id"),userTable.getBoolean("administrator") );
                    //User tmp = new User(userTable.getString("login_name").trim(),properties.getServerName(),userTable.getInt("id"),DBConnection.stringToBool(userTable.getString("administrator")) );
                    User tmp = new User(userTable.getInt("id"),userTable.getString("login_name").trim(),properties.getServerName(),userTable.getBoolean("administrator") );
                    
                    users.addElement(tmp);
                    
                }
                catch(Exception e)
                {
                    logger.error(e);
                    
                    if(e instanceof java.sql.SQLException)
                        throw e;
                }
                
                
            }// end while
            
            userTable.close();
            
            //--------------------load userGroups--------------------------------------------------
            
            ResultSet userGroupTable = con.submitQuery("get_all_usergroups",new Object[0]);
            
            
            
            while(userGroupTable.next())
            {
                try
                {
                    // UserGroup tmp = new UserGroup(userGroupTable.getString("name").trim(),properties.getServerName(),userGroupTable.getInt("id") );
                    
                    UserGroup tmp = new UserGroup(userGroupTable.getInt("id"),userGroupTable.getString("name").trim(),properties.getServerName(),userGroupTable.getString("descr"));
                    userGroups.addElement(tmp);
                    //userGroupHash.put(new Integer(tmp.getID()),tmp);
                    
                }
                catch(Exception e)
                {
                    logger.error(e);
                    
                    if(e instanceof java.sql.SQLException)
                        throw e;
                }
                
                
            }// end while
            
            userGroupTable.close();
            
            //--------------------load memberships--------------------------------------------------
            
            ResultSet memberTable = con.submitQuery("get_all_memberships",new Object[0]);
            
            
            
            while(memberTable.next())
            {
                try
                {
                   
                    String  lsName=properties.getServerName();
                    
                    String login = memberTable.getString("login_name");
                    String ug = memberTable.getString("ug");
                    
                    String ugDomain=memberTable.getString("ugDomain");
                    
                    if( ugDomain == null || ugDomain.equalsIgnoreCase("local"))
                        ugDomain=lsName;
                    
                    String usrDomain=lsName;
                    
                    Membership tmp = new Membership(login,usrDomain,ug,ugDomain);
                    memberships.addElement(tmp);
                    // durch getkey ersetzen  xxxx
                   // membershipHash.put(login+usrDomain,tmp);
                    
                }
                catch(Exception e)
                {
                    logger.error(e);
                    
                    if(e instanceof java.sql.SQLException)
                        throw e;
                }
                
                
            }// end while
            
            memberTable.close();
            
            
            //			addSearchMasks(con);
            
            
            // prepare statement for validate user (called very often) :-)
            String valUser = "select count(*) from cs_usr as u ,cs_ug as ug ,cs_ug_membership as m where u.id=m.usr_id and  ug.id = m.ug_id and trim(login_name) = ? and trim(ug.name) = ?";
            validateUser = con.getConnection().prepareStatement(valUser);
            
        }
        catch (java.lang.Exception e)
        {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR ::  in membership statement"+ e.getMessage(),e);
            
        }
        
        
        
        
    }// end Konstruktor
    
    
    
    public Vector getUsers()
    {return users;}
    
    public Vector getUserGroups()
    {return userGroups;}
    
    public Vector getMemberships()
    {return memberships;}
    
    public boolean changePassword(User user, String oldPassword, String newPassword) throws Exception
    {
        DBConnection con = conPool.getConnection();
        
        java.lang.Object[] params = new java.lang.Object[3];
        
        params[0] = newPassword;
        params[1] = user.getName().toLowerCase();
        params[2] = oldPassword;
        
        if(con.submitUpdate("change_user_password",params)>0)
            return true;
        else
            return false;
        
    }
    

    
    public boolean validateUser(User user)
    {
        
//        if(user == null)
//        {   logger.error("user for validation was null");
//            return false;
//        }
//        String name = user.getName().trim();
//        String ug_name = user.getUserGroup().getName().trim();
//        
//        
//          
//        if(name == null || ug_name==null)
//        {   logger.error("user name for validation was null");
//            return false;
//        }
//        
//        logger.debug("stmnt at validate user "+ name + " user group " +ug_name);
//        
//        try
//        {
//            validateUser.setString(1, name);
//            validateUser.setString(2, ug_name);
//            
//            ResultSet result = validateUser.executeQuery();
//            
//            if( result.next())
//                if(result.getInt(1)>0)
//                    return true;
//            
//            
//        }
//        catch(Exception e)
//        {logger.error("wahrscheinlich user nicht gefunden",e);}
//        
//        return false;
        
        return true;
        
    }
    
    //--------------------------------------------------------------------------
    
    public boolean validateUserPassword(User user,String password) throws Exception
    {
        
        DBConnection con = conPool.getConnection();
        
        java.lang.Object[] params = new java.lang.Object[2];
        
        params[0] = user.getName().trim().toLowerCase();
        params[1] = password.trim().toLowerCase();
        
        logger.debug("Name :" + params[0]+"Passwort :"+ params[1]);
        
        ResultSet result = con.submitQuery("verify_user_password",params);
        
        logger.debug("Result there ?"+result);
        
        if( result.next())
            if(result.getInt(1)>0)
                return true;
        
        return false;
        
    }
    
    
    
    
    
}
