package Sirius.server.localserver.method;


import Sirius.util.*;
import Sirius.server.newuser.permission.*;
import Sirius.util.*;
import java.util.*;


public class Method implements java.io.Serializable,Cloneable,Mapable
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    protected int id;
    
    protected PermissionHolder permissions;
    
    protected ArrayList classKeys = new ArrayList();
    
    //mapable by name
    protected String plugin_id;
    
    protected String method_id;
    
    protected String description;
    
    boolean o_multiple;
    
    boolean c_multiple;
    
    
    
    
    
    
    
//-----------------------------------------------------------------------------------
    
    //public Method(int id){this.id = id;} //dummy
    
    
    public Method(int id,String plugin_id,String method_id,boolean c_multiple,boolean o_multiple,Policy policy)
    {
        this.id = id;
        this.plugin_id = plugin_id;
        this.method_id = method_id;
        if (policy==null) {
            policy=Policy.createParanoidPolicy();
        }
        permissions = new PermissionHolder(policy);
        description = "";
        this.c_multiple = c_multiple;// beliebig viele klassen beliebig viele Objekte
        this.o_multiple = o_multiple;// 1 klasse mehrer Objekte
        
    }
    
    
    public Method(int id,String plugin_id,String method_id,boolean c_multiple,boolean o_multiple,String description,Policy policy)
    {
        
        this(id,plugin_id,method_id,c_multiple,o_multiple,policy);
        this.description = description;
        
        
    }
    
    
//-----------------------------------------------------------------------------------
    final public String getDescription()
    {return description;}
    
    //------------------------------------------------------------------------------
    
    public void setDescription(String description)
    { this.description= description;}
    
//-----------------------------------------------------------------------------------
    final public PermissionHolder getPermissions()
    {return permissions;}
    
//-----------------------------------------------------------------------------------
    
    
    final public int getID()
    {return id;}
    
    public Object getKey()
    {return method_id+"@"+plugin_id;}
    
    final public boolean isMultiple()
    {return o_multiple;}
    
    final public boolean isClassMultiple()
    {return c_multiple;}
    
    
    
    
    public String toString()
    {return "Name ::"+getKey()+" id::"+id;}
    
    final public void addPermission(Mapable m)
    {
        permissions.addPermission(m);
    }
    
//	final public void removePermission(String localServerName, int userGroupID)
//	{
//		permissions.addPermission(localServerName,userGroupID,false);
//	}
    
    public Object constructKey(Mapable m)
    {
        if(m instanceof Method)
            return m.getKey();
        else
            return null;
    }
    
    public void addClassKey(String key)
    { if (logger!=null)logger.debug("add class key"+ key + " to method " +this);classKeys.add(key);}
    
    public   Collection getClassKeys()
    { return classKeys;}
    
    
}
