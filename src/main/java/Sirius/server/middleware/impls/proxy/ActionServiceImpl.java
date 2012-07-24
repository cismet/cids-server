/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MetaServiceImpl.java
 *
 * Created on 25. September 2003, 10:42
 */
package Sirius.server.middleware.impls.proxy;

import Sirius.server.Server;
import Sirius.server.ServerType;
import Sirius.server.middleware.interfaces.proxy.ActionService;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;
import Sirius.server.search.QueryExecuter;

import org.openide.util.Exceptions;

import java.io.*;

import java.rmi.RemoteException;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ActionServiceImpl implements ActionService {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            ActionServiceImpl.class);

    //~ Instance fields --------------------------------------------------------

    private Map activeLocalServers;
    private NameServer nameServer;
    private Server[] localServers;
    // resolves Query tree
    private QueryExecuter qex;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActionServiceImpl object.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   nameServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public ActionServiceImpl(final Map activeLocalServers, final NameServer nameServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
        this.localServers = nameServer.getServers(ServerType.LOCALSERVER);
        qex = new QueryExecuter(activeLocalServers);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object executeTask(final User user, final String taskname, final String domain) {
        try {
            final File file = new File(taskname);
            final FileInputStream fin = new FileInputStream(file);
            final byte[] fileContent = new byte[(int)file.length()];
            fin.read(fileContent);
            return fileContent;
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }
}
