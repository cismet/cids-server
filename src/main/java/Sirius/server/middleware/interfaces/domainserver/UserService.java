/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.domainserver;

import Sirius.server.localserver.user.PasswordCheckException;
import Sirius.server.newuser.*;

import java.rmi.*;

import java.security.Key;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
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
    /**
     * change password.
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     * @param   context      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  UserException           DOCUMENT ME!
     * @throws  PasswordCheckException  DOCUMENT ME!
     */
    boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException, PasswordCheckException;

    /**
     * DOCUMENT ME!
     *
     * @param   user               DOCUMENT ME!
     * @param   password           DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    User validateUser(final User user, String password, final ConnectionContext connectionContext)
            throws RemoteException, UserException;

    /**
     * DOCUMENT ME!
     *
     * @param   jwt                DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    User validateUser(final String jwt, final ConnectionContext connectionContext) throws RemoteException;

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
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   key      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   key      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean hasConfigAttr(final User user, final String key, final ConnectionContext context) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Key getPublicJwtKey() throws RemoteException;
}
