/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry;

import Sirius.server.*;

import java.util.HashMap;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ServerManager {

    //~ Instance fields --------------------------------------------------------

    // protected ServerProperties props;
    protected ServerListHash servers;
    protected IpListHash ips;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerManager object.
     */
    ServerManager( /*ServerProperties props*/) {
        // this.props=props;

        servers = new ServerListHash();

        ips = new IpListHash();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean registerServer(final int serverTyp, final String name, final String ip) throws Exception {
        return registerServer(serverTyp, name, ""); // NOI18N
    }

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
    public boolean registerServer(final int serverTyp, final String name, final String ip, final String port)
            throws Exception {
        final boolean ipsDone = ips.addServerIP(serverTyp, name, ip, port);

        final boolean serversDone = servers.addServer(serverTyp, name, ip, port);

        return serversDone && ipsDone;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public boolean unregisterServer(final int serverTyp, final String name, final String ip) throws Exception {
        return unregisterServer(serverTyp, name, ""); // NOI18N
    }

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
    public boolean unregisterServer(final int serverTyp, final String name, final String ip, final String port)
            throws Exception {
        final boolean ipsDone = ips.removeServerIP(serverTyp, name, ip, port);
        final boolean serversDone = servers.removeServer(serverTyp, name /*,ip,port*/);

        return ipsDone && serversDone;
    }

//-------------------------------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public HashMap<String, String> getServerIPs(final int serverTyp) throws Exception {
        return ips.getServerIPs(serverTyp);
    }

    /**
     * -------------------------------------------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    String getServerIP(final int serverTyp, final String name) throws Exception {
        return ips.getServerIP(serverTyp, name);
    }

    /**
     * -------------------------------------------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Server[] getServers(final int serverTyp) {
        final Vector s = servers.getServerList(serverTyp);

        return (Server[])s.toArray(new Server[s.size()]);
    }

    /**
     * -------------------------------------------------------------------------------------------------------------
     *
     * @param   serverTyp   DOCUMENT ME!
     * @param   serverName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Server getServer(final int serverTyp, final String serverName) {
        return servers.getServer(serverTyp, serverName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getServerCount(final int serverTyp) {
        return servers.getServerList(serverTyp).size();
    }
}
