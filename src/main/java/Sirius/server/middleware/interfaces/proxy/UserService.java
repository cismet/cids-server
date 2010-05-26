/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.*;

import java.rmi.*;

import java.util.*;

/**
 * encapsulates methods relatad to user management.
 *
 * @version  $Revision$, $Date$
 */

public interface UserService extends Remote {

    //~ Methods ----------------------------------------------------------------

    // change password
    /**
     * changes users password.
     *
     * @param   user         user token of the user whose password is to be changed
     * @param   oldPassword  current password
     * @param   newPassword  new password
     *
     * @return  password changed successfully
     *
     * @throws  RemoteException  non user related server error
     * @throws  UserException    Sirius.server.newuser.UserException server error (eg wrong current password)
     */
    boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException, UserException;

    // login retrieving userAccessToken
    /**
     * retrieves a user token.
     *
     * @param   userGroupLsName  domain of the user group the user belongs to
     * @param   userGroupName    name of the user group the user belongs to
     * @param   userLsName       domain where the user is hosted
     * @param   userName         login of the user
     * @param   password         password
     *
     * @return  a user abject (user token)
     *
     * @throws  RemoteException  server error (non user dependant)
     * @throws  UserException    Sirius.server.newuser.UserException server error (eg bad login)
     */
    User getUser(String userGroupLsName, String userGroupName, String userLsName, String userName, String password)
            throws RemoteException, UserException;

    /**
     * list all available (of domain servers online) usergroups eg admins@altlasten.
     *
     * @return  list of user group names eg admins@altlasen
     *
     * @throws  RemoteException  server error (eg no domain servers online)
     */
    Vector getUserGroupNames() throws RemoteException;                    // Vector contains String[2] name +lsName

    /**
     * list all available (of domain servers online) usergroups eg admins@altlasten for a certain login.
     *
     * @param   userName  login name
     * @param   lsHome    domain where the user login is hosted
     *
     * @return  list of user group names eg admins@altlasen if userName is member of these user groups
     *
     * @throws  RemoteException  server error (bad login)
     */
    Vector getUserGroupNames(String userName, String lsHome) throws RemoteException;
}
