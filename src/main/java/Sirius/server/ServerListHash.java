package Sirius.server;

import java.util.*;


public class ServerListHash extends Hashtable
{
    
    // 'Elemente sind Vectoren von servern
    
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    public ServerListHash()
    {super();init();}
    
    public ServerListHash(int capacity,float loadfactor)
    {super(capacity,loadfactor);init();}
    
    public ServerListHash(int capacity)
    {super(capacity);init();}
    
    //-------------------------------------------------------------------------
    
    private void init()
    {
        //Debug xxx not necessary if Xoption for Garbagecollector ist set
        new ServerType("",1);
        
        int[] types = ServerType.getAllServerTypes();
        
        for(int i=0;i<types.length;i++)
            put( new Integer(types[i]),new Vector(5,5) );
        
        
    }
    
    
    
    
    //-------------------------------------------------------------------------------------------------
    
    
    public boolean addServer(int serverTyp, String name ,String ip, String port)
    {
        Server s = findServer(serverTyp,name/*,ip,port*/);
        
        logger.debug("server there? "+name+ " not null?"+s);
      
        if(s==null)//not found
        {
            s=new Server(serverTyp,name,ip,port);
            
            if(containsKey(new Integer(serverTyp)))
            {
                getServerList(serverTyp).add(s);
                return true;
            }
            
        }
        
        
        
        logger.error("tried to add server " +name +" "+ip +" "+port +" but it's already there - or servertype is not defined type::"+serverTyp +" "+s);
        return false;
        
    }
    
    
    
    public Vector getServerList(int serverTyp)
    { return (Vector)get(new Integer(serverTyp));}
    
    
    
    
    public Server getServer(int serverTyp, String name/*, String ip, String port*/)
    {return findServer(serverTyp,name/*,ip,port*/);}
    
    
    
    
    
    
    
    public boolean removeServer(int serverTyp, String name/*, String ip, String port*/)
    {
        Server s =findServer(serverTyp,name/*,ip,port*/);
        if(s!=null)
            
        {
            
            if(containsKey(new Integer(serverTyp)))
            {
                getServerList(serverTyp).remove(s);
                
                return true;
                
            }
            
            
            
        }
        
        return false;
        
    }
    
    
    
    protected Server findServer(int serverTyp, String name/*, String ip, String port*/)
    {
        Integer key = new Integer(serverTyp);
        
        if(!containsKey(key))
        {
            return null;
        }
        else
        {
            Vector s = getServerList(serverTyp);
            
            for(int i = 0;i<s.size();i++)
            {
                Server server = ((Server)s.get(i));
                
                if( server.getName().equalsIgnoreCase(name))
                {
                    return server;
                }
                
            }
        }
        
        
        
        return null;
    }
    
    
    
    
    
    
    
}