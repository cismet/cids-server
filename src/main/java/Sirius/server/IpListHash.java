
package Sirius.server;

//import de.cismet.tools.collections.StringMapsString;
import java.util.*;

public class IpListHash extends Hashtable<Integer,HashMap<String,String> >
{
    
    
//-------------------------------------------------------------------------
    public IpListHash()
    {super();init();}
    
    public IpListHash(int capacity)
    {super(capacity);init();}
    
    public IpListHash(int capacity,float factor)
    {super(capacity,factor);init();}
    
    
//-------------------------------------------------------------------------
    
    private void init()
    {
        //debug make sure class is loaded xxx
        new ServerType("",1);
        
        int[] types = ServerType.getAllServerTypes();
        
        for(int i=0;i<types.length;i++)
            put( new Integer(types[i]),new HashMap(10) );
        
        
    }
    
    //-------------------------------------------------------------------------
    
    
    public boolean addServerIP(int serverTyp, String name, String ip, String port) throws Exception
    {
        
        //vorsicht xxx ip port werden ueberschrieben
        if(!contains(serverTyp,name,ip,port))
        {
            getServerIPs(serverTyp).put(name,ip+":"+port);
            return true;
        }
        else
            
            return false;
        
        
        
    }
    
    
//----------------------------------------------------------------------
    
    
    public boolean contains(int serverTyp, String name, String ip, String port) throws Exception
    {
        
        HashMap<String,String> sms = getServerIPs(serverTyp);
        
        if(sms.containsKey(name))
        {
            if( sms.get(name).equals(ip+":"+port))
                return true;
        }
        
        return false;
        
    }
    
    
//-------------------------------------------------------------------------
    
    public boolean contains(Server server) throws Exception
    {
        return contains(server.getType(), server.getName(), server.getIP(), server.getPort());
        
        
    }
    
    //-------------------------------------------------------------------------
    
    
    /**Lievert zu einem definierten Servertyp alle Serverips in einer Datenstruktur StringMapsString*/
    public HashMap<String,String> getServerIPs(int serverTyp) throws Exception
    {
        return get(serverTyp);
    }
    
    
    
    
    //-------------------------------------------------------------------------
    
    
    /** liefert die IP eines Servers
     * @param serverTyp spezifiziert, um welchen Server es sich handelt (Call-,Local-,ProtocolServer),
     * es sollen die Konstanten {@link #CALLSERVER CALLSERVER}, {@link #LOCALSERVER LOCALSERVER} und
     * {@link #PROTOCOLSERVER PROTOCOLSERVER} benutzt werden.	 **/
    public String getServerIP(int serverTyp, String name) throws Exception
    {
        return ((HashMap<String,String>) get(serverTyp) ).get(name);
    }
    
    
    //-------------------------------------------------------------------------
    
    public boolean removeServerIP(int serverTyp, String name, String ip, String port) throws Exception
    {
        
        HashMap<String,String>  serverIPs = getServerIPs(serverTyp);
        
        if( contains(serverTyp,name,ip,port))
        {
            serverIPs.remove(name);
            return true;
            
        }
        else
            return false;
        
        
    }
    
    
//------------------------------------------------------------------------------------
    
    public boolean removeIPForServerType(int serverTyp) throws Exception
    {
        
        if( remove(getServerIPs(serverTyp) )!=null)
            return true;
        
        else
            return false;
        
        
    }
    
    
    
//-------------------------------------------------------------------------
    
    
    
}