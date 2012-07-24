/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.User;

import java.rmi.RemoteException;

import de.cismet.cids.server.actions.ServerActionParameter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface ActionService {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     * @param   taskname  DOCUMENT ME!
     * @param   body      jasong DOCUMENT ME!
     * @param   params    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Object executeTask(User user, String domain, String taskname, Object body, ServerActionParameter... params)
            throws RemoteException;
}
