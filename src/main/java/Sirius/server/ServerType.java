package Sirius.server;

import java.util.*;


public class ServerType implements Comparable,Sirius.server.property.Createable
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    // Konstantendeklaration
    /** Konstante, die den ServerTyp NOT_PREDEFINED f\u00FCr Servertypen welche nicht in den folgenden Konstanten vorgesehen sind.**/
    public static final int NOT_PREDEFINED = 0;
    
    
    /** Konstante, die den ServerTyp LocalServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int, String, String)	unregisterServer},
     * {@link #getServerIP(int,String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int LOCALSERVER = 1;
    
    /** Konstante, die den ServerTyp CallServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int , String , String)	unregisterServer},
     * {@link #getServerIP(int, String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int CALLSERVER = 2;
    
    /** Konstante, die den ServerTyp ProtocolServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int , String, String)	unregisterServer},
     * {@link #getServerIP(int, String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int PROTOCOLSERVER = 3;
    
    /** Konstante, die den ServerTyp ProtocolServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int , String, String)	unregisterServer},
     * {@link #getServerIP(int, String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int USERSERVER = 4;
    
    ////////////			/** Konstante, die den ServerTyp TranslationServer repraesentiert. Wird benoetigt bei
    ////////////			{@link #registerServer(int, String, String) registerServer},
    ////////////			{@link #unregisterServer(int , String, String)	unregisterServer},
    ////////////			{@link #getServerIP(int, String) getServerIP }
    ////////////			{@link #getServerIPs(int) getServerIPs }**/
    ////////////			public static final int TRANSLATIONSERVER = 5;
    
    
    /** Konstante, die den ServerTyp ModelServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int , String, String)	unregisterServer},
     * {@link #getServerIP(int, String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int MODELSERVER = 6;
    
    /** Konstante, die den ServerTyp ModelServer repraesentiert. Wird benoetigt bei
     * {@link #registerServer(int, String, String) registerServer},
     * {@link #unregisterServer(int , String, String)	unregisterServer},
     * {@link #getServerIP(int, String) getServerIP }
     * {@link #getServerIPs(int) getServerIPs }**/
    public static final int IRSEARCHSERVER = 7;
    
    
    
    
    protected static final Hashtable typeStrings = new Hashtable(10);
    
    
    //class constuctor
    {
        typeStrings.put(new Integer(NOT_PREDEFINED),"unknown");
        typeStrings.put(new Integer(LOCALSERVER),"localServer");
        typeStrings.put(new Integer(CALLSERVER),"callServer");
        //typeStrings.put(new Integer(PROTOCOLSERVER),"protocolServer");
        typeStrings.put(new Integer(USERSERVER),"userServer");
         //typeStrings.put(new Integer(MODELSERVER),"modelServer");
        //typeStrings.put(new Integer(IRSEARCHSERVER),"irSearchServer");
        
        
    }
    
    
    
    public static String getBindString(int type)
    
    
    {
        // debug to make shure Class is loaded xxx
        new ServerType("",1);
        
        
        Object  o =typeStrings.get(new Integer(type));
       //logger.debug("type :"+type +"  "+ o);
        
        return o.toString();
        
        
    }
    
    
    
    
    
    
    //--------------------------------------------------------------------------------------------------
    
    
    protected int id;
    protected String name;
    
    //--------------------------------------------------------------------------------------------------
    
    
    
    public ServerType(String name,int id)
    {
        this.name = name;
        this.id=id;
        
        
    }
    
    //--------------------------------------------------------------------------------------------------
    
    /////////////Comparable///////////////////////////////////////////////////////////
    public int compareTo(Object o)
    {
        
        return ((ServerType)o).id-id;
        
        
    }
    //-------------------------------------------------------------------------
    
    public static int[] getAllServerTypes()
    {
        
        Enumeration enu = typeStrings.keys();
        int[] result = new int[typeStrings.size()];
        int i = 0;
        
        while(enu.hasMoreElements())
        {
            result[i]=((Integer)enu.nextElement()).intValue();
            i++;
            
            //assert i<size()
            
        }
        
        
        return result;
        
    }
    
    //--------------------------------------------------------------------------------
    // adds not predefined
    public static boolean addType(int id,String name)
    {
        Integer ID = new Integer(id);
        
        if(!typeStrings.contains(ID))
        {
            typeStrings.put(ID,name);
            return true;
        }
        else
        {
            return false;
            
        }
        
        
    }
    
    //------------------------------------------------------------------------------------------
    
    /////////////////////////////////////////////////////////////////////////
    
    public java.lang.Object createObject(String constructorArgs,String delimiter)
    {
        String[] args = tokenizeString(constructorArgs,delimiter);
        
        if(args.length == 2)
        {
            
            return new ServerType(args[0],new Integer(args[1]).intValue());
        }
        else
        {
           logger.error("<LS> ERROR Warning :: creatObject falsche Anzahl ConstructorParameter "+args.length);
            return null;
        }
        
        
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    private static final String[] tokenizeString(String s,String delimiter)
    {
        
        StringTokenizer tokenizer = new StringTokenizer(s,delimiter);
        String[] stringArray = new String[tokenizer.countTokens()];
        int i=0;
        
        while (tokenizer.hasMoreTokens())
        {
            stringArray[i++]=tokenizer.nextToken();
        }
        
        return stringArray;
        
        
        
    }
    
    
    
    
    
    
    
}