/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import java.rmi.*;

import Sirius.server.newuser.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */

public interface UserService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * change password.
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException, UserException;

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   password  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean validateUser(User user, String password) throws RemoteException;
}
