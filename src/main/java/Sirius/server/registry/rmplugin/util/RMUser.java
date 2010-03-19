/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class RMUser implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private String userName;
    private String userGroup;
    private String userDomain;
    private long onlineTime;
    private String ipAddress;
    private String qualifiedName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RMUser.
     *
     * @param  userName    DOCUMENT ME!
     * @param  group       DOCUMENT ME!
     * @param  Domain      DOCUMENT ME!
     * @param  onlineTime  DOCUMENT ME!
     * @param  ipAddress   DOCUMENT ME!
     */
    public RMUser(String userName, String group, String Domain, long onlineTime, String ipAddress) {
        this.userName = userName;
        this.onlineTime = onlineTime;
        this.ipAddress = ipAddress;
        this.userGroup = group;
        this.userDomain = Domain;
        qualifiedName = userName + "@" + userGroup + "@" + userDomain;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserName() {
        return userName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getOnlineTimeInMillis() {
        return onlineTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOnlineTimeAsString() {
        // long currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        // long elapsed = currentTime - onlineTime;
        return dateFormat.format(new Date(onlineTime));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserGroup() {
        return userGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserDomain() {
        return userDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
}
