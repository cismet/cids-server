package Sirius.server.newuser;
import Sirius.util.*;

public class User implements java.io.Serializable,Mapable
{
    
    //    /** die BenutzerID **/
    protected int id;// identifier im ls
    
    /** der LoginName des Benutzers **/
    protected String name;// loginname
    
    /** der Heimat-LocalServer des Benutzers **/
    protected String domain; // heimatserver
    
    /** Die Benutzergruppe, der der Benutzer zugeordnet ist. Sie wird explizit gesetzt **/
    protected UserGroup userGroup;
    
    /** Variable, die anzeigt, ob eine Benutzergruppe gesetzt wurde **/
    protected boolean valid = false;
    
    protected boolean isAdmin = false;
    
    //    /**  Konstruktor fuer Testzwecke, initialisert id = -1, name = default, localServerName = default **/
    //    public User() {
    //        id = -1;
    //        name = "default";
    //        domain = "default";
    //    }
    
    /**
     * @param name Benutzername
     * @param Heimat-LocalServer des Benutzers
     * @param BenutzerID **/
    public User(int id,String name,String domain )
    {
        this.domain = domain;
        this.id = id;
        this.name = name;
    }
    
    
    
    public User(int id,String name,String domain ,boolean isAdmin )
    {
        this(id,name,domain);
        this.isAdmin = isAdmin;
        
    }
    
    
    
    /** legt Benutzer an und weist ihm direkt eine Benutzergruppe zu.
     * @param name der Benutzername
     * @param localServerName Heimat-LocalServer des Benutzers
     * @param id BenutzerID
     * @param userGroup, die Benutzergruppe, der der Benutzer zugeordnet werden soll **/
    public User(int id,String name,String domain , UserGroup userGroup )
    {
        this(id,name,domain);
        this.userGroup = userGroup;
    }
    
    
    public String toString()
    {
        return getKey().toString();
    }
    
    
    
    public String getDomain()
    {return domain;}
    
    /** @return Login-Name des Benutzers **/
    public String getName()
    {return name;}
    
    //public void changePassword(/*UserServer userServer,*/String oldPassword,String newPassword) throws Exception {}
    
    /** weist dem User eine UserGroup zu und setzt valid = true. Mit isValid() kann abgefragt werden,
     * ob schon eine UserGroup gesetzt wurde **/
    public void setUserGroup(UserGroup userGroup)
    {
        this.userGroup = userGroup;
        valid = true;
    }
    
    /** liefert UserGroup **/
    public UserGroup getUserGroup()
    {return userGroup;}
    
    
    
    public void setValid()
    { valid = true; }
    
    /** hiermit kann abgefragt werden, ob dem User schon eine Usergroup zugewiesen wurde **/
    public boolean isValid()
    { return valid; }
    
    //** hiermit kann abgefragt werden, ob es sich um einen Admin handelt
    public boolean isAdmin()
    { return  isAdmin; }
    
    
    public boolean equals(java.lang.Object obj)
    {
        User user = (User) obj;
        return (this.name.equals(user.name) && this.domain.equals(user.domain) );
        
    }
    
    public Object getKey()
    {
        if(userGroup!=null)
            return name+"@"+userGroup.getKey();
        else return name+"";
    }
    
    
      public Object getRegistryKey()
    {
       
            return name+"@"+domain;
       
    }
    
    public Object constructKey(Mapable m)
    {
        if(m instanceof User)
            return m.getKey();
        else return null;
    }
    
    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId()
    {
        return id;
    }
    
    
    
}


