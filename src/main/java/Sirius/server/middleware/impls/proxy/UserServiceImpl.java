/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UserServiceImpl.java
 *
 * Created on 25. September 2003, 12:53
 */
package Sirius.server.middleware.impls.proxy;

import java.util.*;

import java.rmi.*;
import java.rmi.server.*;

import Sirius.server.newuser.*;
//import Sirius.middleware.interfaces.domainserver.*;
import Sirius.server.middleware.interfaces.proxy.*;
/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class UserServiceImpl {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private UserServer userServer;
    private java.util.Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UserServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   userServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public UserServiceImpl(java.util.Hashtable activeLocalServers, UserServer userServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.userServer = userServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Wie konnte das jemals gehen Falsche Reihenfolge in Signatur public User getUser( String userLsName, String
     * userName, String userGroupLsName, String userGroupName, String password) throws RemoteException, UserException {.
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
    public User getUser(
            String userGroupLsName,
            String userGroupName,
            String userLsName,
            String userName,
            String password) throws RemoteException, UserException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUser gerufen f\u00FCr user::" + userName);

            logger.debug("userLsName:" + userLsName);
            logger.debug("userName:" + userName);
            logger.debug("userGroupLsName:" + userGroupLsName);
            logger.debug("userGroupName:" + userGroupName);
            logger.debug("password:" + password);
        }
        User u = userServer.getUser(userLsName, userName, userGroupLsName, userGroupName, password);

        boolean validated = false;

        if (u != null) {
            Sirius.server.middleware.interfaces.domainserver.UserService us =
                (Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(userLsName);

            if (us != null) {
                validated = us.validateUser(u, password);
            } else {
                throw new UserException(
                    "Login fehlgeschlagen, Heimatserver des Users nicht erreichbar :: " + password,
                    false,
                    false,
                    false,
                    true);
            }
        }

        if (validated) {
            return u;
        }

        throw new UserException("Login fehlgeschlagen, Passwort falsch :: " + password, false, true, false, false);
    }

    /**
     * result contains strings.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames() throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUserGroupName gerufen");
        }

        Vector names = new Vector(20, 20);

        Collection c = userServer.getUserGroups();

        Iterator i = c.iterator();

        while (i.hasNext()) {
            UserGroup tmpUserGroup;

            String[] s = new String[2];
            tmpUserGroup = (UserGroup)i.next();

            s[0] = tmpUserGroup.getName();
            s[1] = tmpUserGroup.getDomain();

            names.add(s);
        }

        return names;
    }

    /**
     * result contains string[2] subset of all ugs.
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames(String userName, String lsHome) throws RemoteException {
        if (logger.isDebugEnabled()) {
            logger.debug("getUserGroupNames gerufen for :username:" + userName);
        }
        return userServer.getUserGroupNames(userName.trim(), lsHome.trim());
    }

    /**
     * DOCUMENT ME!
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
    public boolean changePassword(User user, String oldPassword, String newPassword) throws RemoteException,
        UserException {
        if (logger.isDebugEnabled()) {
            logger.debug("changePassword gerufen for :user:" + user);
        }
        return ((Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(user.getDomain()))
                    .changePassword(user, oldPassword, newPassword);
    }
}
