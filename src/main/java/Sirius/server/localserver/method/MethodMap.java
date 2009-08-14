package Sirius.server.localserver.method;


import java.util.*;

public class MethodMap extends java.util.HashMap implements java.io.Serializable
{
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodMap.class);
//constructor
    public MethodMap()
    {super();}
    
//constructor
    public MethodMap(int capacity, float factor)
    {super(capacity,factor);}
    
    
    
////////////////////////////////////////////////////
    
    public void add(String key,Method method) throws Exception
    {
        
        if( !this.containsMethod(key) )
            super.put(method.getKey(),method);
        else
        { 
          Method m = this.getMethod(key);
          m.getPermissions().addPermissions(method.getPermissions()); // add only permissions
          m.getClassKeys().addAll(method.getClassKeys());
          
          if(m.isClassMultiple()!=method.isClassMultiple()) // wenn unterschiedliche Angaben wird alles auf false gesetzt
              m.c_multiple=false;
          if(m.isMultiple()!=method.isMultiple())
              m.o_multiple=false;
          
        }
        
        if( !containsMethod(method.getKey() ) )
            throw new java.lang.Exception("Couldn't add Method:"+method.getKey());
    }// end add
    
    
///////////////////////////////////////////////////////////////
    
    public Method getMethod(String key) throws Exception
    {
        
        if(containsMethod(key))
        {
            java.lang.Object candidate = super.get(key);
            if (candidate instanceof Method)
                return (Method) candidate;
            throw new java.lang.NullPointerException("Entry is not a Method :" +key);
        }// endif
        
        throw new java.lang.NullPointerException("No entry Method :"+key); // to be changed in further versions when exception concept is accomplished
    }// end getMethod
    
    
    
/////// containsIKey/////////////////////////////////
    
    public boolean containsMethod(java.lang.Object key)
    {return super.containsKey(key);}
    
// \u00FCberschreiben da berechtigungen f\u00FCr gleiche methoden auf verschiedenen ls vergeben werden
    
    public void putAll(Map t)
    {
        if(t==null||t.isEmpty())
            return;
        
        Iterator i = t.values().iterator();
        while (i.hasNext())
        {
            Method e = (Method) i.next();
            try
            {
                this.add((String)e.getKey(), e);
            }
            catch(Exception ex)
            {if(logger!=null)logger.error("Fehler beim Methode hinzuf\u00FCgen (putAll)",ex);
             else System.err.println("Fehler beim Methode hinzuf\u00FCgen (putAll)/n"+ex.getMessage());}
        }
    }
    
//    public void rehash()
//    {super.rehash();}
    
}// end of class MethodMap
