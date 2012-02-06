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

    //~ Instance fields --------------------------------------------------------

    /** Name des Servers. */
    private final String name;
    /** IP des Servers. */
    private final String ip;
    /** Port des Servers. */
    private final String serverPort;
    /** Servertyp. */
    private final int type;
    /** registry port. */
    private final String rmiPort;

    //~ Constructors -----------------------------------------------------------

    /**
     * Konstruktor.
     *
     * @param  serverType  Kennzeichnet die Art des Servers. es soll einer der Konstanten
     *                     {@link #LOCALSERVER LOCALSERVER} oder {@link #CALLSERVER CALLSERVER} verwendet werden.
     * @param  name        Name mit dem der Server angesprochen wird
     * @param  ip          the ip of the server and the registry
     * @param  rmiPort     the port where the rmi registry is running
     * @param  serverPort  the port where the server is exported
     */
    public Server(final int serverType,
            final String name,
            final String ip,
            final String rmiPort,
            final String serverPort) {
        this.type = serverType;
        this.name = name;
        this.ip = ip;
        this.rmiPort = rmiPort;
        this.serverPort = serverPort;
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
        if ((server != null) && this.name.equals(server.name) && (this.type == server.type)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (97 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        hash = (97 * hash) + this.type;

        return hash;
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
     * liefert die Adresse in Form von ip:serverPort (133.77.100.100:5555).*
     *
     * @return  DOCUMENT ME!
     */
    public String getAddress() {
        return (ip + ":" + serverPort); // NOI18N
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
    public String getServerPort() {
        return serverPort;
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
        return (type + ":" + name + ":" + ip + ":" + serverPort); // NOI18N
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
        return "//" + ip + ":" + rmiPort + "/" + type + "/" + name; // NOI18N
    }
}
