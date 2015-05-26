/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Vector;

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

    /**
     * Retrieves the value for the given key for the given {@link User}. That means if a user requests a value for a key
     * the value is delivered with the following strategy:
     *
     * <ul>
     *   <li>the value of the <code>User</code> if it is set</li>
     *   <li>the value of the <code>User</code>'s {@link UserGroup} if it is set</li>
     *   <li>the value of the <code>UserGroup</code>'s <code>Domain</code> if it is set.</li>
     *   <li><code>null</code> otherwise.</li>
     * </ul>
     * <br>
     * <br>
     * If you want to explicitely retrieve the value for the <code>UserGroup</code> though the <code>User</code>'s value
     * is set hand over an <code>User</code> object with <code>id &lt; 0</code>.<br>
     * If you want to explicitely retrieve the value for the <code>Domain</code> though any other value is set hand over
     * an <code>User</code> object with <code>id &lt; 0</code> that contains a <code>UserGroup</code> object with <code>id
     * &lt; 0</code>.
     *
     * @param   user  the <code>User</code> whose value shall be retrieved
     * @param   key   the key for the value to be retrieved
     *
     * @return  the associated value or <code>null</code>
     *
     * @throws  RemoteException  if an internal error occurs
     */
    String getConfigAttr(final User user, final String key) throws RemoteException;

    /**
     * Determines whether the given {@link User} has a value associated with the given key or not. For value retrieval
     * strategy see {@link #getConfigAttr(Sirius.server.newuser.User, java.lang.String) }.
     *
     * @param   user  the <code>User</code> to be checked
     * @param   key   the key to be checked
     *
     * @return  true whether there is a value associated with the given key for the given <code>User</code>, false
     *          otherwise.
     *
     * @throws  RemoteException  if an internal error occurs
     *
     * @see     #getConfigAttr(Sirius.server.newuser.User, java.lang.String)
     */
    boolean hasConfigAttr(final User user, final String key) throws RemoteException;
}
