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
import org.openide.util.Lookup;

import java.io.*;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;

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
    private HashMap<String, ServerAction> serverActionMap = new HashMap<String, ServerAction>();

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

        final Collection<? extends ServerAction> serverActions = Lookup.getDefault().lookupAll(ServerAction.class);
        for (final ServerAction serverAction : serverActions) {
            serverActionMap.put(serverAction.getTaskName(), serverAction);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object executeTask(final User user,
            final String domain,
            final String taskname,
            final Object body,
            final ServerActionParameter... params) {
        final ServerAction serverAction = serverActionMap.get(taskname);
        if (serverAction != null) {
            return serverAction.execute(body, params);
        } else {
            return null;
        }
    }
}
