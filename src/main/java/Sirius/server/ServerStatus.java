/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ServerStatus.java
 *
 * Created on 2. M\u00E4rz 2004, 10:10
 */
package Sirius.server;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ServerStatus {

    //~ Instance fields --------------------------------------------------------

    protected LinkedHashMap msg;
    long lastUpdate;
    long lastGet;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ServerStatus.
     */
    public ServerStatus() {
        msg = new LinkedHashMap(100);
        lastUpdate = System.currentTimeMillis();
        lastGet = lastUpdate;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap getMessages() {
        lastGet = System.currentTimeMillis();
        return msg;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getMessage(String key) {
        lastGet = System.currentTimeMillis();
        return (msg.containsKey(key) ? msg.get(key) : null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getAllMessages() {
        lastGet = System.currentTimeMillis();
        return msg.values();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getMessageKeys() {
        lastGet = System.currentTimeMillis();
        return msg.keySet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key      DOCUMENT ME!
     * @param  message  DOCUMENT ME!
     */
    public void addMessage(String key, String message) {
        msg.put(key, message);
        lastUpdate = System.currentTimeMillis();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean newMessage() {
        return (lastUpdate > lastGet);
    }
}
