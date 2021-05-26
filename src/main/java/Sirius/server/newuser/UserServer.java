/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;

import java.rmi.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public interface UserServer extends Remote {

    //~ Methods ----------------------------------------------------------------

    // public void login(User u) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Vector getUsers() throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   userGroupLocalServerName  DOCUMENT ME!
     * @param   userGroupName             DOCUMENT ME!
     * @param   userLocalServerName       DOCUMENT ME!
     * @param   userName                  DOCUMENT ME!
     * @param   password                  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    User getUser(
            String userGroupLocalServerName,
            String userGroupName,
            String userLocalServerName,
            String userName,
            String password) throws RemoteException, UserException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void registerUser(User user) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void registerUsers(Vector users) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   users  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void unregisterUsers(Vector users) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void unregisterUser(User user) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   localServerName  DOCUMENT ME!
     * @param   userGroup        DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void registerUserGroup(String localServerName, UserGroup userGroup) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   localServerName  DOCUMENT ME!
     * @param   userGroups       DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void registerUserGroups(String localServerName, Vector userGroups) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   localServerName  DOCUMENT ME!
     * @param   userGroups       DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void unregisterUserGroups(String localServerName, Vector userGroups) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   localServerName  DOCUMENT ME!
     * @param   userGroups       DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void unregisterUserGroup(String localServerName, UserGroup userGroups) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   membership  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean registerUserMembership(Membership membership) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   memberships  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    void registerUserMemberships(Vector memberships) throws RemoteException;

    /**
     * liefert alle Benutergruppen.*
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Vector getUserGroups() throws RemoteException;

    /**
     * liefert einen Vector mit String-Arrays[2] String[0] - userName String[1] - userLocalServerName.*
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Vector getUserGroupNames(User user) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsName    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Vector getUserGroupNames(String userName, String lsName) throws RemoteException;

    // public Vector getUserGroups(String userName,String lsName)
    // throws RemoteException;

    /** liefert alle Benutzergruppen eines Benutzers **/
    // public Vector getUserGroups(User user)
    // throws RemoteException;

}
