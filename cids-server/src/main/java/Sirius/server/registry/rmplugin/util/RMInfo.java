/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   Sebastian TODO Refactor
 * @version  $Revision$, $Date$
 */
public class RMInfo implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private String userName;
    private String userGroup;
    private String userDomain;
    private int port;
    private InetAddress address;
    private URI rmiAddress;
    private String key;
    private InetAddress ip;
    private long onlineSince;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RMInfo.
     *
     * @param  userName     DOCUMENT ME!
     * @param  userGroup    DOCUMENT ME!
     * @param  userDomain   DOCUMENT ME!
     * @param  port         DOCUMENT ME!
     * @param  onlineSince  DOCUMENT ME!
     * @param  ip           DOCUMENT ME!
     * @param  rmiAddress   DOCUMENT ME!
     */
    public RMInfo(final String userName,
            final String userGroup,
            final String userDomain,
            final int port,
            final long onlineSince,
            final InetAddress ip,
            final URI rmiAddress) {
        this.userName = userName;
        this.userGroup = userGroup;
        this.userDomain = userDomain;
        this.port = port;
        this.onlineSince = onlineSince;
        this.rmiAddress = rmiAddress;
        this.ip = ip;
        key = userName + "@" + userGroup + "@" + userDomain; // NOI18N
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
    public String getUserGroup() {
        return userGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getPort() {
        return port;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public URI getRmiAddress() {
        return rmiAddress;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RMInfo) {
            if (rmiAddress.equals(((RMInfo)obj).getRmiAddress())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getOnlineSince() {
        return onlineSince;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public InetAddress getIp() {
        return ip;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getOnlineTimeInMillis() {
        return (System.currentTimeMillis() - onlineSince);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getOnlineTimeInSeconds() {
        return (System.currentTimeMillis() - onlineSince) / 1000;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOnlineTimeAsText() {
        final long currentTime = System.currentTimeMillis();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); // NOI18N

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // NOI18N
        final long elapsed = currentTime - onlineSince;
        return dateFormat.format(new Date(elapsed));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIP() {
        return ip.getHostAddress();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getHost() {
        return ip.getHostName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserDomain() {
        return userDomain;
    }
}
