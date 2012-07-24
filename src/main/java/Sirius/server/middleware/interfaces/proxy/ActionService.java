/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.User;

import java.io.File;

import java.rmi.RemoteException;

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
     * @param   taskname  DOCUMENT ME!
     * @param   jasong    DOCUMENT ME!
     * @param   domain    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Object executeTask(User user, String taskname, String jasong, String domain) throws RemoteException;
}
