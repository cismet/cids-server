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
    public boolean registerServer(int serverTyp, String name, String ip) throws Exception {
        return registerServer(serverTyp, name, "");
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
    public boolean registerServer(int serverTyp, String name, String ip, String port) throws Exception {
        boolean ipsDone = ips.addServerIP(serverTyp, name, ip, port);

        boolean serversDone = servers.addServer(serverTyp, name, ip, port);

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
    public boolean unregisterServer(int serverTyp, String name, String ip) throws Exception {
        return unregisterServer(serverTyp, name, "");
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
    public boolean unregisterServer(int serverTyp, String name, String ip, String port) throws Exception {
        boolean ipsDone = ips.removeServerIP(serverTyp, name, ip, port);
        boolean serversDone = servers.removeServer(serverTyp, name /*,ip,port*/);

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
    public HashMap<String, String> getServerIPs(int serverTyp) throws Exception {
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
    String getServerIP(int serverTyp, String name) throws Exception {
        return ips.getServerIP(serverTyp, name);
    }

    /**
     * -------------------------------------------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Server[] getServers(int serverTyp) {
        Vector s = servers.getServerList(serverTyp);

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
    public Server getServer(int serverTyp, String serverName) {
        return servers.getServer(serverTyp, serverName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getServerCount(int serverTyp) {
        return servers.getServerList(serverTyp).size();
    }
}
