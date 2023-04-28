/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ServerListHash extends Hashtable {

    //~ Static fields/initializers ---------------------------------------------

    // 'Elemente sind Vectoren von servern

    private static final transient Logger LOG = Logger.getLogger(ServerListHash.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerListHash object.
     */
    public ServerListHash() {
        super();
        init();
    }

    /**
     * Creates a new ServerListHash object.
     *
     * @param  capacity  DOCUMENT ME!
     */
    public ServerListHash(final int capacity) {
        super(capacity);
        init();
    }

    /**
     * Creates a new ServerListHash object.
     *
     * @param  capacity    DOCUMENT ME!
     * @param  loadfactor  DOCUMENT ME!
     */
    public ServerListHash(final int capacity, final float loadfactor) {
        super(capacity, loadfactor);
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * -------------------------------------------------------------------------
     */
    private void init() {
        // FIXME: nasty workaround
        // Debug xxx not necessary if Xoption for Garbagecollector ist set
        new ServerType("", 1); // NOI18N

        final int[] types = ServerType.getAllServerTypes();

        for (int i = 0; i < types.length; i++) {
            put(types[i], new Vector(5, 5));
        }
    }

    /**
     * -------------------------------------------------------------------------------------------------
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     * @param   ip         DOCUMENT ME!
     * @param   port       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addServer(final int serverTyp, final String name, final String ip, final String port) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("server there? " + name + " not null?"); // NOI18N
        }

        if (findServer(serverTyp, name) != null) {
            LOG.warn("adding server " + name + " " + ip + " " + port + " but it's already there. gonna replace it");
        }
        if (containsKey(serverTyp)) {
            getServerList(serverTyp).add(new Server(serverTyp, name, ip, port, port));
            return true;
        } else {
            LOG.error("tried to add server " + name + " " + ip + " " + port + " but servertype is not defined type::"
                        + serverTyp);
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector getServerList(final int serverTyp) {
        return (Vector)get(serverTyp);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Server getServer(final int serverTyp, final String name) {
        return findServer(serverTyp, name);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeServer(final int serverTyp, final String name) {
        final Server s = findServer(serverTyp, name);

        if (s != null) {
            if (containsKey(serverTyp)) {
                getServerList(serverTyp).remove(s);

                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serverTyp  DOCUMENT ME!
     * @param   name       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Server findServer(final int serverTyp, final String name) {
        final Integer key = serverTyp;

        if (!containsKey(key)) {
            return null;
        } else {
            final Vector s = getServerList(serverTyp);

            for (int i = 0; i < s.size(); i++) {
                final Server server = ((Server)s.get(i));

                if (server.getName().equalsIgnoreCase(name)) {
                    return server;
                }
            }
        }

        return null;
    }
}
