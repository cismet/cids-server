/*
 * RMInfo.java
 *
 * Created on 23. November 2006, 16:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.registry.rmplugin.util;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Sebastian
 * TODO Refactor
 */
public class RMInfo implements Serializable {
    
    
    private String userName;
    private String userGroup;
    private String userDomain;
    private int    port;
    private InetAddress address;
    private URI rmiAddress;
    private String key;
    private InetAddress ip;
    private long onlineSince;
    
    
    /** Creates a new instance of RMInfo */
    public RMInfo(String userName,String userGroup,String userDomain,int port,long onlineSince,InetAddress ip,URI rmiAddress) {
        this.userName = userName;
        this.userGroup = userGroup;
        this.userDomain = userDomain;
        this.port = port;
        this.onlineSince = onlineSince;
        this.rmiAddress = rmiAddress;
        this.ip = ip;
        key = userName + "@" + userGroup + "@" + userDomain;
    }
    
    public String getUserName() {
        return userName;
    }
    
    
    public String getUserGroup() {
        return userGroup;
    }
    
    
    public int getPort() {
        return port;
    }
    
    
    public URI getRmiAddress() {
        return rmiAddress;
    }
    
    
    
    public String getKey() {
        return key;
    }
    
    
    
    public boolean equals(Object obj) {
        if(obj instanceof RMInfo){
            if(rmiAddress.equals(((RMInfo) obj).getRmiAddress())){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public long getOnlineSince() {
        return onlineSince;
    }
    
    public InetAddress getIp() {
        return ip;
    }
    
    public long getOnlineTimeInMillis(){
        return (System.currentTimeMillis() - onlineSince);
    }
    
    public long getOnlineTimeInSeconds(){
        return (System.currentTimeMillis() - onlineSince)/1000;
    }
    
    public String getOnlineTimeAsText(){
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("HH:mm:ss");
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long elapsed = currentTime - onlineSince;
        return dateFormat.format(new Date(elapsed));
    }
    
    public String getIP(){
        return ip.getHostAddress();
    }
    
    public String getHost(){
        return ip.getHostName();
    }

    public String getUserDomain() {
        return userDomain;
    }
}
