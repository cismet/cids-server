/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.localserver.user.PasswordCheckException;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.security.Key;

import java.util.Vector;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * encapsulates methods relatad to user management.
 *
 * @version  $Revision$, $Date$
 */

public interface UserService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  UserException           DOCUMENT ME!
     * @throws  PasswordCheckException  DOCUMENT ME!
     */
    @Deprecated
    boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException,
        UserException,
        PasswordCheckException;

    // change password
    /**
     * changes users password.
     *
     * @param   user         user token of the user whose password is to be changed
     * @param   oldPassword  current password
     * @param   newPassword  new password
     * @param   context      DOCUMENT ME!
     *
     * @return  password changed successfully
     *
     * @throws  RemoteException         non user related server error
     * @throws  UserException           Sirius.server.newuser.UserException server error (eg wrong current password)
     * @throws  PasswordCheckException  DOCUMENT ME!
     */
    boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException, PasswordCheckException;

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    @Deprecated
    User getUser(String userGroupLsName, String userGroupName, String userLsName, String userName, String password)
            throws RemoteException, UserException;

    // login retrieving userAccessToken
    /**
     * retrieves a user token.
     *
     * @param   userGroupLsName  domain of the user group the user belongs to
     * @param   userGroupName    name of the user group the user belongs to
     * @param   userLsName       domain where the user is hosted
     * @param   userName         login of the user
     * @param   password         password
     * @param   context          DOCUMENT ME!
     *
     * @return  a user abject (user token)
     *
     * @throws  RemoteException  server error (non user dependant)
     * @throws  UserException    Sirius.server.newuser.UserException server error (eg bad login)
     */
    User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password,
            final ConnectionContext context) throws RemoteException, UserException;

    /**
     * DOCUMENT ME!
     *
     * @param   domain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Key getPublicJwtKey(String domain) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    Vector getUserGroupNames() throws RemoteException; // Vector contains String[2] name +lsName
    /**
     * list all available (of domain servers online) usergroups eg admins@altlasten.
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  list of user group names eg admins@altlasen
     *
     * @throws  RemoteException  server error (eg no domain servers online)
     */
    Vector getUserGroupNames(final ConnectionContext context) throws RemoteException; // Vector contains String[2]
                                                                                      // name +lsName

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    Vector getUserGroupNames(String userName, String lsHome) throws RemoteException;

    /**
     * list all available (of domain servers online) usergroups eg admins@altlasten for a certain login.
     *
     * @param   userName  login name
     * @param   lsHome    domain where the user login is hosted
     * @param   context   DOCUMENT ME!
     *
     * @return  list of user group names eg admins@altlasen if userName is member of these user groups
     *
     * @throws  RemoteException  server error (bad login)
     */
    Vector getUserGroupNames(final String userName, final String lsHome, final ConnectionContext context)
            throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   key   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    String getConfigAttr(final User user, final String key) throws RemoteException;

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
     * <br/>
     * <br/>
     * If you want to explicitely retrieve the value for the <code>UserGroup</code> though the <code>User</code>'s value
     * is set hand over an <code>User</code> object with <code>id < 0</code>.<br/>
     * If you want to explicitely retrieve the value for the <code>Domain</code> though any other value is set hand over
     * an <code>User</code> object with <code>id < 0</code> that contains a <code>UserGroup</code> object with <code>id
     * < 0</code>.
     *
     * @param   user     the <code>User</code> whose value shall be retrieved
     * @param   key      the key for the value to be retrieved
     * @param   context  DOCUMENT ME!
     *
     * @return  the associated values or <code>null</code>
     *
     * @throws  RemoteException  if an internal error occurs
     */
    String getConfigAttr(final User user, final String key, final ConnectionContext context) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   key   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    @Deprecated
    boolean hasConfigAttr(final User user, final String key) throws RemoteException;

    /**
     * Determines whether the given {@link User} has a value associated with the given key or not. For value retrieval
     * strategy see {@link #getConfigAttr(Sirius.server.newuser.User, java.lang.String) }.
     *
     * @param   user     the <code>User</code> to be checked
     * @param   key      the key to be checked
     * @param   context  DOCUMENT ME!
     *
     * @return  true whether there is a value associated with the given key for the given <code>User</code>, false
     *          otherwise.
     *
     * @throws  RemoteException  if an internal error occurs
     *
     * @see     #getConfigAttr(Sirius.server.newuser.User, java.lang.String)
     */
    boolean hasConfigAttr(final User user, final String key, final ConnectionContext context) throws RemoteException;
}
