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

import Sirius.server.middleware.interfaces.proxy.ActionService;
import Sirius.server.naming.NameServer;
import Sirius.server.newuser.User;

import java.rmi.RemoteException;

import java.util.Map;

import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.connectioncontext.ClientConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContext;

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

    private final Map activeLocalServers;

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
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    @Deprecated
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return executeTask(user, taskname, taskdomain, ClientConnectionContext.createDeprecated(), body, params);
    }

    @Override
    public Object executeTask(final User user,
            final String taskname,
            final String taskdomain,
            final ConnectionContext context,
            final Object body,
            final ServerActionParameter... params) throws RemoteException {
        return ((Sirius.server.middleware.interfaces.domainserver.ActionService)activeLocalServers.get(taskdomain))
                    .executeTask(user, taskname, context, body, params);
    }
}
