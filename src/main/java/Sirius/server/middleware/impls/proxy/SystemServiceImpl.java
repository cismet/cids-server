/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.impls.proxy;

import Sirius.server.middleware.interfaces.domainserver.SystemService;
import Sirius.server.naming.NameServer;

import Sirius.util.image.Image;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.Hashtable;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class SystemServiceImpl {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SystemServiceImpl.class);

    //~ Instance fields --------------------------------------------------------

    private final Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SystemServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public SystemServiceImpl(final Hashtable activeLocalServers, final NameServer nameServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   lsName             DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Image[] getDefaultIcons(final String lsName, final ConnectionContext connectionContext)
            throws RemoteException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Info <CS> getDefIcons from " + lsName); // NOI18N
        }
        Image[] i = new Image[0];
        SystemService s = null;

        try {
            s = (SystemService)activeLocalServers.get(lsName.trim());
            i = s.getDefaultIcons();
            if (LOG.isDebugEnabled()) {
                LOG.debug("image[] " + i);                                    // NOI18N
            }
        } catch (Exception e) {
            LOG.error("Info <CS> getDefIcons from " + lsName + " failed", e); // NOI18N

            throw new RemoteException("getDefIcons(lsName) failed", e); // NOI18N
        }

        return i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Image[] getDefaultIcons(final ConnectionContext connectionContext) throws RemoteException {
        Image[] i = new Image[0];
        SystemService s = null;

        try {
            if (activeLocalServers.size() > 0) {
                s = (SystemService)activeLocalServers.values().iterator().next();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("<CS> getDefIcons");                 // NOI18N
                }
                i = s.getDefaultIcons();
            } else {
                throw new Exception("no LocalServer registered!"); // NOI18N
            }
        } catch (Exception e) {
            LOG.error("Info <CS> getDefIcons failed", e);          // NOI18N

            throw new RemoteException("getDefIcons(void) failed", e); // NOI18N
        }

        return i;
    }
}
