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
//import Sirius.middleware.interfaces.domainserver.*;

import Sirius.server.localserver.user.LoginRestriction;
import Sirius.server.middleware.interfaces.domainserver.UserService;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.UserServer;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class UserServiceImpl {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserServiceImpl.class);
    private static final String DOMAINSPLITTER = "@";

    //~ Instance fields --------------------------------------------------------

    private UserServer userServer;
    private Hashtable activeLocalServers;
    private Hashtable<String, LoginRestriction> loginRestrictions = new Hashtable<String, LoginRestriction>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UserServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   userServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public UserServiceImpl(final Hashtable activeLocalServers, final UserServer userServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.userServer = userServer;
        final Collection<? extends LoginRestriction> lookupResults = Lookup.getDefault()
                    .lookupAll(LoginRestriction.class);
        for (final LoginRestriction lr : lookupResults) {
            loginRestrictions.put(lr.getKey(), lr);
        }
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
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password) throws RemoteException, UserException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser calles for user::" + userName); // NOI18N

            LOG.debug("userLsName:" + userLsName);           // NOI18N
            LOG.debug("userName:" + userName);               // NOI18N
            LOG.debug("userGroupLsName:" + userGroupLsName); // NOI18N
//            LOG.debug("userGroupName:" + userGroupName);                      // NOI18N
            LOG.debug((("password:" + password) == null) ? "null" : "*****"); // NOI18N
        }
        final User u = userServer.getUser(userGroupLsName, userGroupName, userLsName, userName, password);

        boolean validated = false;

        if (u != null) {
            final Sirius.server.middleware.interfaces.domainserver.UserService us =
                (Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(userLsName);

            if (us != null) {
                validated = us.validateUser(u, password);
            } else {
                throw new UserException(
                    "Login failed, home server of the user is not reachable :: "
                            + userName, // NOI18N
                    false,
                    false,
                    false,
                    true);
            }
        }

        if (validated) {
            final String loginRestrictionValue = getConfigAttr(u, "login.restriction");
            if (loginRestrictionValue != null) {
                if (loginRestrictionValue.endsWith("()")) {
                    final String key = loginRestrictionValue.substring(0, loginRestrictionValue.length() - 2);
                    final LoginRestriction restriction = loginRestrictions.get(key);
                    if (restriction != null) {
                        if (!restriction.isLoginAllowed()) {
                            throw new UserException("Login failed due to restriction:: " + userName,
                                false,
                                true,
                                false,
                                false); // NOI18N
                        }
                    } else {
                        throw new UserException("Login failed (Configured Restriction not found):: " + userName,
                            false,
                            true,
                            false,
                            false);     // NOI18N
                    }
                } else if (loginRestrictionValue.endsWith(")")) {
                    final String[] splits = loginRestrictionValue.substring(0, loginRestrictionValue.length())
                                .split("(");
                    final String key = splits[0];
                    final String value = splits[1];
                    final LoginRestriction restriction = loginRestrictions.get(key);
                    if (restriction != null) {
                        restriction.configure(value);
                        if (!restriction.isLoginAllowed()) {
                            throw new UserException("Login failed due to restriction:: " + userName,
                                false,
                                true,
                                false,
                                false); // NOI18N
                        }
                    } else {
                        throw new UserException("Login failed (Configured Restriction not found):: " + userName,
                            false,
                            true,
                            false,
                            false);     // NOI18N
                    }
                } else {
                    final LoginRestriction restriction = loginRestrictions.get(loginRestrictionValue);
                    if (restriction != null) {
                        if (!restriction.isLoginAllowed()) {
                            throw new UserException("Login failed due to restriction:: " + userName,
                                false,
                                true,
                                false,
                                false); // NOI18N
                        }
                    } else {
                        throw new UserException("Login failed (Configured Restriction not found):: " + userName,
                            false,
                            true,
                            false,
                            false);     // NOI18N
                    }
                }
            }

            return u;
        }

        throw new UserException("Login failed :: " + userName, false, true, false, false); // NOI18N
    }

    /**
     * result contains strings.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupName called"); // NOI18N
        }

        final Vector names = new Vector(20, 20);

        final Collection c = userServer.getUserGroups();

        final Iterator i = c.iterator();

        while (i.hasNext()) {
            final UserGroup tmpUserGroup;

            final String[] s = new String[2];
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
    public Vector getUserGroupNames(final String userName, final String lsHome) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupNames called for :username:" + userName); // NOI18N
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
    public boolean changePassword(final User user, final String oldPassword, final String newPassword)
            throws RemoteException, UserException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePassword called for :user:" + user); // NOI18N
        }
        return ((Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(user.getDomain()))
                    .changePassword(user, oldPassword, newPassword);
    }

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
    public String getConfigAttr(final User user, final String key) throws RemoteException {
        final String domain;
        final String realKey;
        if (key.contains(DOMAINSPLITTER)) {
            final String[] split = key.split(DOMAINSPLITTER);
            domain = split[1];
            realKey = split[0];
        } else {
            domain = user.getDomain();
            realKey = key;
        }
        final UserService userService = (UserService)activeLocalServers.get(domain);
        if (userService != null) {
            return userService.getConfigAttr(user, realKey);
        } else {
            return null;
        }
    }
}
