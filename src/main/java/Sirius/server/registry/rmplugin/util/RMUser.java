/*
 * RMUser.java
 *
 * Created on 29. November 2006, 17:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.registry.rmplugin.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *
 * @author Sebastian
 */
public class RMUser implements Serializable {
    
    private String userName;
    private String userGroup;
    private String userDomain;
    private long onlineTime;
    private String ipAddress;
    private String qualifiedName;
    
    /** Creates a new instance of RMUser */
    public RMUser(String userName,String group,String Domain,long onlineTime,String ipAddress) {
        this.userName = userName;
        this.onlineTime = onlineTime;
        this.ipAddress = ipAddress;
        this.userGroup = group;
        this.userDomain = Domain;
        qualifiedName = userName + "@" + userGroup + "@" + userDomain;
    }

    public String getUserName() {
        return userName;
    }

    public long getOnlineTimeInMillis() {
        return onlineTime;
    }
    
    public String getOnlineTimeAsString() {
        //long currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("HH:mm:ss");
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        //long elapsed = currentTime - onlineTime;
        return dateFormat.format(new Date(onlineTime));
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
    
}
