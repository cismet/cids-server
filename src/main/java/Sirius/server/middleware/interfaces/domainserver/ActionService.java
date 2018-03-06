/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.newuser.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ActionService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   taskname  DOCUMENT ME!
     * @param   body      jasong DOCUMENT ME!
     * @param   params    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    Object executeTask(User user, String taskname, Object body, ServerActionParameter... params) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   taskname  DOCUMENT ME!
     * @param   context   DOCUMENT ME!
     * @param   body      DOCUMENT ME!
     * @param   params    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Object executeTask(User user,
            String taskname,
            final ConnectionContext context,
            Object body,
            ServerActionParameter... params) throws RemoteException;
}
