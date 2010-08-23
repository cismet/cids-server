/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;
import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class IpListHash extends Hashtable<Integer, HashMap<String, String>> {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -6535120085516271972L;

    //~ Constructors -----------------------------------------------------------

    /**
     * -------------------------------------------------------------------------
     */
    public IpListHash() {
        super();
        init();
    }

    /**
     * Creates a new IpListHash object.
     *
     * @param  capacity  DOCUMENT ME!
     */
    public IpListHash(final int capacity) {
        super(capacity);
        init();
    }

    /**
     * Creates a new IpListHash object.
     *
     * @param  capacity  DOCUMENT ME!
     * @param  factor    DOCUMENT ME!
     */
    public IpListHash(final int capacity, final float factor) {
        super(capacity, factor);
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -------------------------------------------------------------------------
     */
    private void init() {
        // debug make sure class is loaded xxx
        new ServerType("", 1);  // NOI18N

        final int[] types = ServerType.getAllServerTypes();

        for (int i = 0; i < types.length; i++) {
            put(new Integer(types[i]), new HashMap(10));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     * @param   port       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean addServerIP(final int serverTyp, final String name, final String ip, final String port)
            throws Exception {
        // vorsicht xxx ip port werden ueberschrieben
        if (!contains(serverTyp, name, ip, port)) {
            getServerIPs(serverTyp).put(name, ip + ":" + port);  // NOI18N
            return true;
        } else {
            return false;
        }
    }

    /**
     * ----------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     * @param   port       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean contains(final int serverTyp, final String name, final String ip, final String port)
            throws Exception {
        final HashMap<String, String> sms = getServerIPs(serverTyp);

        if (sms.containsKey(name)) {
            if (sms.get(name).equals(ip + ":" + port)) {  // NOI18N
                return true;
            }
        }

        return false;
    }

    /**
     * -------------------------------------------------------------------------
     *
     * @param   server  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean contains(final Server server) throws Exception {
        return contains(server.getType(), server.getName(), server.getIP(), server.getPort());
    }

    // -------------------------------------------------------------------------

    /**
     * Lievert zu einem definierten Servertyp alle Serverips in einer Datenstruktur StringMapsString.
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public HashMap<String, String> getServerIPs(final int serverTyp) throws Exception {
        return get(serverTyp);
    }

    // -------------------------------------------------------------------------

    /**
     * liefert die IP eines Servers.
     *
     * @param   serverTyp  spezifiziert, um welchen Server es sich handelt (Call-,Local-,ProtocolServer), es sollen die
     *                     Konstanten {@link #CALLSERVER CALLSERVER}, {@link #LOCALSERVER LOCALSERVER} und
     *                     {@link #PROTOCOLSERVER PROTOCOLSERVER} benutzt werden. *
     * @param   name       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public String getServerIP(final int serverTyp, final String name) throws Exception {
        return ((HashMap<String, String>)get(serverTyp)).get(name);
    }

    /**
     * -------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     * @param   port       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean removeServerIP(final int serverTyp, final String name, final String ip, final String port)
            throws Exception {
        final HashMap<String, String> serverIPs = getServerIPs(serverTyp);

        if (contains(serverTyp, name, ip, port)) {
            serverIPs.remove(name);
            return true;
        } else {
            return false;
        }
    }

    /**
     * ------------------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean removeIPForServerType(final int serverTyp) throws Exception {
        if (remove(getServerIPs(serverTyp)) != null) {
            return true;
        } else {
            return false;
        }
    }

//-------------------------------------------------------------------------

}
