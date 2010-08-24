/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Server implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1634105076311813828L;

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
        name = "default"; // NOI18N
        ip = "127.0.0.1"; // NOI18N
        rmiPort = "1099"; // NOI18N
        port = "1099";    // NOI18N
        type = ServerType.NOT_PREDEFINED;
    }

    /**
     * Creates a new Server object.
     *
     * @param  name  DOCUMENT ME!
     * @param  ip    DOCUMENT ME!
     * @param  port  DOCUMENT ME!
     */
    public Server(final String name, final String ip, final String port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.rmiPort = "1099"; // NOI18N
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
    public Server(final int serverType, final String name, final String ip, final String rmiPort) {
        this.type = serverType;
        this.name = name;
        this.ip = ip;
        this.rmiPort = rmiPort;
        port = "1099"; // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * ueberlaedt equals() von java.lang.Object. Es werden keine Referenzen verglichen, sondern die Membervariablen *
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean equals(final java.lang.Object obj) {
        final Server server = (Server)obj;
        if (this.name.equals(server.name) /* && this.ip.equals(server.ip) && this.port.equals(server.port) */
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
        return (ip + ":" + port); // NOI18N
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
        return "rmi://" + ip + ":" + rmiPort + "/" + type + "/" + name; // NOI18N
    }

    @Override
    public String toString() {
        return (type + ":" + name + ":" + ip + ":" + port); // NOI18N
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
        return type + "/" + name; // NOI18N
    }
}
