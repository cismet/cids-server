/*
 * ServerStatus.java
 *
 * Created on 2. M\u00E4rz 2004, 10:10
 */

package Sirius.server;


import java.util.*;

/**
 *
 * @author  schlob
 */
public class ServerStatus {
    
    protected LinkedHashMap msg;
    long lastUpdate;
    long lastGet;
    
    /** Creates a new instance of ServerStatus */
    public ServerStatus() {
        
        msg = new LinkedHashMap(100);
        lastUpdate=System.currentTimeMillis();
        lastGet=lastUpdate;
    }
    
    
    public HashMap getMessages(){lastGet=System.currentTimeMillis();return msg;}
    
    
    public Object getMessage(String key){lastGet=System.currentTimeMillis();return  (msg.containsKey(key)? msg.get(key) :null);}
    
    
    public Collection getAllMessages(){lastGet=System.currentTimeMillis();return msg.values();}
    
    public Collection getMessageKeys(){lastGet=System.currentTimeMillis();return msg.keySet();}
    
    
    public void addMessage(String key,String message){msg.put(key,message); lastUpdate=System.currentTimeMillis();}
    
   
    public boolean newMessage(){return (lastUpdate>lastGet);}
    
}
