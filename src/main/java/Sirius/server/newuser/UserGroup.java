package Sirius.server.newuser;
import Sirius.util.*;
public class UserGroup implements java.io.Serializable,Mapable
{
    
    
    protected int id;
    protected String domain;
    protected String name;
    protected String description;
    protected boolean isAdmin;
    
    
    
    public UserGroup(int id,String name,String domain  )
    {
        
        this.id=id;
        this.domain =domain.trim();
        this.name = name.trim();
        this.description="";
        this.isAdmin = false;
        
    }
    
    public UserGroup(int id,String name,String domain,String description  )
    {
        this(id,name,domain);
        this.description=description;
        
    }
    
    public String toString()
    {
        return getKey().toString();
    }
    
    
    
    
    public final String getName()
    {return name;}
    
    
    
    public boolean equals(java.lang.Object ug)
    {
        UserGroup userGroup = (UserGroup)ug;
        
        return getKey().equals(userGroup.getKey());
        
    }
    
    
    
    
    //Mapable
    public Object getKey()
    {return name+"@"+domain;}
    
    
    public final boolean isAdmin()
    {return isAdmin;}
    
    public String getDomain()
    {return domain;}
    
    
    
    /** Setter for property isAdmin.
     * @param isAdmin New value of property isAdmin.
     *
     */
    public void setIsAdmin(boolean isAdmin)
    {
        this.isAdmin = isAdmin;
    }
    
    public Object constructKey(Mapable m)
    {
        if(m instanceof UserGroup)
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
    
    public static Object[] parseKey(String classKey) throws Exception
    {
        
        Object[] result = new Object[2];
        
        if(classKey.contains("@"))
        {
            String[]  split = classKey.split("@");
            result[0]= split[0];
            result[1]=split[1];
        }
        else // nehme ich an dass die domain fehlt
        {
            result[0] =  classKey;
            result[1] = "LOCAL";
        }
        return result;
    }
    
}
