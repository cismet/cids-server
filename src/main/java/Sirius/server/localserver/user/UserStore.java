/*
 * UserStore.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * sascha.schlobinski@cismet.de
 * thorsten.hell@cismet.de
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 18.02.2010, 14:46:33
 *
 */

package Sirius.server.localserver.user;

import Sirius.server.newuser.*;
import Sirius.server.sql.*;
import Sirius.server.property.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author sascha.schlobinski@cismet.de
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class UserStore
{
    private static final transient Logger LOG = Logger.getLogger(
            UserStore.class);
    
    
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
                    LOG.error(e);
                    
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
                    LOG.error(e);
                    
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
                    LOG.error(e);
                    
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
            LOG.error("<LS> ERROR ::  in membership statement"+ e.getMessage(),e);
            
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
    
    public boolean validateUserPassword(final User user, final String password)
            throws 
            SQLException
    {
        final DBConnection con = conPool.getConnection();
        ResultSet result = null;
        try
        {
            // TODO: should username and password be trimmed?
            result = con.submitInternalQuery("verify_user_password",
                    user.getName().trim().toLowerCase(),
                    password.trim().toLowerCase());
            return result.next() && result.getInt(1) == 1;
        }finally
        {
            DBConnection.closeResultSets(result);
        }
    }
}