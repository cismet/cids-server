/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import java.util.Hashtable;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Server implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    /** Name des Servers.* */
    protected String name;
    /** IP des Servers.* */
    protected String ip;
    /** Port des Servers.* */
    protected String port;
    /** Servertyp.* */
    protected int type;

    protected String rmiPort;

    //~ Constructors -----------------------------------------------------------

    /**
     * StandardKonstruktor, setzt name = default, ip = 127.0.0.1, port 1099.*
     */
    public Server() {
        name = "default";
        ip = "127.0.0.1";
        rmiPort = "1099";
        port = "1099";
        type = ServerType.NOT_PREDEFINED;
    }

    /**
     * Creates a new Server object.
     *
     * @param  name  DOCUMENT ME!
     * @param  ip    DOCUMENT ME!
     * @param  port  DOCUMENT ME!
     */
    public Server(String name, String ip, String port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.rmiPort = "1099";
        this.type = ServerType.NOT_PREDEFINED;
    }

    /**
     * Konstruktor.
     *
     * @param  serverType  Kennzeichnet die Art des Servers. es soll einer der Konstanten
     *                     {@link #LOCALSERVER LOCALSERVER} oder {@link #CALLSERVER CALLSERVER} verwendet werden.
     * @param  name        Name mit dem der Server angesprochen wird
     * @param  ip          IP.
     * @param  rmiPort     port, Port auf dem die RMIRegistry laeuft und and der der Server angemeldet ist*
     */
    public Server(int serverType, String name, String ip, String rmiPort) {
        this.type = serverType;
        this.name = name;
        this.ip = ip;
        this.rmiPort = rmiPort;
        port = "1099";
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ueberlaedt equals() von java.lang.Object. Es werden keine Referenzen verglichen, sondern die Membervariablen *
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean equals(java.lang.Object obj) {
        Server server = (Server)obj;
        if (
            this.name.equals(server.name) /* && this.ip.equals(server.ip) && this.port.equals(server.port) */
                    && (this.type == server.type)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * liefert den Servernamen.*
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * liefert die Adresse in Form von ip:port (133.77.100.100:5555).*
     *
     * @return  DOCUMENT ME!
     */
    public String getAddress() {
        return (ip + ":" + port);
    }

    /**
     * liefert die IP.*
     *
     * @return  DOCUMENT ME!
     */
    public String getIP() {
        return ip;
    }

    /**
     * liefert Portnummer.*
     *
     * @return  DOCUMENT ME!
     */
    public String getPort() {
        return port;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRMIAddress() {
        return "rmi://" + ip + ":" + rmiPort + "/" + type + "/" + name;
    }

    public String toString() {
        return (type + ":" + name + ":" + ip + ":" + port);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRMIPort() {
        return rmiPort;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBindString() {
        return type + "/" + name;
    }
}
