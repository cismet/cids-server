package Sirius.server.localserver.method;


import Sirius.server.sql.*;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.PermissionHolder;
import java.sql.*;
import Sirius.server.property.*;






public class MethodCache
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    protected MethodMap methods;
    protected java.util.Vector methodArray;
    protected ServerProperties properties;
    
    
    public MethodCache(DBConnectionPool conPool, ServerProperties properties)
    {
        this.properties = properties;
        
        methodArray = new java.util.Vector(50);
        
        
        methods = new MethodMap(50,0.7f);  // allocation of the hashtable
        
        DBConnection con = conPool.getConnection();
        try
        {
            ResultSet methodTable = con.submitQuery("get_all_methods",new Object[0]);
            
            
            
            while(methodTable.next())//add all objects to the hashtable
            {
                Method tmp = new Method(methodTable.getInt("id"),methodTable.getString("plugin_id").trim(),methodTable.getString("method_id").trim(),methodTable.getBoolean("class_mult"),methodTable.getBoolean("mult"),methodTable.getString("descr"),null);
                methods.add(properties.getServerName(),tmp);
                methodArray.addElement(tmp);
                
                logger.debug("Methode "+tmp+"gecacht");
                
            }// end while
            
            methodTable.close();
            
            // methods.rehash(); MethodMap jetzt hashmap
            
            logger.debug("methodmap :"+methods);
            
            addMethodPermissions(conPool);
            
            addClassKeys(conPool);
            
        }
        
        
        catch (java.lang.Exception e)
        {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: when trying to submit get_all_methods statement",e);
            
        }
        
        
        
        
        
        
    }// end of constructor
    
    
    //----------------------------------------------------------------------------------------
    
    
    final private void addMethodPermissions(DBConnectionPool conPool)
    {
        try
        {	DBConnection con = conPool.getConnection();
                
                ResultSet permTable =  con.submitQuery("get_all_method_permissions",new Object[0]);
                
                String lsName = properties.getServerName();
                
                while(permTable.next())
                {
                    String methodID = permTable.getString("method_id").trim();
                    String pluginID = permTable.getString("plugin_id").trim();
                    String ugLsHome = permTable.getString("ls").trim();
                    int ugID = permTable.getInt("ug_id");
                    
                    String mkey = methodID+"@"+pluginID;
                    
                    if(methods.containsMethod(mkey))
                    {
                        Method tmp = methods.getMethod(mkey);
                        
                        if(ugLsHome == null || ugLsHome.equalsIgnoreCase("local"))
                            ugLsHome = new String(lsName);
                        
                        tmp.addPermission(new UserGroup(ugID,"",ugLsHome));
                    }
                    else
                        logger.error("<LS> ERROR :: theres a method permission without method methodID "+mkey);
                    
                    
                }
                
                permTable.close();
                
                
        }
        
        catch (java.lang.Exception e)
        {       ExceptionHandler.handle(e);
                
                logger.error("<LS> ERROR :: addMethodPermissions",e);
                
        }
        
        
    }
    
    
    //----------------------------------------------------------------------------------------
    
    final public MethodMap getMethods()
    {
        logger.debug("getMethods gerufen"+methods);
        return methods;
    }
    
    //------------------------------------------------------------------------------------------
    
    final public MethodMap getMethods(UserGroup ug) throws Exception
    {
        MethodMap view = new MethodMap(methodArray.size(),0.7f);
        
        for(int i = 0; i < methodArray.size();i++)
        {
            Method m = (Method)methodArray.get(i);
            
            if(m.getPermissions().hasPermission(ug.getKey(),PermissionHolder.READPERMISSION))
                //view.add(properties.getServerName(),m);
                view.add((String)m.getKey(),m);
            
        }
        
        return view;
    }
    
    //------------------------------------------------------------------------------------------
    
    
    public void addClassKeys(DBConnectionPool conPool)
    {
        try
        {	DBConnection con = conPool.getConnection();
                
                String sql = "select c.id as c_id , m.plugin_id as p_id,m.method_id as m_id  from cs_class as c, cs_method as m, cs_method_class_assoc as assoc where c.id=assoc.class_id and m.id = assoc.method_id";
                
                ResultSet table =  con.getConnection().createStatement().executeQuery(sql);
                
                String lsName = properties.getServerName();
                
                while(table.next())
                {
                    String methodID = table.getString("m_id").trim();
                    String pluginID = table.getString("p_id").trim();
                    int classID = table.getInt("c_id");
                    
                    String key =methodID+"@"+pluginID;
                    if( methods.containsMethod(key) )
                    {
                        String cKey = classID+"@"+lsName;
                        methods.getMethod(key).addClassKey(cKey);
                        logger.debug("add class key "+ cKey+ "to mehtod "+ key);
                        
                    }
                    else
                        logger.error("no method key "+key);
                    
                }
                
                table.close();
                
                
        }
        catch (java.lang.Exception e)
        {       ExceptionHandler.handle(e);
                
                logger.error("<LS> ERROR :: addMethodClassKeys",e);
                
        }
        
        
        
    }
    
}




