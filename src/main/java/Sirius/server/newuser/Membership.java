package Sirius.server.newuser;

import java.io.*;
import Sirius.util.*;

/** Eine Klasse, die Informationen enthaelt, welche Benutzergruppen welchem Benutzer zugeordnet werden **/
public class Membership implements Serializable, Mapable
{
    protected String login;
    protected String userDomain;
    protected String ug;
    protected String ugDomain;
    
    
   
    public Membership(String login, String userDomain, String ug, String ugDomain)
    {
        this.login=login.trim();
        this.userDomain=userDomain.trim();
        this.ug=ug.trim();
        this.ugDomain=ugDomain.trim();
        
    }
    
    public String toString()
    {
        return login+"/"+userDomain+"/"+ug+"/"+ugDomain;
    }
    
  
    
    
    public boolean equals(java.lang.Object mem)
    {
        Membership m = (Membership)mem;
        return m.login.equals(this.login)&&m.userDomain.equals(this.userDomain)&&m.ug.equals(this.ug)&&m.ugDomain.equals(this.ugDomain);
    }
    
    /**
     * Getter for property login.
     * @return Value of property login.
     */
    public java.lang.String getLogin()
    {
        return login;
    }
    
    /**
     * Setter for property login.
     * @param login New value of property login.
     */
    public void setLogin(java.lang.String login)
    {
        this.login = login;
    }
    
    /**
     * Getter for property ug.
     * @return Value of property ug.
     */
    public java.lang.String getUg()
    {
        return ug;
    }
    
    /**
     * Setter for property ug.
     * @param ug New value of property ug.
     */
    public void setUg(java.lang.String ug)
    {
        this.ug = ug;
    }
    
    /**
     * Getter for property ugDomain.
     * @return Value of property ugDomain.
     */
    public java.lang.String getUgDomain()
    {
        return ugDomain;
    }
    
    /**
     * Setter for property ugDomain.
     * @param ugDomain New value of property ugDomain.
     */
    public void setUgDomain(java.lang.String ugDomain)
    {
        this.ugDomain = ugDomain;
    }
    
    /**
     * Getter for property userDomain.
     * @return Value of property userDomain.
     */
    public java.lang.String getUserDomain()
    {
        return userDomain;
    }
    
    /**
     * Setter for property userDomain.
     * @param userDomain New value of property userDomain.
     */
    public void setUserDomain(java.lang.String userDomain)
    {
        this.userDomain = userDomain;
    }
    
    public Object constructKey(Mapable m)
    {
        return getKey();
    }
    
    public Object getKey()
    {
        return login+"@"+userDomain+"€"+ug+"@"+ugDomain;
    }
    
    
    public Object getUserKey()
    {
         return login+"@"+userDomain;
    
    }
    
    public Object getUserGroupkey()
    {
    
        return ug+"@"+ugDomain;
    }
    
// end equals
}

